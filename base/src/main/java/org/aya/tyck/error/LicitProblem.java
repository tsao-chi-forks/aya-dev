// Copyright (c) 2020-2021 Yinsen (Tesla) Zhang.
// Use of this source code is governed by the MIT license that can be found in the LICENSE.md file.
package org.aya.tyck.error;

import org.aya.api.distill.DistillerOptions;
import org.aya.api.error.ExprProblem;
import org.aya.api.error.Problem;
import org.aya.concrete.Expr;
import org.aya.core.term.Term;
import org.aya.pretty.doc.Doc;
import org.aya.pretty.doc.Style;
import org.aya.util.error.SourcePos;
import org.jetbrains.annotations.NotNull;

public sealed interface LicitProblem extends Problem {
  @Override default @NotNull Severity level() {
    return Severity.ERROR;
  }

  record LicitMismatchError(@Override @NotNull Expr expr, @NotNull Term type) implements LicitProblem, ExprProblem {
    @Override public @NotNull Doc describe(@NotNull DistillerOptions options) {
      return Doc.vcat(
        Doc.english("Cannot check"),
        Doc.par(1, expr.toDoc(options)),
        Doc.english("against the Pi type"),
        Doc.par(1, type.toDoc(options)),
        Doc.english("because explicitness do not match"));
    }
  }

  record UnexpectedImplicitArgError(@Override @NotNull Expr.NamedArg expr) implements LicitProblem {
    @Override public @NotNull SourcePos sourcePos() {
      return expr.expr().sourcePos();
    }

    @Override public @NotNull Doc describe(@NotNull DistillerOptions options) {
      return Doc.sep(Doc.english("Unexpected implicit argument"),
        Doc.styled(Style.code(), expr.toDoc(options)));
    }
  }
}
