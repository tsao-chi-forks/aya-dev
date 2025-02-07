// Copyright (c) 2020-2021 Yinsen (Tesla) Zhang.
// Use of this source code is governed by the MIT license that can be found in the LICENSE.md file.
package org.aya.concrete;

import kala.collection.immutable.ImmutableSeq;
import kala.collection.mutable.DynamicSeq;
import kala.control.Either;
import kala.tuple.Unit;
import kala.value.Ref;
import org.aya.api.concrete.ConcreteExpr;
import org.aya.api.distill.AyaDocile;
import org.aya.api.distill.DistillerOptions;
import org.aya.api.error.Reporter;
import org.aya.api.ref.LocalVar;
import org.aya.api.ref.Var;
import org.aya.concrete.desugar.AyaBinOpSet;
import org.aya.concrete.desugar.Desugarer;
import org.aya.concrete.resolve.context.ModuleContext;
import org.aya.concrete.resolve.visitor.ExprResolver;
import org.aya.concrete.stmt.QualifiedID;
import org.aya.distill.BaseDistiller;
import org.aya.distill.ConcreteDistiller;
import org.aya.generic.Level;
import org.aya.generic.ParamLike;
import org.aya.generic.ref.PreLevelVar;
import org.aya.pretty.doc.Doc;
import org.aya.tyck.ExprTycker;
import org.aya.util.binop.BinOpParser;
import org.aya.util.error.SourceNode;
import org.aya.util.error.SourcePos;
import org.aya.util.error.WithPos;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * @author re-xyr
 */
public sealed interface Expr extends ConcreteExpr {
  <P, R> R doAccept(@NotNull Visitor<P, R> visitor, P p);

  default <P, R> R accept(@NotNull Visitor<P, R> visitor, P p) {
    visitor.traceEntrance(this, p);
    var ret = doAccept(visitor, p);
    visitor.traceExit(ret, this, p);
    return ret;
  }

  /**
   * @see org.aya.concrete.stmt.Stmt#resolve
   * @see org.aya.concrete.resolve.visitor.StmtShallowResolver
   */
  @Contract(pure = true)
  default Expr resolve(@NotNull ModuleContext context) {
    var exprResolver = new ExprResolver(ExprResolver.RESTRICTIVE);
    return accept(exprResolver, context);
  }

  @Override default @NotNull Expr desugar(@NotNull Reporter reporter) {
    return accept(new Desugarer(new AyaBinOpSet(reporter)), Unit.unit());
  }

  @Override default @NotNull Doc toDoc(@NotNull DistillerOptions options) {
    return accept(new ConcreteDistiller(options), BaseDistiller.Outer.Free);
  }

  interface Visitor<P, R> {
    default void traceEntrance(@NotNull Expr expr, P p) {
    }
    default void traceExit(R r, @NotNull Expr expr, P p) {
    }
    R visitRef(@NotNull RefExpr expr, P p);
    R visitUnresolved(@NotNull UnresolvedExpr expr, P p);
    R visitLam(@NotNull LamExpr expr, P p);
    R visitPi(@NotNull PiExpr expr, P p);
    R visitSigma(@NotNull SigmaExpr expr, P p);
    R visitRawUniv(@NotNull RawUnivExpr expr, P p);
    R visitUniv(@NotNull UnivExpr expr, P p);
    R visitApp(@NotNull AppExpr expr, P p);
    R visitHole(@NotNull HoleExpr expr, P p);
    R visitTup(@NotNull TupExpr expr, P p);
    R visitProj(@NotNull ProjExpr expr, P p);
    R visitNew(@NotNull NewExpr expr, P p);
    R visitLitInt(@NotNull LitIntExpr expr, P p);
    R visitRawUnivArgs(@NotNull RawUnivArgsExpr expr, P p);
    R visitUnivArgs(@NotNull UnivArgsExpr expr, P p);
    R visitLsuc(@NotNull LSucExpr expr, P p);
    R visitLmax(@NotNull LMaxExpr expr, P p);
    R visitLitString(@NotNull LitStringExpr expr, P p);
    R visitBinOpSeq(@NotNull BinOpSeq binOpSeq, P p);
    R visitError(@NotNull ErrorExpr error, P p);
  }

  sealed interface WithTerm extends Expr {
    @NotNull Ref<ExprTycker.Result> theCore();
    default @Nullable ExprTycker.Result core() {
      return theCore().value;
    }
  }

  /**
   * @author re-xyr
   */
  record UnresolvedExpr(
    @NotNull SourcePos sourcePos,
    @NotNull QualifiedID name
  ) implements Expr {
    public UnresolvedExpr(@NotNull SourcePos sourcePos, @NotNull String name) {
      this(sourcePos, new QualifiedID(sourcePos, name));
    }

    @Override public <P, R> R doAccept(@NotNull Visitor<P, R> visitor, P p) {
      return visitor.visitUnresolved(this, p);
    }
  }

