// Copyright (c) 2020-2021 Yinsen (Tesla) Zhang.
// Use of this source code is governed by the MIT license that can be found in the LICENSE.md file.
package org.aya.tyck.error;

import org.aya.api.distill.DistillerOptions;
import org.aya.api.error.ExprProblem;
import org.aya.concrete.Expr;
import org.aya.pretty.doc.Doc;
import org.aya.pretty.doc.Style;
import org.jetbrains.annotations.NotNull;

public record LitProblem(@NotNull Expr expr) implements ExprProblem {
  @Override public @NotNull Doc describe(@NotNull DistillerOptions options) {
    return Doc.sep(Doc.english("Unable to convert the literal"),
      Doc.styled(Style.code(), expr.toDoc(options)),
      Doc.english("to a Term, because it's underlying definition is not found."));
  }

  @Override public @NotNull Severity level() {
    return Severity.ERROR;
  }
}
