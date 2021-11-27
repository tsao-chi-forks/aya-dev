// Copyright (c) 2020-2021 Yinsen (Tesla) Zhang.
// Use of this source code is governed by the MIT license that can be found in the LICENSE.md file.
package org.aya.repr;

public sealed interface TermShape {
  record Any() implements TermShape {}
  record Call(int superLevel) implements TermShape {}
  record TeleRef(int superLevel, int nth) implements TermShape {}
}