  record ErrorExpr(@NotNull SourcePos sourcePos, @NotNull AyaDocile description) implements Expr {
    public ErrorExpr(@NotNull SourcePos sourcePos, @NotNull Doc description) {
      this(sourcePos, options -> description);
    }

    @Override public <P, R> R doAccept(@NotNull Visitor<P, R> visitor, P p) {
      return visitor.visitError(this, p);
    }
  }

  /**
   * @author ice1000
   */
  record HoleExpr(
    @Override @NotNull SourcePos sourcePos,
    boolean explicit,
    @Nullable Expr filling,
    Ref<ImmutableSeq<LocalVar>> accessibleLocal
  ) implements Expr {
    public HoleExpr(@NotNull SourcePos sourcePos, boolean explicit, @Nullable Expr filling) {
      this(sourcePos, explicit, filling, new Ref<>());
    }

    @Override public <P, R> R doAccept(@NotNull Visitor<P, R> visitor, P p) {
      return visitor.visitHole(this, p);
    }
  }

  /**
   * @author re-xyr
   */
  record AppExpr(
    @NotNull SourcePos sourcePos,
    @NotNull Expr function,
    @NotNull NamedArg argument
  ) implements Expr {
    @Override public <P, R> R doAccept(@NotNull Visitor<P, R> visitor, P p) {
      return visitor.visitApp(this, p);
    }
  }

  static @NotNull Expr unapp(@NotNull Expr expr, @NotNull DynamicSeq<NamedArg> args) {
    while (expr instanceof AppExpr app) {
      args.append(app.argument);
      expr = app.function;
    }
    args.reverse();
    return expr;
  }

  /**
   * @author AustinZhu
   */
  record NamedArg(boolean explicit, @Nullable String name, @NotNull Expr expr)
    implements AyaDocile, SourceNode, BinOpParser.Elem<Expr> {

    public NamedArg(boolean explicit, @NotNull Expr expr) {
      this(explicit, null, expr);
    }

    @Override public @NotNull Doc toDoc(@NotNull DistillerOptions options) {
      var doc = name == null ? expr.toDoc(options) :
        Doc.braced(Doc.sep(Doc.plain(name), Doc.symbol("=>"), expr.toDoc(options)));
      return Doc.bracedUnless(doc, explicit);
    }

    @Override public @NotNull SourcePos sourcePos() {
      return expr.sourcePos();
    }
  }

  /**
   * @author re-xyr
   */
  record PiExpr(
    @NotNull SourcePos sourcePos,
    boolean co,
    @NotNull Expr.Param param,
    @NotNull Expr last
  ) implements Expr {
    @Override public <P, R> R doAccept(@NotNull Visitor<P, R> visitor, P p) {
      return visitor.visitPi(this, p);
    }
  }

  /**
   * @author re-xyr
   */
  record LamExpr(
    @NotNull SourcePos sourcePos,
    @NotNull Expr.Param param,
    @NotNull Expr body
  ) implements Expr {
    @Override public <P, R> R doAccept(@NotNull Visitor<P, R> visitor, P p) {
      return visitor.visitLam(this, p);
    }
  }

  /**
   * @author re-xyr
   */
  record SigmaExpr(
    @NotNull SourcePos sourcePos,
    boolean co,
    @NotNull ImmutableSeq<@NotNull Param> params
  ) implements Expr {
    @Override public <P, R> R doAccept(@NotNull Visitor<P, R> visitor, P p) {
      return visitor.visitSigma(this, p);
    }
  }

  /**
   * <pre>
   * def infix + add (a b : Nat) => ...
   * def test => zero + zero
   * </pre>
   *
   * @author ice1000
   */
  record RefExpr(
    @NotNull SourcePos sourcePos,
    @NotNull Var resolvedVar,
    @NotNull Ref<ExprTycker.Result> theCore
  ) implements Expr, WithTerm {
    public RefExpr(@NotNull SourcePos sourcePos, @NotNull Var resolvedVar) {
      this(sourcePos, resolvedVar, new Ref<>());
    }

    @Override public <P, R> R doAccept(@NotNull Visitor<P, R> visitor, P p) {
      return visitor.visitRef(this, p);
    }
  }

  /**
   * @author re-xyr, ice1000
   */
  record RawUnivExpr(@NotNull SourcePos sourcePos) implements Expr {
    @Override public <P, R> R doAccept(@NotNull Visitor<P, R> visitor, P p) {
      return visitor.visitRawUniv(this, p);
    }
  }

  record UnivExpr(@NotNull SourcePos sourcePos, @NotNull Level<PreLevelVar> level) implements Expr {
    @Override public <P, R> R doAccept(@NotNull Visitor<P, R> visitor, P p) {
      return visitor.visitUniv(this, p);
    }
  }

