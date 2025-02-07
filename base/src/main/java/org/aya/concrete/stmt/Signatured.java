// Copyright (c) 2020-2021 Yinsen (Tesla) Zhang.
// Use of this source code is governed by the MIT license that can be found in the LICENSE.md file.
package org.aya.concrete.stmt;

import kala.collection.immutable.ImmutableSeq;
import org.aya.api.concrete.ConcreteDecl;
import org.aya.concrete.Expr;
import org.aya.core.def.Def;
import org.aya.tyck.order.TyckUnit;
import org.aya.util.binop.OpDecl;
import org.aya.util.error.SourcePos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An item in the signature, with telescope and result type.
 *
 * @author ice1000
 */
public sealed abstract class Signatured implements ConcreteDecl, OpDecl, TyckUnit permits Decl, Decl.DataCtor, Decl.StructField {
  public final @NotNull SourcePos sourcePos;
  public final @NotNull SourcePos entireSourcePos;
  public final @Nullable OpInfo opInfo;
  public final @NotNull BindBlock bindBlock;

  // will change after resolve
  public @NotNull ImmutableSeq<Expr.Param> telescope;
  public @Nullable Def.Signature signature;

  @Override public @NotNull SourcePos sourcePos() {
    return sourcePos;
  }

  protected Signatured(
    @NotNull SourcePos sourcePos,
    @NotNull SourcePos entireSourcePos,
    @Nullable OpDecl.OpInfo opInfo,
    @NotNull BindBlock bindBlock,
    @NotNull ImmutableSeq<Expr.Param> telescope
  ) {
    this.sourcePos = sourcePos;
    this.entireSourcePos = entireSourcePos;
    this.opInfo = opInfo;
    this.bindBlock = bindBlock;
    this.telescope = telescope;
  }

  @Override public @Nullable OpInfo opInfo() {
    return opInfo;
  }

  @Override public boolean needTyck() {
    return ref().core == null;
  }
}
