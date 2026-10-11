package com.devlens.analysis.complexity;

import com.devlens.analysis.dto.ComplexityAnalysisResponse;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.DoStmt;
import com.github.javaparser.ast.stmt.ForEachStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.WhileStmt;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Static asymptotic complexity analyzer detecting loop depth, halving patterns,
 * recursive branching, and space allocations.
 */
@Component
public class ComplexityAnalyzer {

    public ComplexityAnalysisResponse analyze(CompilationUnit cu) {
        List<MethodDeclaration> methods = cu.findAll(MethodDeclaration.class);

        // Find primary algorithmic method (skip main if other methods exist)
        MethodDeclaration targetMethod = null;
        for (MethodDeclaration m : methods) {
            if (!m.getNameAsString().equals("main")) {
                targetMethod = m;
                break;
            }
        }
        if (targetMethod == null && !methods.isEmpty()) {
            targetMethod = methods.get(0);
        }

        if (targetMethod == null) {
            return defaultConstantComplexity();
        }

        return analyzeMethod(targetMethod);
    }

    private ComplexityAnalysisResponse analyzeMethod(MethodDeclaration method) {
        String methodName = method.getNameAsString();
        String methodCode = method.toString();

        // 1. Detect recursive calls
        List<MethodCallExpr> calls = method.findAll(MethodCallExpr.class);
        int recursiveCallCount = 0;
        for (MethodCallExpr call : calls) {
            if (call.getNameAsString().equals(methodName)) {
                recursiveCallCount++;
            }
        }

        // 2. Loop Nesting Depth & Loop Detection
        int maxLoopDepth = calculateMaxLoopDepth(method);
        boolean hasHalvingLoop = detectHalvingLoop(method);
        boolean hasSortCall = methodCode.contains("Arrays.sort") || methodCode.contains("Collections.sort");
        boolean hasEarlyExitFlag = methodCode.contains("swapped") && methodCode.contains("break;");

        // Loop line ranges for multiplier UI
        Map<String, String> loopMultiplier = findLoopMultiplier(method, maxLoopDepth);

        // 3. Asymptotic Classification
        String worstTime;
        String avgTime;
        String bestTime;
        String auxSpace;
        boolean inPlace = !methodCode.contains("new int[") && !methodCode.contains("new ArrayList") && !methodCode.contains("new HashMap");
        String explanation;
        List<String> reasoningSteps = new ArrayList<>();

        boolean isDivideAndConquer = recursiveCallCount >= 2 && (
                methodCode.contains("partition") ||
                methodCode.contains("merge") ||
                methodCode.contains("/ 2") ||
                methodCode.contains(">> 1") ||
                (methodCode.contains("low") && methodCode.contains("high"))
        );

        if (isDivideAndConquer) {
            // Divide and Conquer / QuickSort / MergeSort
            worstTime = methodCode.contains("partition") ? "O(n²)" : "O(n log n)";
            avgTime = "O(n log n)";
            bestTime = "O(n log n)";
            auxSpace = methodCode.contains("partition") ? "O(log n)" : "O(n)";
            explanation = "Divide-and-conquer recurrence relation T(n) = 2T(n/2) + O(n) resolves to O(n log n) by the Master Theorem.";
            reasoningSteps.add("Recursive branching partitions problem space across dual sub-arrays.");
            reasoningSteps.add("Partitioning/merging scans elements linearly across tree levels.");
            reasoningSteps.add("Recursion tree has depth log₂(n) with linear operations per level = O(n log n).");
        } else if (recursiveCallCount >= 2) {
            // Exponential recursion (e.g. fib(n-1) + fib(n-2))
            worstTime = "O(2^n)";
            avgTime = "O(2^n)";
            bestTime = "O(1)";
            auxSpace = "O(n)";
            inPlace = false;
            explanation = "Multiple self-recursive calls without memoization spawn an exponential call tree with branching factor ≥ 2.";
            reasoningSteps.add("Detected " + recursiveCallCount + " recursive invocations of '" + methodName + "()' per stack frame.");
            reasoningSteps.add("Recursion depth reaches depth 'n', yielding approximately 2^n total function calls.");
            reasoningSteps.add("Auxiliary space is bounded by the call stack recursion depth O(n).");
        } else if (hasSortCall) {
            worstTime = "O(n log n)";
            avgTime = "O(n log n)";
            bestTime = "O(n)";
            auxSpace = "O(log n)";
            explanation = "Invocation of standard Dual-Pivot Quicksort / TimSort executes in guaranteed O(n log n) asymptotic time.";
            reasoningSteps.add("Method delegates array reordering to optimized library sorting routines.");
            reasoningSteps.add("Sorting dominant term bounds overall runtime to O(n log n).");
        } else if (maxLoopDepth >= 3) {
            worstTime = "O(n³)";
            avgTime = "O(n³)";
            bestTime = "O(n³)";
            auxSpace = "O(1)";
            explanation = "Triple nested loops iterate over three-dimensional bounds resulting in cubic comparisons and steps.";
            reasoningSteps.add("Detected 3 levels of nested loops.");
            reasoningSteps.add("Each loop dimension scales linearly with input size n.");
            reasoningSteps.add("Total operations scale proportionally to n * n * n = O(n³).");
        } else if (maxLoopDepth == 2) {
            // Nested loops (BubbleSort, SelectionSort, Matrix scan)
            worstTime = "O(n²)";
            avgTime = "O(n²)";
            bestTime = hasEarlyExitFlag ? "O(n)" : "O(n²)";
            auxSpace = "O(1)";
            explanation = hasEarlyExitFlag ?
                    "Nested loops execute quadratic comparisons in worst/average case. With the swapped flag, already sorted arrays exit in linear O(n) time." :
                    "Nested loops execute quadratic comparisons across all array passes without early termination.";
            reasoningSteps.add("Outer loop executes up to n passes across the collection.");
            reasoningSteps.add("Inner loop executes adjacent element comparisons and swaps.");
            if (hasEarlyExitFlag) {
                reasoningSteps.add("Early-exit termination flag breaks on pass 1 if no elements swapped, reducing best case to linear O(n).");
            } else {
                reasoningSteps.add("Total operations evaluate to ½(n² - n) comparisons in all cases.");
            }
        } else if (maxLoopDepth == 1) {
            if (hasHalvingLoop) {
                // Binary search
                worstTime = "O(log n)";
                avgTime = "O(log n)";
                bestTime = "O(1)";
                auxSpace = "O(1)";
                explanation = "Loop index interval is halved on each iteration, converging logarithmically.";
                reasoningSteps.add("Search boundary is subdivided by 2 on each step.");
                reasoningSteps.add("Number of iterations to reach base case satisfies 2^k = n, hence k = log₂(n).");
            } else {
                // Single linear loop
                worstTime = "O(n)";
                avgTime = "O(n)";
                bestTime = "O(1)";
                auxSpace = "O(1)";
                explanation = "Single loop traverses elements linearly from index 0 to n.";
                reasoningSteps.add("Loop counter increments monotonically by a constant factor.");
                reasoningSteps.add("Each element is visited a constant number of times O(1).");
            }
        } else if (recursiveCallCount == 1 && hasHalvingLoop) {
            // Binary search recursive
            worstTime = "O(log n)";
            avgTime = "O(log n)";
            bestTime = "O(1)";
            auxSpace = "O(log n)";
            explanation = "Single recursive call halves the search space logarithmically.";
            reasoningSteps.add("Subproblem size halves on each recursive activation.");
            reasoningSteps.add("Recursion stack depth is log₂(n).");
        } else {
            return defaultConstantComplexity();
        }

        Map<String, String> timeComplexity = new LinkedHashMap<>();
        timeComplexity.put("worst", worstTime);
        timeComplexity.put("average", avgTime);
        timeComplexity.put("best", bestTime);

        Map<String, Object> spaceComplexity = new LinkedHashMap<>();
        spaceComplexity.put("auxiliary", auxSpace);
        spaceComplexity.put("inPlace", inPlace);

        return ComplexityAnalysisResponse.builder()
                .timeComplexity(timeComplexity)
                .spaceComplexity(spaceComplexity)
                .explanation(explanation)
                .reasoningSteps(reasoningSteps)
                .loopMultiplier(loopMultiplier)
                .build();
    }

