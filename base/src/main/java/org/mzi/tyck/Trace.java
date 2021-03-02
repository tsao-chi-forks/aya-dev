// Copyright (c) 2020-2021 Yinsen (Tesla) Zhang.
// Use of this source code is governed by the GNU GPLv3 license that can be found in the LICENSE file.
package org.mzi.tyck;

import org.glavo.kala.collection.mutable.Buffer;
import org.jetbrains.annotations.NotNull;
import org.mzi.concrete.Expr;
import org.mzi.core.term.Term;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

/**
 * @author ice1000
 */
public sealed interface Trace {
  @NotNull Buffer<@NotNull Trace> subtraces();

  interface Visitor<P, R> {
    R visitExpr(@NotNull ExprT t, P p);
    R visitUnify(@NotNull UnifyT t, P p);
  }

  <P, R> R accept(@NotNull Visitor<P, R> visitor, P p);

  class Builder {
    public Deque<@NotNull Buffer<@NotNull Trace>> tops = new ArrayDeque<>();

    public void shift(@NotNull Trace trace) {
      Objects.requireNonNull(tops.peekLast()).append(trace);
      tops.addLast(trace.subtraces());
    }

    public void reduce() {
      tops.removeLast();
    }
  }

  record ExprT(@NotNull Expr expr, @NotNull Term term, @NotNull Buffer<@NotNull Trace> subtraces) implements Trace {
    public ExprT(@NotNull Expr expr, @NotNull Term term) {
      this(expr, term, Buffer.of());
    }

    @Override public <P, R> R accept(@NotNull Visitor<P, R> visitor, P p) {
      return visitor.visitExpr(this, p);
    }
  }

  record UnifyT(@NotNull Term lhs, @NotNull Term rhs, @NotNull Buffer<@NotNull Trace> subtraces) implements Trace {
    public UnifyT(@NotNull Term lhs, @NotNull Term rhs) {
      this(lhs, rhs, Buffer.of());
    }

    @Override public <P, R> R accept(@NotNull Visitor<P, R> visitor, P p) {
      return visitor.visitUnify(this, p);
    }
  }
}
