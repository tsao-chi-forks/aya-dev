// Copyright (c) 2020-2021 Yinsen (Tesla) Zhang.
// Use of this source code is governed by the MIT license that can be found in the LICENSE.md file.
package org.aya.concrete.parse;

import kala.collection.Seq;
import kala.collection.immutable.ImmutableSeq;
import kala.control.Either;
import org.antlr.v4.runtime.CodePointBuffer;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.aya.api.error.Reporter;
import org.aya.api.error.SourceFileLocator;
import org.aya.concrete.Expr;
import org.aya.concrete.stmt.Stmt;
import org.aya.parser.AyaLexer;
import org.aya.parser.AyaParser;
import org.aya.util.error.SourceFile;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.BiFunction;

public interface AyaParsing {
  @Contract("_ -> new") static @NotNull AyaParser parser(@NotNull String text) {
    return new AyaParser(tokenize(text));
  }

  @Contract("_ -> new") static @NotNull Seq<Token> tokens(@NotNull String text) {
    var tokenStream = tokenize(text);
    tokenStream.fill();
    return Seq.wrapJava(tokenStream.getTokens());
  }

  private static @NotNull CommonTokenStream tokenize(@NotNull String text) {
    return new CommonTokenStream(lexer(text));
  }

  private static @NotNull AyaLexer lexer(@NotNull String text) {
    var intBuffer = IntBuffer.wrap(text.codePoints().toArray());
    var codePointBuffer = CodePointBuffer.withInts(intBuffer);
    var charStream = CodePointCharStream.fromBuffer(codePointBuffer);
    return new AyaLexer(charStream);
  }

  @Contract("_, _ -> new")
  private static @NotNull AyaParser parser(@NotNull SourceFile sourceFile, @NotNull Reporter reporter) {
    var lexer = lexer(sourceFile.sourceCode());
    lexer.removeErrorListeners();
    var listener = new ReporterErrorListener(sourceFile, reporter);
    lexer.addErrorListener(listener);
    var parser = new AyaParser(new CommonTokenStream(lexer));
    parser.removeErrorListeners();
    parser.addErrorListener(listener);
    return parser;
  }

  static @NotNull ImmutableSeq<Stmt> program(
    @NotNull SourceFileLocator locator,
    @NotNull Reporter reporter, @NotNull Path path
  ) throws IOException {
    var sourceFile = new SourceFile(locator.displayName(path).toString(), path, Files.readString(path));
    return program(reporter, sourceFile);
  }

  static @NotNull ImmutableSeq<Stmt> program(@NotNull Reporter reporter, SourceFile sourceFile) {
    return new AyaProducer(sourceFile, reporter).visitProgram(
      parser(sourceFile, reporter).program());
  }

  private static @NotNull <T> T replParser(
    @NotNull Reporter reporter, @NotNull String text,
    @NotNull BiFunction<AyaProducer, AyaParser, T> tree
  ) {
    var sourceFile = new SourceFile("<stdin>", Path.of("stdin"), text);
    var parser = parser(sourceFile, reporter);
    return tree.apply(new AyaProducer(sourceFile, reporter), parser);
  }

  static @NotNull Either<ImmutableSeq<Stmt>, Expr> repl(@NotNull Reporter reporter, @NotNull String text) {
    return replParser(reporter, text, (pro, par) -> pro.visitRepl(par.repl()));
  }

  static @NotNull Expr expr(@NotNull Reporter reporter, @NotNull String text) {
    return replParser(reporter, text, (pro, par) -> pro.visitExpr(par.expr()));
  }
}
