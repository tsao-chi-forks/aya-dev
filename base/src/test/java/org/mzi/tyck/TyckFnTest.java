// Copyright (c) 2020-2020 Yinsen (Tesla) Zhang.
// Use of this source code is governed by the Apache-2.0 license that can be found in the LICENSE file.
package org.mzi.tyck;

import org.junit.jupiter.api.Test;
import org.mzi.api.error.SourcePos;
import org.mzi.concrete.Expr;
import org.mzi.concrete.Param;
import org.mzi.core.term.AppTerm;
import org.mzi.core.term.LamTerm;
import org.mzi.core.term.PiTerm;
import org.mzi.core.term.RefTerm;
import org.mzi.generic.Arg;
import org.mzi.ref.LocalVar;
import org.mzi.test.Lisp;
import org.mzi.test.ThrowingReporter;

import static org.glavo.kala.collection.mutable.Buffer.of;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test if the tycker is functioning, say, working for simple cases.
 *
 * @author ice1000
 */
public class TyckFnTest {
  @Test
  public void idLamConnected() {
    var a = new LocalVar("a");
    // \A a.a
    idLamTestCase(new Expr.TelescopicLamExpr(SourcePos.NONE,
      of(
        new Param(SourcePos.NONE, new LocalVar("_"), true),
        new Param(SourcePos.NONE, a, true)),
      new Expr.RefExpr(SourcePos.NONE, a)));
  }

  @Test
  public void idLamDisconnected() {
    var a = new LocalVar("a");
    // \A.\a.a
    idLamTestCase(new Expr.TelescopicLamExpr(SourcePos.NONE,
      of(new Param(SourcePos.NONE, new LocalVar("_"), true)),
      new Expr.TelescopicLamExpr(SourcePos.NONE, of(new Param(SourcePos.NONE, a, true)),
        new Expr.RefExpr(SourcePos.NONE, a))));
  }

  private void idLamTestCase(Expr.TelescopicLamExpr lamAaa) {
    var piUAA = Lisp.reallyParse("(Pi (A (U) ex) (Pi (a A ex) A))");
    var result = lamAaa.accept(new ExprTycker(ThrowingReporter.INSTANCE), piUAA);
    assertNotNull(result);
    if (!(result.wellTyped() instanceof LamTerm lam && result.type() instanceof PiTerm dt)) {
      fail();
      return;
    }
    var lam_aa = AppTerm.make(lam, new Arg<>(new RefTerm(() -> "_"), true));
    assertEquals(lam.body(), lam_aa);
    var newVar = new RefTerm(new LocalVar("xyr"));
    assertEquals(newVar, AppTerm.make(lam_aa, new Arg<>(newVar, true)));
    assertTrue(dt.body() instanceof RefTerm);
  }
}
