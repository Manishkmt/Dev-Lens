package com.devlens.analysis.syntax;

import com.devlens.analysis.dto.IssueDto;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseProblemException;
import com.github.javaparser.ParseResult;
import com.github.javaparser.Problem;
import com.github.javaparser.ast.CompilationUnit;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Parses Java source code into an Abstract Syntax Tree (AST) using JavaParser
 * and generates user-friendly plain-English syntax diagnostic issues.
 */
@Component
public class SyntaxAnalyzer {

    private final JavaParser javaParser = new JavaParser();

    public static class SyntaxAnalysisResult {
        private final CompilationUnit compilationUnit;
        private final List<IssueDto> syntaxErrors;

        public SyntaxAnalysisResult(CompilationUnit compilationUnit, List<IssueDto> syntaxErrors) {
            this.compilationUnit = compilationUnit;
            this.syntaxErrors = syntaxErrors != null ? syntaxErrors : new ArrayList<>();
        }

        public boolean isSuccess() {
            return compilationUnit != null && syntaxErrors.isEmpty();
        }

        public Optional<CompilationUnit> getCompilationUnit() {
            return Optional.ofNullable(compilationUnit);
        }

        public List<IssueDto> getSyntaxErrors() {
            return syntaxErrors;
        }
    }

    public SyntaxAnalysisResult parse(String code) {
        if (code == null || code.trim().isEmpty()) {
            List<IssueDto> errors = List.of(IssueDto.builder()
                    .id("SYNTAX-1")
                    .type("ERROR")
                    .severity("ERROR")
                    .severityClass("badge-danger")
                    .title("Empty Source Code")
                    .line(1)
                    .endLine(1)
                    .codeSnippet("")
                    .explanation("The editor contains no source code to analyze.")
                    .whyItHappens("Static code analysis requires valid Java class definitions.")
                    .suggestedFix("Provide a valid Java class with methods.")
                    .build());
            return new SyntaxAnalysisResult(null, errors);
        }

        // Check if class declaration is present (strict parsing decision)
        if (!hasValidClassDeclaration(code)) {
            List<IssueDto> errors = List.of(IssueDto.builder()
                    .id("SYNTAX-CLASS-REQUIRED")
                    .type("ERROR")
                    .severity("ERROR")
                    .severityClass("badge-danger")
                    .title("Enclosing Class Required")
                    .line(1)
                    .endLine(1)
                    .codeSnippet(extractFirstSnippet(code))
                    .explanation("Java source files require an enclosing class declaration (e.g., 'public class Solution { ... }'). Bare methods and statements are not standalone compilable units.")
                    .whyItHappens("Java is an object-oriented language where all executable logic must reside inside a class, record, or interface.")
                    .suggestedFix("Wrap your method in a class: 'public class Solution { ... }'")
                    .fixCode("public class Solution {\n" + code + "\n}")
                    .build());
            return new SyntaxAnalysisResult(null, errors);
        }

        ParseResult<CompilationUnit> parseResult = javaParser.parse(code);

        if (!parseResult.isSuccessful() || parseResult.getResult().isEmpty()) {
            List<IssueDto> errors = new ArrayList<>();
            int idCounter = 1;
            for (Problem problem : parseResult.getProblems()) {
                int line = 1;
                int endLine = 1;
                if (problem.getLocation().isPresent() && problem.getLocation().get().getBegin().getRange().isPresent()) {
                    line = problem.getLocation().get().getBegin().getRange().get().begin.line;
                    endLine = problem.getLocation().get().getBegin().getRange().get().end.line;
                }

                String cleanMessage = humanizeSyntaxError(problem.getMessage());

                errors.add(IssueDto.builder()
                        .id("SYNTAX-" + (idCounter++))
                        .type("ERROR")
                        .severity("ERROR")
                        .severityClass("badge-danger")
                        .title("Syntax Error")
                        .line(line)
                        .endLine(endLine)
                        .codeSnippet(extractLineSnippet(code, line))
                        .explanation(cleanMessage)
                        .whyItHappens("The code violates Java grammar rules and cannot be converted into an Abstract Syntax Tree.")
                        .suggestedFix(suggestSyntaxFix(cleanMessage))
                        .build());
            }

            if (errors.isEmpty()) {
                errors.add(IssueDto.builder()
                        .id("SYNTAX-1")
                        .type("ERROR")
                        .severity("ERROR")
                        .severityClass("badge-danger")
                        .title("Parsing Failure")
                        .line(1)
                        .endLine(1)
                        .explanation("The Java parser encountered an unrecoverable syntax issue.")
                        .whyItHappens("Syntax error in class structure.")
                        .suggestedFix("Verify balanced braces, parentheses, and semicolons.")
                        .build());
            }

            return new SyntaxAnalysisResult(null, errors);
        }

        CompilationUnit cu = parseResult.getResult().get();
        return new SyntaxAnalysisResult(cu, new ArrayList<>());
    }

    private boolean hasValidClassDeclaration(String code) {
        String stripped = code.replaceAll("//.*", "").replaceAll("/\\*.*?\\*/", "");
        return stripped.contains("class ") || stripped.contains("interface ") || stripped.contains("enum ") || stripped.contains("record ");
    }

    private String humanizeSyntaxError(String raw) {
        if (raw == null) return "Syntax error encountered";
        if (raw.contains("';' expected") || raw.contains("insert \";\"")) {
            return "Missing semicolon ';' at the end of statement.";
        }
        if (raw.contains("')' expected") || raw.contains("insert \")\"")) {
            return "Mismatched or missing closing parenthesis ')'.";
        }
        if (raw.contains("'}' expected") || raw.contains("insert \"}\"")) {
            return "Missing closing curly bracket '}' for code block.";
        }
        if (raw.contains("Lexical error")) {
            return "Invalid character or token in Java source.";
        }
        return raw.split("\n")[0];
    }

    private String suggestSyntaxFix(String message) {
        if (message.contains("semicolon")) return "Add a semicolon ';' to complete the statement.";
        if (message.contains("parenthesis")) return "Close the open parenthesis with ')'.";
        if (message.contains("curly bracket")) return "Close the block with '}'.";
        return "Check syntax around the specified line.";
    }

    private String extractLineSnippet(String code, int line) {
        String[] lines = code.split("\r?\n");
        if (line >= 1 && line <= lines.length) {
            return lines[line - 1].trim();
        }
        return "";
    }

    private String extractFirstSnippet(String code) {
        String[] lines = code.split("\r?\n");
        for (String l : lines) {
            if (!l.trim().isEmpty()) {
                return l.trim();
            }
        }
        return "";
    }
}