  record RawUnivArgsExpr(@NotNull SourcePos sourcePos, @NotNull ImmutableSeq<Expr> univArgs) implements Expr {
    @Override public <P, R> R doAccept(@NotNull Visitor<P, R> visitor, P p) {
      return visitor.visitRawUnivArgs(this, p);
    }
  }

  record UnivArgsExpr(
    @Override @NotNull SourcePos sourcePos,
    @NotNull ImmutableSeq<Level<PreLevelVar>> univArgs
  ) implements Expr {
    @Override public <P, R> R doAccept(@NotNull Visitor<P, R> visitor, P p) {
      return visitor.visitUnivArgs(this, p);
    }
  }

  /**
   * @author re-xyr
   */
  record TupExpr(
    @Override @NotNull SourcePos sourcePos,
    @NotNull ImmutableSeq<@NotNull Expr> items
  ) implements Expr {
    @Override public <P, R> R doAccept(@NotNull Visitor<P, R> visitor, P p) {
      return visitor.visitTup(this, p);
    }
  }

  /**
   * @param resolvedIx will be modified during tycking
   * @author re-xyr
   */
  record ProjExpr(
    @NotNull SourcePos sourcePos,
    @NotNull Expr tup,
    @NotNull Either<Integer, WithPos<String>> ix,
    @NotNull Ref<@Nullable Var> resolvedIx,
    @NotNull Ref<ExprTycker.Result> theCore
  ) implements Expr, WithTerm {
    public ProjExpr(
      @NotNull SourcePos sourcePos, @NotNull Expr tup,
      @NotNull Either<Integer, WithPos<String>> ix,
      @NotNull Ref<@Nullable Var> resolvedIx
    ) {
      this(sourcePos, tup, ix, resolvedIx, new Ref<>());
    }

    @Override public <P, R> R doAccept(@NotNull Visitor<P, R> visitor, P p) {
      return visitor.visitProj(this, p);
    }
  }

  record NewExpr(
    @NotNull SourcePos sourcePos,
    @NotNull Expr struct,
    @NotNull ImmutableSeq<Field> fields
  ) implements Expr {
    @Override public <P, R> R doAccept(@NotNull Visitor<P, R> visitor, P p) {
      return visitor.visitNew(this, p);
    }
  }

  record Field(
    @NotNull String name,
    @NotNull ImmutableSeq<WithPos<LocalVar>> bindings,
    @NotNull Expr body
  ) {
  }

  /**
   * @author kiva
   */
  record LitIntExpr(@NotNull SourcePos sourcePos, int integer) implements Expr {
    @Override public <P, R> R doAccept(@NotNull Visitor<P, R> visitor, P p) {
      return visitor.visitLitInt(this, p);
    }
  }

  record LSucExpr(@NotNull SourcePos sourcePos, @NotNull Expr expr) implements Expr {
    @Override public <P, R> R doAccept(@NotNull Visitor<P, R> visitor, P p) {
      return visitor.visitLsuc(this, p);
    }
  }

  record LMaxExpr(@NotNull SourcePos sourcePos, @NotNull ImmutableSeq<Expr> levels) implements Expr {
    @Override public <P, R> R doAccept(@NotNull Visitor<P, R> visitor, P p) {
      return visitor.visitLmax(this, p);
    }
  }

  record LitStringExpr(@NotNull SourcePos sourcePos, @NotNull String string) implements Expr {
    @Override public <P, R> R doAccept(@NotNull Visitor<P, R> visitor, P p) {
      return visitor.visitLitString(this, p);
    }
  }

  /**
   * @author kiva
   */
  record BinOpSeq(
    @NotNull SourcePos sourcePos,
    @NotNull ImmutableSeq<NamedArg> seq
  ) implements Expr {
    @Override
    public <P, R> R doAccept(@NotNull Visitor<P, R> visitor, P p) {
      return visitor.visitBinOpSeq(this, p);
    }
  }

  /**
   * @author re-xyr
   */
  record Param(
    @NotNull SourcePos sourcePos,
    @NotNull LocalVar ref,
    @Nullable Expr type,
    boolean pattern,
    boolean explicit
  ) implements ParamLike<Expr> {
    public Param(@NotNull Param param, @Nullable Expr type) {
      this(param.sourcePos, param.ref, type, param.pattern, param.explicit);
    }

    public Param(@NotNull SourcePos sourcePos, @NotNull LocalVar var, boolean explicit) {
      this(sourcePos, var, null, false, explicit);
    }

    public @NotNull Expr.Param mapExpr(@NotNull Function<@NotNull Expr, @Nullable Expr> mapper) {
      return new Param(sourcePos, ref, type != null ? mapper.apply(type) : null, pattern, explicit);
    }
  }
}
