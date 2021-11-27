// Copyright (c) 2020-2021 Yinsen (Tesla) Zhang.
// Use of this source code is governed by the MIT license that can be found in the LICENSE.md file.
package org.aya.repr;

import kala.collection.immutable.ImmutableSeq;
import org.aya.tyck.TyckDeclTest;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ShapeMatcherTest {
  @Test
  public void matchNat() {
    match(true, AyaShapes.NAT, "open data Nat | zero | suc Nat");
    match(true, AyaShapes.NAT, "open data Nat | suc Nat | zero");
    match(true, AyaShapes.NAT, "open data Nat | z | s Nat");
    match(true, AyaShapes.NAT_LIKE, "open data Nat | zero | suc Nat");
    match(true, AyaShapes.NAT_LIKE, "open data Nat | suc Nat | zero");
    match(true, AyaShapes.NAT_LIKE, "open data Nat | z | s Nat");

    match(true, AyaShapes.NAT_LIKE, """
    open data Nat | zero | suc Nat
    open data Fin (n : Nat) | suc n => fzero | suc n => fsuc (Fin n)
    """);

    match(ImmutableSeq.of(true, false), AyaShapes.NAT, """
    open data Nat | zero | suc Nat
    open data Fin (n : Nat) | suc n => fzero | suc n => fsuc (Fin n)
    """);

    match(false, AyaShapes.NAT, "open data Nat | s | z");
    match(false, AyaShapes.NAT_LIKE, "open data Nat | s | z");
  }

  @Test
  public void matchList() {
    match(true, AyaShapes.LIST, "open data List (A : Type) : Type | nil | cons A (List A)");
    match(true, AyaShapes.LIST, "open data Stack (A : Type) : Type | empty | push A (Stack A)");
    match(true, AyaShapes.LIST_LIKE, "open data List (A : Type) : Type | nil | cons A (List A)");
    match(true, AyaShapes.LIST_LIKE, "open data Stack (A : Type) : Type | empty | push A (Stack A)");

    match(false, AyaShapes.LIST, "open data ImmSeq (A : Type) : Type | of A | prepended A (ImmSeq A)");
    match(false, AyaShapes.LIST_LIKE, "open data ImmSeq (A : Type) : Type | of A | prepended A (ImmSeq A)");

    match(ImmutableSeq.of(false, true), AyaShapes.LIST_LIKE, """
    open data Nat | zero | suc Nat
    open data Vec (A : Type) (n : Nat) : Type | A, zero => vnil | A, suc n => vcons A (Vec A n)
    """);
  }

  public void match(boolean should, @NotNull CodeShape shape, @Language("TEXT") @NonNls @NotNull String code) {
    var def = TyckDeclTest.successTyckDecls(code);
    def.forEach(d -> assertEquals(should, ShapeMatcher.match(shape, d)));
  }

  public void match(@NotNull ImmutableSeq<Boolean> should, @NotNull CodeShape shape, @Language("TEXT") @NonNls @NotNull String code) {
    var def = TyckDeclTest.successTyckDecls(code);
    def.zipView(should).forEach(tup -> assertEquals(tup._2, ShapeMatcher.match(shape, tup._1)));
  }
}
