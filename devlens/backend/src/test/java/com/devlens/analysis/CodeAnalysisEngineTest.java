package com.devlens.analysis;

import com.devlens.analysis.dto.*;
import com.devlens.analysis.service.CodeAnalysisEngineService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class CodeAnalysisEngineTest {

    @Autowired
    private CodeAnalysisEngineService engineService;

    @Test
    @DisplayName("1. Bubble Sort: returns O(n²) worst time, O(1) space, and valid flowchart")
    void testBubbleSortAnalysis() {
        String code = "public class BubbleSort {\n" +
                "    public static void bubbleSort(int[] arr) {\n" +
                "        int n = arr.length;\n" +
                "        boolean swapped;\n" +
                "        for (int i = 0; i < n - 1; i++) {\n" +
                "            swapped = false;\n" +
                "            for (int j = 0; j < n - 1; j++) {\n" +
                "                if (arr[j] > arr[j + 1]) {\n" +
                "                    int temp = arr[j];\n" +
                "                    arr[j] = arr[j + 1];\n" +
                "                    arr[j + 1] = temp;\n" +
                "                    swapped = true;\n" +
                "                }\n" +
                "            }\n" +
                "            if (!swapped) break;\n" +
                "        }\n" +
                "    }\n" +
                "}";

        AnalyzeRequest req = AnalyzeRequest.builder().language("java").code(code).build();
        ComplexityAnalysisResponse complexity = engineService.analyzeComplexity(req);
        assertEquals("O(n²)", complexity.getTimeComplexity().get("worst"));
        assertEquals("O(1)", complexity.getSpaceComplexity().get("auxiliary"));

        FlowchartAnalysisResponse flowchart = engineService.analyzeFlowchart(req);
        assertNotNull(flowchart.getMermaid());
        assertTrue(flowchart.getMermaid().contains("graph TD"));
        assertTrue(flowchart.getMermaid().contains("bubbleSort"));
    }

    @Test
    @DisplayName("2. Binary Search: returns O(log n) time and O(1) space")
    void testBinarySearchAnalysis() {
        String code = "public class BinarySearch {\n" +
                "    public static int search(int[] arr, int target) {\n" +
                "        int low = 0, high = arr.length - 1;\n" +
                "        while (low <= high) {\n" +
                "            int mid = low + (high - low) / 2;\n" +
                "            if (arr[mid] == target) return mid;\n" +
                "            if (arr[mid] < target) low = mid + 1;\n" +
                "            else high = mid - 1;\n" +
                "        }\n" +
                "        return -1;\n" +
                "    }\n" +
                "}";

        AnalyzeRequest req = AnalyzeRequest.builder().language("java").code(code).build();
        ComplexityAnalysisResponse complexity = engineService.analyzeComplexity(req);
        assertEquals("O(log n)", complexity.getTimeComplexity().get("worst"));
        assertEquals("O(1)", complexity.getSpaceComplexity().get("auxiliary"));
    }

    @Test
    @DisplayName("3. Fibonacci Exponential Recursion: returns O(2^n) time")
    void testFibonacciRecursionAnalysis() {
        String code = "public class Fibonacci {\n" +
                "    public static int fib(int n) {\n" +
                "        if (n <= 1) return n;\n" +
                "        return fib(n - 1) + fib(n - 2);\n" +
                "    }\n" +
                "}";

        AnalyzeRequest req = AnalyzeRequest.builder().language("java").code(code).build();
        ComplexityAnalysisResponse complexity = engineService.analyzeComplexity(req);
        assertEquals("O(2^n)", complexity.getTimeComplexity().get("worst"));
        assertEquals("O(n)", complexity.getSpaceComplexity().get("auxiliary"));
    }

    @Test
    @DisplayName("4. QuickSort Recursive Partitioning: returns O(n log n) average time")
    void testQuickSortAnalysis() {
        String code = "public class QuickSort {\n" +
                "    public static void sort(int[] arr, int low, int high) {\n" +
                "        if (low < high) {\n" +
                "            int pi = partition(arr, low, high);\n" +
                "            sort(arr, low, pi - 1);\n" +
                "            sort(arr, pi + 1, high);\n" +
                "        }\n" +
                "    }\n" +
                "    private static int partition(int[] arr, int low, int high) { return low; }\n" +
                "}";

        AnalyzeRequest req = AnalyzeRequest.builder().language("java").code(code).build();
        ComplexityAnalysisResponse complexity = engineService.analyzeComplexity(req);
        assertEquals("O(n log n)", complexity.getTimeComplexity().get("average"));
    }

    @Test
    @DisplayName("5. Triple Nested Loops: returns O(n³) cubic complexity")
    void testMatrixMultiplyAnalysis() {
        String code = "public class MatrixMultiply {\n" +
                "    public static void multiply(int[][] a, int[][] b, int[][] c, int n) {\n" +
                "        for (int i = 0; i < n; i++) {\n" +
                "            for (int j = 0; j < n; j++) {\n" +
                "                for (int k = 0; k < n; k++) {\n" +
                "                    c[i][j] += a[i][k] * b[k][j];\n" +
                "                }\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}";

        AnalyzeRequest req = AnalyzeRequest.builder().language("java").code(code).build();
        ComplexityAnalysisResponse complexity = engineService.analyzeComplexity(req);
        assertEquals("O(n³)", complexity.getTimeComplexity().get("worst"));
    }

    @Test
    @DisplayName("6. Infinite Loop: detects while(true) without break as ERROR")
    void testInfiniteLoopWhileTrue() {
        String code = "public class Server {\n" +
                "    public void run() {\n" +
                "        while (true) {\n" +
                "            System.out.println(\"running\");\n" +
                "        }\n" +
                "    }\n" +
                "}";

        AnalyzeRequest req = AnalyzeRequest.builder().language("java").code(code).build();
        ErrorAnalysisResponse errors = engineService.analyzeErrors(req);
        assertTrue(errors.getIssues().stream().anyMatch(i -> i.getTitle().contains("Definite Infinite Loop")));
    }

    @Test
    @DisplayName("7. Loop Variable Never Modified: detects infinite loop condition")
    void testLoopVariableNeverModified() {
        String code = "public class Counter {\n" +
                "    public void count() {\n" +
                "        int i = 0;\n" +
                "        while (i < 10) {\n" +
                "            System.out.println(i);\n" +
                "        }\n" +
                "    }\n" +
                "}";

        AnalyzeRequest req = AnalyzeRequest.builder().language("java").code(code).build();
        ErrorAnalysisResponse errors = engineService.analyzeErrors(req);
        assertTrue(errors.getIssues().stream().anyMatch(i -> i.getTitle().contains("Never Modified")));
    }

    @Test
    @DisplayName("8. String Comparison with ==: detects reference equality bug")
    void testStringEqualityDetection() {
        String code = "public class AuthCheck {\n" +
                "    public boolean check(String user) {\n" +
                "        return user == \"admin\";\n" +
                "    }\n" +
                "}";

        AnalyzeRequest req = AnalyzeRequest.builder().language("java").code(code).build();
        ErrorAnalysisResponse errors = engineService.analyzeErrors(req);
        assertTrue(errors.getIssues().stream().anyMatch(i -> i.getTitle().contains("String Reference Comparison")));
    }

    @Test
    @DisplayName("9. Empty Catch Block: detects swallowed exception")
    void testEmptyCatchBlockDetection() {
        String code = "public class FileHandler {\n" +
                "    public void load() {\n" +
                "        try {\n" +
                "            int x = 10 / 0;\n" +
                "        } catch (Exception e) {\n" +
                "        }\n" +
                "    }\n" +
                "}";

        AnalyzeRequest req = AnalyzeRequest.builder().language("java").code(code).build();
        ErrorAnalysisResponse errors = engineService.analyzeErrors(req);
        assertTrue(errors.getIssues().stream().anyMatch(i -> i.getTitle().contains("Empty Catch Block")));
    }

    @Test
    @DisplayName("10. Unused Local Variable: detects declared variable that is never read")
    void testUnusedVariableDetection() {
        String code = "public class MathUtils {\n" +
                "    public int add(int a, int b) {\n" +
                "        int unusedVar = 42;\n" +
                "        return a + b;\n" +
                "    }\n" +
                "}";

        AnalyzeRequest req = AnalyzeRequest.builder().language("java").code(code).build();
        ErrorAnalysisResponse errors = engineService.analyzeErrors(req);
        assertTrue(errors.getIssues().stream().anyMatch(i -> i.getTitle().contains("Unused Local Variable")));
    }

    @Test
    @DisplayName("11. Syntax Error (missing semicolon): returns structured error without 500")
    void testSyntaxErrorHandling() {
        String code = "public class Broken {\n" +
                "    public void test() {\n" +
                "        int a = 10\n" + // missing semicolon
                "    }\n" +
                "}";

        AnalyzeRequest req = AnalyzeRequest.builder().language("java").code(code).build();
        ErrorAnalysisResponse errors = engineService.analyzeErrors(req);
        assertEquals("syntax_error", errors.getStatus());
        assertTrue(errors.getIssuesCount() > 0);
        assertFalse(errors.getIssues().get(0).getExplanation().isEmpty());
    }

    @Test
    @DisplayName("12. Syntax Error (bare method without enclosing class): returns class required warning")
    void testBareMethodRejection() {
        String code = "public void calculate() { int a = 5; }";

        AnalyzeRequest req = AnalyzeRequest.builder().language("java").code(code).build();
        ErrorAnalysisResponse errors = engineService.analyzeErrors(req);
        assertEquals("syntax_error", errors.getStatus());
        assertTrue(errors.getIssues().stream().anyMatch(i -> i.getTitle().contains("Enclosing Class Required")));
    }

    @Test
    @DisplayName("13. Non-Java Language: returns structured 200 with limited support notice")
    void testNonJavaLanguageNotice() {
        String code = "def bubble_sort(arr):\n    pass\n";

        AnalyzeRequest req = AnalyzeRequest.builder().language("python").code(code).build();
        ErrorAnalysisResponse errors = engineService.analyzeErrors(req);
        assertEquals("limited_support", errors.getStatus());
        assertTrue(errors.getIssues().get(0).getTitle().contains("Limited Language Support"));

        ReviewAnalysisResponse review = engineService.analyzeReview(req);
        assertEquals(70, review.getOverallScore());
    }

    @Test
    @DisplayName("14. Quality Review: reliability-weighted scoring reflects defect deductions")
    void testQualityReviewScoring() {
        String cleanCode = "public class SafeCalculator {\n" +
                "    public static int add(int a, int b) {\n" +
                "        return a + b;\n" +
                "    }\n" +
                "}";

        AnalyzeRequest req = AnalyzeRequest.builder().language("java").code(cleanCode).build();
        ReviewAnalysisResponse review = engineService.analyzeReview(req);
        assertTrue(review.getOverallScore() >= 90);
        assertNotNull(review.getBreakdown().get("reliability"));
        assertNotNull(review.getBreakdown().get("maintainability"));
    }
}
