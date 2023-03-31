package com.example.chatgptclient.utils.languages;

import org.jetbrains.annotations.NotNull;

import io.noties.prism4j.GrammarUtils;
import io.noties.prism4j.Prism4j;
import io.noties.prism4j.annotations.Extend;

import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.regex.Pattern.compile;
import static io.noties.prism4j.Prism4j.pattern;
import static io.noties.prism4j.Prism4j.token;

@SuppressWarnings("unused")
@Extend("java")
public class Prism_scala {

  @NotNull
  public static Prism4j.Grammar create(@NotNull Prism4j prism4j) {
    final Prism4j.Grammar scala = GrammarUtils.extend(
      GrammarUtils.require(prism4j, "java"),
      "scala",
      new GrammarUtils.TokenFilter() {
        @Override
        public boolean test(@NotNull Prism4j.Token token) {
          final String name = token.name();
          return !"class-name".equals(name) && !"function".equals(name);
        }
      },
      token("keyword", pattern(
        compile("<-|=>|\\b(?:abstract|case|catch|class|def|do|else|extends|final|finally|for|forSome|if|implicit|import|lazy|match|new|null|object|override|package|private|protected|return|sealed|self|super|this|throw|trait|try|type|val|var|while|with|yield)\\b")
      )),
      token("string",
        pattern(compile("\"\"\"[\\s\\S]*?\"\"\""), false, true),
        pattern(compile("(\"|')(?:\\\\.|(?!\\1)[^\\\\\\r\\n])*\\1"), false, true)
      ),
      token("number", pattern(
        compile("\\b0x[\\da-f]*\\.?[\\da-f]+|(?:\\b\\d+\\.?\\d*|\\B\\.\\d+)(?:e\\d+)?[dfl]?", CASE_INSENSITIVE)
      ))
    );

    scala.tokens().add(
      token("symbol", pattern(compile("'[^\\d\\s\\\\]\\w*")))
    );

    GrammarUtils.insertBeforeToken(scala, "number",
      token("builtin", pattern(compile("\\b(?:String|Int|Long|Short|Byte|Boolean|Double|Float|Char|Any|AnyRef|AnyVal|Unit|Nothing)\\b")))
    );

    return scala;
  }
}
