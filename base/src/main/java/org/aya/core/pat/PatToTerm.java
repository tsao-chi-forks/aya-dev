// Copyright (c) 2020-2021 Yinsen (Tesla) Zhang.
// Use of this source code is governed by the MIT license that can be found in the LICENSE.md file.
package org.aya.core.pat;

import kala.collection.immutable.ImmutableSeq;
import org.aya.api.ref.LocalVar;
import org.aya.api.util.Arg;
import org.aya.core.term.CallTerm;
import org.aya.core.term.IntroTerm;
import org.aya.core.term.RefTerm;
import org.aya.core.term.Term;
import org.jetbrains.annotations.NotNull;

/**
 * @author ice1000
 * @see org.aya.tyck.pat.Conquer for why not final
 */
public class PatToTerm {
  static final @NotNull PatToTerm INSTANCE = new PatToTerm();

  protected PatToTerm() {
  }

  public Term visit(@NotNull Pat pat) {
    return switch (pat) {
      case Pat.Absurd absurd -> new RefTerm(new LocalVar("()"));
      case Pat.Prim prim -> new CallTerm.Prim(prim.ref(), ImmutableSeq.empty(), ImmutableSeq.empty());
      case Pat.Ctor ctor -> visitCtor(ctor);
      case Pat.Bind bind -> new RefTerm(bind.bind());
      case Pat.Tuple tuple -> new IntroTerm.Tuple(tuple.pats().map(this::visit));
      case Pat.Meta meta -> new RefTerm.MetaPat(meta);
    };
  }

  protected @NotNull Term visitCtor(Pat.@NotNull Ctor ctor) {
    var data = (CallTerm.Data) ctor.type();
    var core = ctor.ref().core;
    var tele = core.selfTele;
    var args = ctor.params().view().zip(tele)
      .map(p -> new Arg<>(visit(p._1), p._2.explicit()))
      .toImmutableSeq();
    return new CallTerm.Con(data.ref(), ctor.ref(), data.args(), data.sortArgs(), args);
  }
}
