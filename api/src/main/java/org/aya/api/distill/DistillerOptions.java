// Copyright (c) 2020-2021 Yinsen (Tesla) Zhang.
// Use of this source code is governed by the MIT license that can be found in the LICENSE.md file.
package org.aya.api.distill;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.Map;

/**
 * @author ice1000
 */
public final class DistillerOptions {
  public final @NotNull Map<Key, Boolean> map = new EnumMap<>(Key.class);

  {
    reset();
  }

  public void reset() {
    for (Key value : Key.values()) map.put(value, false);
    map.put(Key.InlineMetas, true);
  }

  public enum Key {
    InlineMetas,
    ShowImplicitArgs,
    ShowImplicitPats,
    ShowLambdaTypes,
    ShowLevels,
  }

  @Contract(pure = true, value = "->new") public static @NotNull DistillerOptions debug() {
    var map = informative();
    map.map.put(Key.ShowLambdaTypes, true);
    map.map.put(Key.ShowLevels, true);
    return map;
  }

  @Contract(pure = true, value = "->new") public static @NotNull DistillerOptions informative() {
    var map = pretty();
    map.map.put(Key.ShowImplicitArgs, true);
    return map;
  }

  @Contract(pure = true, value = "->new") public static @NotNull DistillerOptions pretty() {
    var map = new DistillerOptions();
    map.map.put(Key.ShowImplicitPats, true);
    return map;
  }
}
