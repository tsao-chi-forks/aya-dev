// Copyright (c) 2020-2021 Yinsen (Tesla) Zhang.
// Use of this source code is governed by the MIT license that can be found in the LICENSE.md file.
package org.aya.concrete;

import kala.collection.immutable.ImmutableSeq;
import kala.control.Either;
import kala.tuple.Tuple2;
import kala.value.Ref;
import org.aya.api.distill.DistillerOptions;
import org.aya.concrete.parse.AyaParsing;
import org.aya.concrete.parse.AyaProducer;
import org.aya.concrete.stmt.Command;
import org.aya.concrete.stmt.Decl;
import org.aya.concrete.stmt.Stmt;
import org.aya.pretty.doc.Doc;
import org.aya.test.ThrowingReporter;
import org.aya.util.error.Global;
import org.aya.util.error.SourceFile;
import org.aya.util.error.SourcePos;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ParseTest {
  public static final @NotNull AyaProducer INSTANCE = new AyaProducer(SourceFile.NONE,
    ThrowingReporter.INSTANCE);

  @BeforeAll public static void enableTest() {
    Global.NO_RANDOM_NAME = true;
    Global.UNITE_SOURCE_POS = true;
  }

  @AfterAll public static void exit() {
    Global.reset();
  }

  private static @NotNull Expr parseExpr(@NotNull @NonNls @Language("TEXT") String code) {
    return INSTANCE.visitExpr(AyaParsing.parser(code).expr());
  }

  public static @NotNull ImmutableSeq<Stmt> parseStmt(@NotNull @NonNls @Language("TEXT") String code) {
    return INSTANCE.visitStmt(AyaParsing.parser(code).stmt()).toImmutableSeq();
  }

  public static @NotNull ImmutableSeq<Stmt> parseManyStmt(@NotNull @NonNls @Language("TEXT") String code) {
    return INSTANCE.visitProgram(AyaParsing.parser(code).program());
  }

  public static @NotNull Tuple2<Decl, ImmutableSeq<Stmt>> parseDecl(@NotNull @NonNls @Language("TEXT") String code) {
    return INSTANCE.visitDecl(AyaParsing.parser(code).decl());
  }

  @Test
  public void issue141() {
    Assertions.assertEquals(parseStmt("module a {}"),
      ImmutableSeq.of(new Command.Module(SourcePos.NONE, "a", ImmutableSeq.empty())));
  }

  @Test public void successCmd() {
    parseOpen("open A");
    parseOpen("open A::B");
    parseOpen("open A using ()");
    parseOpen("open A hiding ()");
    parseImport("import A");
    parseImport("import A::B");
    parseImport("import A::B using ()");
    parseAndPretty("open Boy::Next::Door using (door) (next)", """
        open Boy::Next::Door using (door, next)
      """);
  }

  @Test public void successLiteral() {
    assertTrue(parseExpr("diavolo") instanceof Expr.UnresolvedExpr);
    assertTrue(parseExpr("Type") instanceof Expr.RawUnivExpr);
  }

  @Test public void successDecl() {
    parseFn("def a => 1");
    parseFn("def a (b : X) => b");
    parseFn("def a (f : Pi a b c d -> a) => b");
    parseFn("def a (t : Sig a b ** s) => b");
    parseFn("""
      def uncurry (A : Type) (B : Type) (C : Type)
                   (f : Pi A B -> C)
                   (p : Sig A ** B) : C
        => f (p.1) (p.2)""");
    parseData("data Unit");
    parseData("data Unit abusing {}");
    parseData("data Unit : A abusing {}");
    parseData("data T {A : Type} : A abusing {}");
    parseAndPretty("def id {A : Type} (a : A) : A => a", """
        def id {A : Type} (a : A) : A => a
      """);
    parseAndPretty("def xx {A B : Type} (a : A) : A => a", """
        def xx {A B : Type} (a : A) : A => a
      """);
  }

  @Test public void nat() {
    parseAndPretty("data Nat | Z | S Nat", """
      data Nat
        | Z
        | S (_ : Nat)
      """);
  }

  @Test public void successExpr() {
    assertTrue(parseExpr("boy") instanceof Expr.UnresolvedExpr);
    assertTrue(parseExpr("f a") instanceof Expr.BinOpSeq);
    assertTrue(parseExpr("f a b c") instanceof Expr.BinOpSeq);
    assertTrue(parseExpr("a.1") instanceof Expr.ProjExpr);
    assertTrue(parseExpr("a.1.2") instanceof Expr.ProjExpr);
    assertTrue(parseExpr("f (a.1) (a.2)") instanceof Expr.BinOpSeq app
      && app.seq().get(1).expr() instanceof Expr.BinOpSeq proj1
      && proj1.seq().sizeEquals(1)
      && proj1.seq().get(0).expr() instanceof Expr.ProjExpr
      && app.seq().get(2).expr() instanceof Expr.BinOpSeq proj2
      && proj2.seq().sizeEquals(1)
      && proj2.seq().get(0).expr() instanceof Expr.ProjExpr);
    assertTrue(parseExpr("f a.1") instanceof Expr.BinOpSeq app
      && app.seq().get(1).expr() instanceof Expr.ProjExpr);
    assertTrue(parseExpr("(f a).1") instanceof Expr.ProjExpr proj
      && proj.tup() instanceof Expr.BinOpSeq);
    assertTrue(parseExpr("λ a => a") instanceof Expr.LamExpr);
    assertTrue(parseExpr("\\ a => a") instanceof Expr.LamExpr);
    assertTrue(parseExpr("\\ a b => a") instanceof Expr.LamExpr);
    assertTrue(parseExpr("Π a -> a") instanceof Expr.PiExpr dt && !dt.co());
    assertTrue(parseExpr("Pi a -> a") instanceof Expr.PiExpr dt && !dt.co());
    assertTrue(parseExpr("Pi a b -> a") instanceof Expr.PiExpr dt
      && !dt.co() && dt.last() instanceof Expr.PiExpr);
    assertTrue(parseExpr("Σ a ** b") instanceof Expr.SigmaExpr dt && !dt.co());
    assertTrue(parseExpr("Sig a ** b") instanceof Expr.SigmaExpr dt && !dt.co());
    assertTrue(parseExpr("Sig a b ** c") instanceof Expr.SigmaExpr dt && !dt.co());
    assertTrue(parseExpr("Pi (x : Sig a ** b) -> c") instanceof Expr.PiExpr dt && !dt.co() && dt.param().type() instanceof Expr.SigmaExpr);
    parseTo("(f a) . 1", new Expr.ProjExpr(
      SourcePos.NONE,
      new Expr.BinOpSeq(
        SourcePos.NONE,
        ImmutableSeq.of(
          new Expr.NamedArg(true, new Expr.BinOpSeq(
            SourcePos.NONE,
            ImmutableSeq.of(
              new Expr.NamedArg(true, new Expr.UnresolvedExpr(SourcePos.NONE, "f")),
              new Expr.NamedArg(true, new Expr.UnresolvedExpr(SourcePos.NONE, "a"))
            ))))),
      Either.left(1),
      new Ref<>(null)
    ));
    parseTo("f a . 1", new Expr.BinOpSeq(
        SourcePos.NONE,
        ImmutableSeq.of(
          new Expr.NamedArg(true, new Expr.UnresolvedExpr(SourcePos.NONE, "f")),
          new Expr.NamedArg(true,
            new Expr.ProjExpr(SourcePos.NONE, new Expr.UnresolvedExpr(SourcePos.NONE, "a"),
              Either.left(1), new Ref<>(null))))
      )
    );
    assertTrue(parseExpr("f (a, b, c)") instanceof Expr.BinOpSeq app
      && app.seq().sizeEquals(2)
      && !app.toDoc(DistillerOptions.debug()).debugRender().isEmpty()
      && app.seq().get(1).expr() instanceof Expr.TupExpr tup
      && tup.items().sizeEquals(3));
    assertTrue(parseExpr("new Pair A B { | fst => a | snd => b }") instanceof Expr.NewExpr neo
      && !neo.toDoc(DistillerOptions.debug()).debugRender().isEmpty());
  }

  private void parseImport(@Language("TEXT") String code) {
    assertTrue(parseStmt(code).first() instanceof Command.Import s && !s.toDoc(DistillerOptions.debug()).debugRender().isEmpty());
  }

  private void parseOpen(@Language("TEXT") String code) {
    assertTrue(parseStmt(code).last() instanceof Command.Open s && !s.toDoc(DistillerOptions.debug()).debugRender().isEmpty());
  }

  private void parseFn(@Language("TEXT") String code) {
    assertTrue(parseDecl(code)._1 instanceof Decl.FnDecl s && !s.toDoc(DistillerOptions.debug()).debugRender().isEmpty());
  }

  private void parseData(@Language("TEXT") String code) {
    assertTrue(parseDecl(code)._1 instanceof Decl.DataDecl);
  }

  @Test
  public void patternParseImplicit() {
    parseAndPretty(
      "def simple | a => a",
      "def simple\n  | a => a"
    );
    parseAndPretty(
      "def unary-tuples-are-ignored | (a) => a",
      "def unary-tuples-are-ignored\n  | a => a"
    );
    parseAndPretty(
      "def im-unary-tuples-are-ignored | {a} => a",
      "def im-unary-tuples-are-ignored\n  | {a} => a"
    );
    parseAndPretty(
      "def we-dont-have-unary-tuples | ((((((((a)))))))) => (a)",
      "def we-dont-have-unary-tuples\n  | a => a"
    );
    parseAndPretty(
      "def we-dont-have-unary-tuples | {{{{{{{{{{a}}}}}}}}}} => a",
      "def we-dont-have-unary-tuples\n  | {a} => a"
    );
    parseAndPretty(
      "def we-dont-have-unary-tuples | ((((((((a)))), b)))) => (a)",
      "def we-dont-have-unary-tuples\n  | (a, b) => a"
    );
    parseAndPretty(
      "def tuples | (a,b,c) => a",
      "def tuples\n  | (a, b, c) => a"
    );
    parseAndPretty(
      "def im-tuples | {a,b,c} => a",
      "def im-tuples\n  | {a, b, c} => a"
    );
    parseAndPretty(
      "def tuples-with-im | (a,{b},c,d,{ef}) => ef",
      "def tuples-with-im\n  | (a, {b}, c, d, {ef}) => ef"
    );
    parseAndPretty(
      "def imtuple-with-extuple | {a, (b, c, d)} => ef",
      "def imtuple-with-extuple\n  | {a, (b, c, d)} => ef"
    );
    parseAndPretty(
      "def im-in-ctor | suc {N} (a) => N",
      "def im-in-ctor\n  | suc {N} a => N"
    );
    parseAndPretty(
      "def im-in-ctor-nested | suc {N} (suc {M} a) => a",
      "def im-in-ctor-nested\n  | suc {N} (suc {M} a) => a"
    );
    parseAndPretty(
      "def final : Nat | (suc {m} {suc x} a, fuck, {114514}) as Outer => a",
      "def final : Nat\n  | (suc {m} {suc x} a, fuck, {114514}) as Outer => a"
    );
    parseAndPretty(
      "struct Very-Simple (A : Type) : Type | x : A | y : Nat",
      """
        struct Very-Simple (A : Type) : Type
          | x : A
          | y : Nat
        """
    );
    parseAndPretty(
      """
        struct With-Tele (B : Nat -> Type) : Type
          | x { X : Type } : Nat
          | y : B zero
        """,
      """
        struct With-Tele (B : Nat -> Type) : Type
          | x {X : Type} : Nat
          | y : B zero
        """
    );
  }

  @Test public void parseStructs() {
    parseAndPretty(
      """
        struct Path (A : I -> Type) (a : A left) (b : A right) : Type
         | at (i : I) : A i {
           | left => a
           | right => b
         }
        """,
      """
        struct Path (A : I -> Type) (a : A left) (b : A right) : Type
          | at (i : I) : A i {
            | left => a
            | right => b
          }
        """
    );
    parseAndPretty(
      "struct Very-Simple (A : Type) : Type | x : A => zero",
      """
        struct Very-Simple (A : Type) : Type
          | x : A => zero
        """
    );
  }

  @Test public void modules() {
    parseAndPretty("""
      module Nat {
       open data ℕ : Type | zero | suc ℕ
      }
      """, """
      module Nat {
        data ℕ : Type
          | zero
          | suc (_ : ℕ)
        open ℕ hiding ()
      }""");
  }

  @Test public void globalStmt() {
    parseAndPretty("universe uu", "universe uu");
  }

  @Test public void patterns() {
    parseAndPretty("def inline final : Nat => a",
      "def inline final : Nat => a");
    parseAndPretty("def opaque final : Nat | impossible",
      "def opaque final : Nat\n  | impossible");
  }

  @Test public void exprAndCounterexamples() {
    parseAndPretty("example def test => Type (lsuc lzero) (lmax lzero)",
      "example def test => Type (lsuc lzero) (lmax lzero)");
    parseAndPretty("counterexample def test => {? Type ?}",
      "counterexample def test => {? Type ?}");
  }

  @Test public void issue350() {
    parseAndPretty("""
        def l : Type => \\ i => Nat
        """,
      """
        def l : Type => \\ (i : _) => Nat
        """);
    parseAndPretty("""
        def l : Type => \\ (i : I) => Nat
        """,
      """
        def l : Type => \\ (i : I) => Nat
        """);
  }

  private void parseAndPretty(@NotNull @NonNls @Language("TEXT") String code, @NotNull @NonNls @Language("TEXT") String pretty) {
    var stmt = parseStmt(code);
    assertEquals(pretty.trim(), Doc.vcat(stmt.view()
        .map(s -> s.toDoc(DistillerOptions.debug())))
      .debugRender()
      .trim());
  }

  private void parseTo(@NotNull @NonNls @Language("TEXT") String code, Expr expr) {
    Assertions.assertEquals(expr, parseExpr(code));
  }
}
