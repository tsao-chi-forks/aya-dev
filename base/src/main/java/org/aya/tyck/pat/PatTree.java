// Copyright (c) 2020-2021 Yinsen (Tesla) Zhang.
// Use of this source code is governed by the MIT license that can be found in the LICENSE.md file.
package org.aya.tyck.pat;

import kala.collection.immutable.ImmutableSeq;
import kala.collection.mutable.DynamicSeq;
import org.aya.api.ref.LocalVar;
import org.aya.concrete.Pattern;
import org.aya.util.TreeBuilder;
import org.aya.util.error.SourcePos;
import org.aya.util.error.WithPos;
import org.jetbrains.annotations.NotNull;

/**
 * @author ice1000, kiva
 */
public record PatTree(
  @NotNull String s,
  boolean explicit, int argsCount,
  @NotNull DynamicSeq<PatTree> children
) implements TreeBuilder.Tree<PatTree> {
  public PatTree(@NotNull String s, boolean explicit, int argsCount) {
    this(s, explicit, argsCount, DynamicSeq.create());
  }

  public @NotNull Pattern toPattern() {
    var childPatterns = children.isEmpty()
      ? ImmutableSeq.<Pattern>fill(argsCount, new Pattern.Bind(SourcePos.NONE, true, new LocalVar("_")))
      : children.view().map(PatTree::toPattern).toImmutableSeq();
    return new Pattern.Ctor(SourcePos.NONE, explicit, new WithPos<>(SourcePos.NONE, new LocalVar(s)), childPatterns, null);
  }

  public final static class Builder extends TreeBuilder<PatTree> {
    public void shiftEmpty(boolean explicit) {
      append(new PatTree("_", explicit, 0));
    }
  }
}
