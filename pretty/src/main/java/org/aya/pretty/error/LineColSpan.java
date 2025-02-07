// Copyright (c) 2020-2021 Yinsen (Tesla) Zhang.
// Use of this source code is governed by the MIT license that can be found in the LICENSE.md file.
package org.aya.pretty.error;

import org.jetbrains.annotations.NotNull;

public record LineColSpan(
  @NotNull String input,
  int startLine,
  int startCol,
  int endLine,
  int endCol) implements Span {
  @Override
  public @NotNull Span.Data normalize(PrettyErrorConfig config) {
    return new Data(startLine, startCol, endLine, endCol);
  }
}
