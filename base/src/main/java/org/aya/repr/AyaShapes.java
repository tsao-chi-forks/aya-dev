// Copyright (c) 2020-2021 Yinsen (Tesla) Zhang.
// Use of this source code is governed by the MIT license that can be found in the LICENSE.md file.
package org.aya.repr;

import kala.collection.immutable.ImmutableSeq;
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
}
