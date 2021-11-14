// Copyright (c) 2020-2021 Yinsen (Tesla) Zhang.
// Use of this source code is governed by the MIT license that can be found in the LICENSE.md file.
package org.aya.concrete.desugar;

import kala.collection.SeqView;
import org.aya.api.ref.DefVar;
import org.aya.api.ref.LocalVar;
import org.aya.concrete.Pattern;
import org.aya.util.binop.Assoc;
import org.aya.util.binop.BinOpParser;
import org.aya.util.binop.BinOpSet;
import org.aya.util.binop.OpDecl;
import org.aya.util.error.SourcePos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public final class BinPatternParser extends BinOpParser<AyaBinOpSet, Pattern, Pattern> {
  public BinPatternParser(@NotNull AyaBinOpSet opSet, @NotNull SeqView<@NotNull Pattern> seq) {
    super(opSet, seq);
  }

  @Override protected @NotNull BinOpParser<AyaBinOpSet, Pattern, Pattern>
  replicate(@NotNull SeqView<@NotNull Pattern> seq) {
    return new BinPatternParser(opSet, seq);
  }

  private static final Pattern OP_APP = new Pattern.Bind(
    SourcePos.NONE, true,
    new LocalVar(BinOpSet.APP_ELEM.name()));

  @Override protected @NotNull Pattern appOp() {
    return OP_APP;
  }

  @Override public @NotNull Pattern
  makeSectionApp(@NotNull SourcePos pos, @NotNull Pattern op, @NotNull Function<Pattern, Pattern> lamBody) {
    // TODO[ice]: pattern does not support operator section
    return createErrorExpr(pos);
  }

  @Override protected void reportAmbiguousPred(String op1, String op2, SourcePos pos) {
    // TODO
  }

  @Override protected void reportFixityError(Assoc top, Assoc current, String op2, String op1, SourcePos pos) {
    // TODO
  }

  @Override protected @NotNull Pattern createErrorExpr(@NotNull SourcePos sourcePos) {
    // TODO
    return null;
  }

  @Override protected @Nullable OpDecl underlyingOpDecl(@NotNull Pattern elem) {
    if (elem.expr() instanceof Pattern.Ctor ref
      && ref.resolved().data() instanceof DefVar<?, ?> defVar
      && defVar.concrete instanceof OpDecl opDecl
    ) return opDecl;
    return null;
  }

  @Override protected int argc(@NotNull OpDecl decl) {
    return BinExprParser.argc0(decl);
  }

  @Override protected @NotNull Pattern
  makeArg(@NotNull SourcePos pos, @NotNull Pattern func, @NotNull Pattern arg, boolean explicit) {
    if (arg instanceof Pattern.Ctor ctor) {
      return new Pattern.Ctor(pos, explicit, ctor.resolved(), ctor.params().appended(arg), ctor.as());
    } else return createErrorExpr(pos);
  }
}
