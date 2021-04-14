// Copyright (c) 2020-2021 Yinsen (Tesla) Zhang.
// Use of this source code is governed by the GNU GPLv3 license that can be found in the LICENSE file.
package org.aya.tyck.error;

import org.aya.concrete.Expr;
import org.aya.core.Meta;
import org.aya.pretty.doc.Doc;
import org.aya.pretty.doc.Docile;
import org.jetbrains.annotations.NotNull;

public record Goal(
  @NotNull Expr.HoleExpr expr,
  @NotNull Meta meta
) implements ExprProblem {
  @Override public @NotNull Doc describe() {
    var doc = Doc.vcat(
      Doc.hcat(Doc.plain("Expected type: "), meta.result.toDoc()),
      Doc.plain("Context:"),
      Doc.vcat(meta.fullTelescope().map(Docile::toDoc))
    );
    return meta.body == null ? doc :
      Doc.vcat(Doc.hcat(Doc.plain("Candidate: "), meta.body.toDoc()), doc);
  }

  @Override public @NotNull Severity level() {
    return Severity.GOAL;
  }
}
