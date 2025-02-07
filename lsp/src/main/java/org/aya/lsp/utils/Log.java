// Copyright (c) 2020-2021 Yinsen (Tesla) Zhang.
// Use of this source code is governed by the MIT license that can be found in the LICENSE.md file.
package org.aya.lsp.utils;

import org.aya.lsp.server.AyaLanguageClient;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class Log {
  private static final @NotNull Path LOG_FILE = Paths.get("aya-last-startup.log").toAbsolutePath();
  private static @Nullable AyaLanguageClient CLIENT = null;

  public static void init(@NotNull AyaLanguageClient client) {
    if (CLIENT == null) synchronized (Log.class) {
      if (CLIENT == null) CLIENT = client;
        // if the code was right, this should never happen
      else throw new IllegalStateException("double initialization occurred");
    }
    i("Log file: %s", LOG_FILE);
  }

  public static void publishProblems(PublishDiagnosticsParams params) {
    if (CLIENT != null) CLIENT.publishDiagnostics(params);
  }

  public static void i(@NotNull String fmt, Object... args) {
    log(MessageType.Info, fmt, args);
  }

  public static void e(@NotNull String fmt, Object... args) {
    log(MessageType.Error, fmt, args);
  }

  public static void w(@NotNull String fmt, Object... args) {
    log(MessageType.Warning, fmt, args);
  }

  public static void d(@NotNull String fmt, Object... args) {
    log(MessageType.Log, fmt, args);
  }

  public static void log(@NotNull MessageType type, @NotNull String fmt, Object... args) {
    var format = fmt.formatted(args);
    logConsole(type, format);
    if (CLIENT != null) CLIENT.logMessage(new MessageParams(type, format));
  }

  public static void logConsole(@NotNull MessageType type, @NotNull String content) {
    try {
      var format = String.format("[%s]: %s%n", type, content);
      System.err.print(format);
      Files.writeString(LOG_FILE, format,
        StandardCharsets.UTF_8,
        StandardOpenOption.APPEND);
    } catch (IOException ignored) {
    }
  }
}
