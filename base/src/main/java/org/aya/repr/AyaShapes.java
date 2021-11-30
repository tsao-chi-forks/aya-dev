// Copyright (c) 2020-2021 Yinsen (Tesla) Zhang.
// Use of this source code is governed by the MIT license that can be found in the LICENSE.md file.
package org.aya.repr;

import kala.collection.immutable.ImmutableSeq;
import org.aya.api.util.Arg;
import org.aya.core.def.CtorDef;
import org.aya.core.term.CallTerm;
import org.aya.core.term.Term;
import org.aya.tyck.ExprTycker;
import org.jetbrains.annotations.NotNull;

import static org.aya.repr.CodeShape.CtorShape;
import static org.aya.repr.CodeShape.DataShape;

public interface AyaShapes {
  @NotNull CodeShape NAT = new DataShape(ImmutableSeq.empty(), ImmutableSeq.of(
    new CtorShape(ImmutableSeq.empty()),
    new CtorShape(ImmutableSeq.of(ParamShape.ex(new TermShape.Call(0))))
  ));

  @NotNull CodeShape NAT_LIKE = new DataShape(ImmutableSeq.of(new ParamShape.Optional(ParamShape.anyEx())), ImmutableSeq.of(
    new CtorShape(ImmutableSeq.empty()),
    new CtorShape(ImmutableSeq.of(ParamShape.ex(new TermShape.Call(0))))
  ));

  @NotNull CodeShape LIST = new DataShape(ImmutableSeq.of(ParamShape.anyEx()), ImmutableSeq.of(
    new CtorShape(ImmutableSeq.empty()),
    new CtorShape(ImmutableSeq.of(
      ParamShape.ex(new TermShape.TeleRef(0, 0)),
      ParamShape.ex(new TermShape.Call(0))
    ))
  ));

  @NotNull CodeShape LIST_LIKE = new DataShape(ImmutableSeq.of(ParamShape.anyEx(), new ParamShape.Optional(ParamShape.anyEx())), ImmutableSeq.of(
    new CtorShape(ImmutableSeq.empty()),
    new CtorShape(ImmutableSeq.of(
      ParamShape.ex(new TermShape.TeleRef(0, 0)),
      ParamShape.ex(new TermShape.Call(0))
    ))
  ));

  static @NotNull Term toAyaNat(@NotNull CallTerm.Data nat, int n) {
    var body = nat.ref().core.body;
    var zero = body.find(it -> it.selfTele.sizeEquals(0));
    var suc = body.find(it -> it.selfTele.sizeEquals(1));
    if (zero.isEmpty() || suc.isEmpty()) throw new ExprTycker.TyckerException();
    return toAyaNat(n, nat, zero.get(), suc.get());
  }

  private static @NotNull Term toAyaNat(int n, @NotNull CallTerm.Data data, @NotNull CtorDef zero, @NotNull CtorDef suc) {
    if (n == 0) return new CallTerm.Con(data.conHead(zero.ref), ImmutableSeq.empty());
    return new CallTerm.Con(data.conHead(suc.ref), ImmutableSeq.of(new Arg<>(toAyaNat(n - 1, data, zero, suc), true)));
  }
}
