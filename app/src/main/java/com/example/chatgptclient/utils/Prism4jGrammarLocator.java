package com.example.chatgptclient.utils;

import androidx.annotation.Nullable;

import com.example.chatgptclient.utils.languages.*;

import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

import io.noties.prism4j.GrammarLocator;
import io.noties.prism4j.Prism4j;

public class Prism4jGrammarLocator implements GrammarLocator {

    @Nullable
    @Override
    public Prism4j.Grammar grammar(@NotNull Prism4j prism4j, @NotNull String language) {
        switch (language) {
            case "brainfuck": return Prism_brainfuck.create(prism4j);
            case "c": return Prism_c.create(prism4j);
            case "clike": return Prism_clike.create(prism4j);
            case "clojure": return Prism_clojure.create(prism4j);
            case "cpp": return Prism_cpp.create(prism4j);
            case "csharp": return Prism_csharp.create(prism4j);
            case "css": return Prism_css.create(prism4j);
            case "css_extras": return Prism_css_extras.create(prism4j);
            case "dart": return Prism_dart.create(prism4j);
            case "git": return Prism_git.create(prism4j);
            case "go": return Prism_go.create(prism4j);
            case "groovy": return Prism_groovy.create(prism4j);
            case "java": return Prism_java.create(prism4j);
            case "javascript": return Prism_javascript.create(prism4j);
            case "json": return Prism_json.create(prism4j);
            case "kotlin": return Prism_kotlin.create(prism4j);
            case "latex": return Prism_json.create(prism4j);
            case "makefile": return Prism_makefile.create(prism4j);
            case "markdown": return Prism_markdown.create(prism4j);
            case "markup": return Prism_markup.create(prism4j);
            case "python":return Prism_python.create(prism4j);
            case "scala": return Prism_scala.create(prism4j);
            case "sql": return Prism_sql.create(prism4j);
            case "swift": return Prism_swift.create(prism4j);
            case "yaml": return Prism_yaml.create(prism4j);
            default: return Prism_clike.create(prism4j);
        }
    }

    @NotNull
    @Override
    public Set<String> languages() {
        Set<String> languages = new HashSet<>();
        languages.add("brainfuck");
        languages.add("c");
        languages.add("clike");
        languages.add("clojure");
        languages.add("cpp");
        languages.add("csharp");
        languages.add("css");
        languages.add("css_extras");
        languages.add("dart");
        languages.add("git");
        languages.add("go");
        languages.add("groovy");
        languages.add("java");
        languages.add("javascript");
        languages.add("json");
        languages.add("kotlin");
        languages.add("latex");
        languages.add("makefile");
        languages.add("markdown");
        languages.add("markup");
        languages.add("python");
        languages.add("scala");
        languages.add("sql");
        languages.add("swift");
        languages.add("yaml");
        return languages;
    }
}
