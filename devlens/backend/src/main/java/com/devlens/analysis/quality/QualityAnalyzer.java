package com.devlens.analysis.quality;

import com.devlens.analysis.dto.IssueDto;
import com.devlens.analysis.dto.ReviewAnalysisResponse;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.stmt.*;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Computes software metrics (Cyclomatic Complexity, method length, parameter count,
 * naming compliance) and evaluates Readability, Maintainability, Reliability, and Best Practices.
 */
@Component
public class QualityAnalyzer {

    public ReviewAnalysisResponse analyze(CompilationUnit cu, List<IssueDto> issues) {
        int readability = 95;
        int maintainability = 90;
        int reliability = 95;
        int bestPractices = 90;

        List<ReviewAnalysisResponse.RecommendationDto> recommendations = new ArrayList<>();

        // 1. Evaluate Reliability based on static issues
        int errorCount = 0;
        int warningCount = 0;
        for (IssueDto issue : issues) {
            if ("ERROR".equalsIgnoreCase(issue.getSeverity())) {
                reliability -= 20;
                errorCount++;
                recommendations.add(ReviewAnalysisResponse.RecommendationDto.builder()
                        .priority("HIGH")
                        .line(issue.getLine())
                        .title(issue.getTitle())
                        .description(issue.getExplanation())
                        .build());
            } else if ("WARNING".equalsIgnoreCase(issue.getSeverity())) {
                reliability -= 8;
                warningCount++;
                recommendations.add(ReviewAnalysisResponse.RecommendationDto.builder()
                        .priority("MEDIUM")
                        .line(issue.getLine())
                        .title(issue.getTitle())
                        .description(issue.getExplanation())
                        .build());
            } else {
                bestPractices -= 3;
            }
        }

        // 2. Evaluate Methods: Length, Cyclomatic Complexity, Parameter Count
        List<MethodDeclaration> methods = cu.findAll(MethodDeclaration.class);
        for (MethodDeclaration m : methods) {
            int lineCount = m.getEnd().map(p -> p.line).orElse(1) - m.getBegin().map(p -> p.line).orElse(1) + 1;
            int paramCount = m.getParameters().size();
            int cyclomaticComplexity = computeCyclomaticComplexity(m);

            if (lineCount > 35) {
                maintainability -= 8;
                readability -= 5;
                recommendations.add(ReviewAnalysisResponse.RecommendationDto.builder()
                        .priority("MEDIUM")
                        .line(m.getBegin().map(p -> p.line).orElse(1))
                        .title("Long Method: '" + m.getNameAsString() + "' (" + lineCount + " lines)")
                        .description("Methods exceeding 30 lines often violate the Single Responsibility Principle. Consider extracting helper methods.")
                        .build());
            }

            if (paramCount > 4) {
                maintainability -= 10;
                recommendations.add(ReviewAnalysisResponse.RecommendationDto.builder()
                        .priority("MEDIUM")
                        .line(m.getBegin().map(p -> p.line).orElse(1))
                        .title("Excessive Parameters: '" + m.getNameAsString() + "' (" + paramCount + " params)")
                        .description("Methods taking more than 4 parameters are hard to test and maintain. Consider bundling them in a parameter object.")
                        .build());
            }

            if (cyclomaticComplexity > 8) {
                maintainability -= 12;
                recommendations.add(ReviewAnalysisResponse.RecommendationDto.builder()
                        .priority("HIGH")
                        .line(m.getBegin().map(p -> p.line).orElse(1))
                        .title("High Cyclomatic Complexity in '" + m.getNameAsString() + "' (M = " + cyclomaticComplexity + ")")
                        .description("High branch density makes comprehensive test coverage difficult and increases latent defect probability.")
                        .build());
            }
        }

        // 3. Evaluate Naming Conventions
        List<ClassOrInterfaceDeclaration> classes = cu.findAll(ClassOrInterfaceDeclaration.class);
        for (ClassOrInterfaceDeclaration c : classes) {
            String name = c.getNameAsString();
            if (!Character.isUpperCase(name.charAt(0))) {
                readability -= 10;
                bestPractices -= 8;
                recommendations.add(ReviewAnalysisResponse.RecommendationDto.builder()
                        .priority("LOW")
                        .line(c.getBegin().map(p -> p.line).orElse(1))
                        .title("Class Name Should Be PascalCase: '" + name + "'")
                        .description("Java convention dictates class names begin with an uppercase letter.")
                        .build());
            }
        }

        List<VariableDeclarator> vars = cu.findAll(VariableDeclarator.class);
        for (VariableDeclarator v : vars) {
            String name = v.getNameAsString();
            if (name.length() > 1 && Character.isUpperCase(name.charAt(0)) && !isConstant(name)) {
                readability -= 3;
                recommendations.add(ReviewAnalysisResponse.RecommendationDto.builder()
                        .priority("LOW")
                        .line(v.getBegin().map(p -> p.line).orElse(1))
                        .title("Variable Name Should Be camelCase: '" + name + "'")
                        .description("Variables should start with a lowercase letter according to standard Java conventions.")
                        .build());
            }
        }

        // Clamp scores to [40, 100]
        readability = Math.max(45, Math.min(100, readability));
        maintainability = Math.max(40, Math.min(100, maintainability));
        reliability = Math.max(35, Math.min(100, reliability));
        bestPractices = Math.max(40, Math.min(100, bestPractices));

        // Reliability-Heavy Formula agreed in grill-me interview:
        // Overall = 0.40 * Reliability + 0.25 * Maintainability + 0.20 * BestPractices + 0.15 * Readability
        int overallScore = (int) Math.round(
                0.40 * reliability +
                0.25 * maintainability +
                0.20 * bestPractices +
                0.15 * readability
        );

        Map<String, ReviewAnalysisResponse.QualityCategoryDto> breakdown = new LinkedHashMap<>();
        breakdown.put("readability", ReviewAnalysisResponse.QualityCategoryDto.builder()
                .score(readability)
                .note(readability >= 85 ? "Clean variable naming and concise structure." : "Code formatting and variable naming could be improved.")
                .build());

        breakdown.put("maintainability", ReviewAnalysisResponse.QualityCategoryDto.builder()
                .score(maintainability)
                .note(maintainability >= 80 ? "Modular architecture with low branching density." : "Refactoring recommended to reduce cyclomatic nesting.")
                .build());

        breakdown.put("reliability", ReviewAnalysisResponse.QualityCategoryDto.builder()
                .score(reliability)
                .note(reliability >= 85 ? "Defensive checks and safe memory access patterns." : "High defect potential due to boundary or null dereference risks.")
                .build());

        breakdown.put("bestPractices", ReviewAnalysisResponse.QualityCategoryDto.builder()
                .score(bestPractices)
                .note(bestPractices >= 85 ? "Complies with standard Java platform idioms." : "Minor idiomatic deviations detected.")
                .build());

        // Sort recommendations by priority (HIGH, MEDIUM, LOW)
        recommendations.sort((r1, r2) -> {
            int p1 = priorityWeight(r1.getPriority());
            int p2 = priorityWeight(r2.getPriority());
            return Integer.compare(p2, p1);
        });

        return ReviewAnalysisResponse.builder()
                .overallScore(overallScore)
                .breakdown(breakdown)
                .recommendations(recommendations)
                .build();
    }

    private int computeCyclomaticComplexity(MethodDeclaration method) {
        int complexity = 1;
        complexity += method.findAll(IfStmt.class).size();
        complexity += method.findAll(WhileStmt.class).size();
        complexity += method.findAll(ForStmt.class).size();
        complexity += method.findAll(ForEachStmt.class).size();
        complexity += method.findAll(DoStmt.class).size();
        complexity += method.findAll(SwitchEntry.class).stream().filter(e -> !e.getLabels().isEmpty()).count();
        complexity += method.findAll(CatchClause.class).size();

        // Count logical && and || in binary expressions
        for (BinaryExpr be : method.findAll(BinaryExpr.class)) {
            if (be.getOperator() == BinaryExpr.Operator.AND || be.getOperator() == BinaryExpr.Operator.OR) {
                complexity++;
            }
        }
        return complexity;
    }

    private boolean isConstant(String name) {
        return name.equals(name.toUpperCase());
    }

    private int priorityWeight(String priority) {
        if ("HIGH".equalsIgnoreCase(priority)) return 3;
        if ("MEDIUM".equalsIgnoreCase(priority)) return 2;
        return 1;
    }
}
