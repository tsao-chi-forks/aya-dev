// Copyright (c) 2020-2021 Yinsen (Tesla) Zhang.
// Use of this source code is governed by the MIT license that can be found in the LICENSE.md file.
package org.aya.repr;

import org.jetbrains.annotations.NotNull;

public sealed interface ParamShape {
  record Any() implements ParamShape {}

  record Licit(@NotNull TermShape type, boolean explicit) implements ParamShape {}

  record Optional(@NotNull ParamShape param) implements ParamShape {}

  static @NotNull ParamShape ex(@NotNull TermShape type) {
    return new Licit(type, true);
  }

  static @NotNull ParamShape im(@NotNull TermShape type) {
    return new Licit(type, false);
  }

  static @NotNull ParamShape anyEx() {
    return ex(new TermShape.Any());
  }

  static @NotNull ParamShape anyIm() {
    return im(new TermShape.Any());
  }
}
