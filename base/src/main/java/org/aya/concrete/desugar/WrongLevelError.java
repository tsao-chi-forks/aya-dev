// Copyright (c) 2020-2021 Yinsen (Tesla) Zhang.
// Use of this source code is governed by the GNU GPLv3 license that can be found in the LICENSE file.
package org.aya.concrete.desugar;

import org.aya.concrete.Expr;
import org.aya.pretty.doc.Doc;
import org.aya.tyck.error.ExprProblem;
import org.jetbrains.annotations.NotNull;

public record WrongLevelError(@NotNull Expr.AppExpr expr, int expected) implements ExprProblem {
  @Override public @NotNull Doc describe() {
    return Doc.hcat(
      Doc.plain("Expected " + expected + " level(s)")
    );
  }

  @Override public @NotNull Severity level() {
    return Severity.ERROR;
  }
}