    private int calculateMaxLoopDepth(Node node) {
        int maxChildDepth = 0;
        for (Node child : node.getChildNodes()) {
            maxChildDepth = Math.max(maxChildDepth, calculateMaxLoopDepth(child));
        }

        boolean isLoop = node instanceof ForStmt || node instanceof WhileStmt ||
                node instanceof ForEachStmt || node instanceof DoStmt;

        return isLoop ? 1 + maxChildDepth : maxChildDepth;
    }

    private boolean detectHalvingLoop(MethodDeclaration method) {
        String code = method.toString();
        return code.contains("/ 2") || code.contains(">> 1") || code.contains("*= 2") || code.contains("/= 2");
    }

    private Map<String, String> findLoopMultiplier(MethodDeclaration method, int maxLoopDepth) {
        Map<String, String> map = new LinkedHashMap<>();
        List<ForStmt> forLoops = method.findAll(ForStmt.class);
        List<WhileStmt> whileLoops = method.findAll(WhileStmt.class);

        int startLine = 1;
        int endLine = 1;

        if (!forLoops.isEmpty()) {
            startLine = forLoops.get(0).getBegin().map(p -> p.line).orElse(1);
            endLine = forLoops.get(forLoops.size() - 1).getEnd().map(p -> p.line).orElse(startLine);
        } else if (!whileLoops.isEmpty()) {
            startLine = whileLoops.get(0).getBegin().map(p -> p.line).orElse(1);
            endLine = whileLoops.get(whileLoops.size() - 1).getEnd().map(p -> p.line).orElse(startLine);
        }

        map.put("lines", "Lines " + startLine + " - " + endLine);
        if (maxLoopDepth >= 2) {
            map.put("operationsFormula", "½(n² - n) comparisons");
        } else if (maxLoopDepth == 1) {
            map.put("operationsFormula", "n iterations");
        } else {
            map.put("operationsFormula", "O(1) constant steps");
        }
        return map;
    }

    private ComplexityAnalysisResponse defaultConstantComplexity() {
        Map<String, String> time = new LinkedHashMap<>();
        time.put("worst", "O(1)");
        time.put("average", "O(1)");
        time.put("best", "O(1)");

        Map<String, Object> space = new LinkedHashMap<>();
        space.put("auxiliary", "O(1)");
        space.put("inPlace", true);

        return ComplexityAnalysisResponse.builder()
                .timeComplexity(time)
                .spaceComplexity(space)
                .explanation("Method contains straight-line sequential instructions without loops or recursion, executing in constant time O(1).")
                .reasoningSteps(List.of("No loops or recursive calls detected in the method body.", "Execution takes a fixed number of CPU cycles irrespective of input scale."))
                .loopMultiplier(Map.of("lines", "Lines 1 - 1", "operationsFormula", "O(1) constant instructions"))
                .build();
    }
}
