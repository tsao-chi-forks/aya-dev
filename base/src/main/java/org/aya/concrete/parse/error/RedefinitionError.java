// Copyright (c) 2020-2021 Yinsen (Tesla) Zhang.
// Use of this source code is governed by the MIT license that can be found in the LICENSE.md file.
package org.aya.concrete.parse.error;

import org.aya.api.distill.DistillerOptions;
import org.aya.api.error.Problem;
import org.aya.pretty.doc.Doc;
import org.aya.pretty.doc.Style;
import org.aya.util.error.SourcePos;
import org.jetbrains.annotations.NotNull;

public record RedefinitionError(
  @NotNull Kind kind,
  @NotNull String name,
  @Override @NotNull SourcePos sourcePos
) implements Problem {
  @Override public @NotNull Doc describe(@NotNull DistillerOptions options) {
    return Doc.sep(Doc.plain("Redefinition of"), Doc.plain(kind.prettyName),
      Doc.styled(Style.code(), Doc.plain(name)));
  }

  @Override public @NotNull Severity level() {
    return Severity.ERROR;
  }

  @Override public @NotNull Stage stage() {
    return Stage.PARSE;
  }

  public enum Kind {
    Prim("primitive"),
    Ctor("constructor"),
    Field("field");

    final @NotNull String prettyName;

    Kind(@NotNull String name) {
      prettyName = name;
    }
  }
}
