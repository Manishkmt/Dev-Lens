package com.devlens.ai.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * MockAIService
 * Provides realistic, rich pedagogical responses matching DevLens schemas.
 * Used automatically when AI_API_KEY is not configured or in offline/demo environments.
 */
@Slf4j
@Service("mockAIService")
public class MockAIService implements AIService {

    @Override
    public String complete(String systemPrompt, String userPrompt) {
        log.info("MockAIService executing completion for prompt type");
        String combined = (systemPrompt + " " + userPrompt).toLowerCase();

        if (combined.contains("vivaquestions") || combined.contains("quiz") || combined.contains("curriculum") || combined.contains("learning")) {
            return generateMockLearning(combined);
        } else if (combined.contains("varianta") || combined.contains("variantb") || combined.contains("variant b") || combined.contains("differential") || combined.contains("compare")) {
            return generateMockCompare(combined);
        } else if (combined.contains("jitstatus") || combined.contains("tradeoffs") || combined.contains("trade-offs") || combined.contains("optimization")) {
            return generateMockOptimization(combined);
        } else if (combined.contains("javadoc") || combined.contains("projectsummary") || combined.contains("documentation")) {
            return generateMockDocs(combined);
        } else if (combined.contains("issuescount") || combined.contains("suggestedfix") || combined.contains("fixcode") || combined.contains("debugging")) {
            return generateMockDebug(combined);
        } else if (combined.contains("linebyline") || combined.contains("conceptchips") || combined.contains("stepbystep") || combined.contains("signature") || combined.contains("summary") || combined.contains("explanation")) {
            return generateMockExplanation(combined);
        } else {
            return generateMockChat(userPrompt);
        }
    }

    private String generateMockExplanation(String prompt) {
        boolean isBeginner = prompt.contains("novice") || prompt.contains("beginner");
        boolean isAdvanced = prompt.contains("senior") || prompt.contains("advanced");

        String summary = isBeginner
                ? "This program sorts numbers step-by-step like arranging playing cards in your hand, repeatedly checking pairs."
                : isAdvanced
                ? "Adaptive quadratic-time exchange sort utilizing single-phase boolean early termination and cache-local memory access."
                : "Optimized Bubble Sort algorithm that repeatedly compares and exchanges adjacent elements with early termination.";

        return """
                {
                  "summary": "%s",
                  "confidence": "99.2%%",
                  "signature": "public static void sort(int[] arr)",
                  "returnType": "void (in-place)",
                  "precondition": "Valid non-null array of comparable elements",
                  "postcondition": "Array reordered in non-decreasing numerical order",
                  "tags": ["#ComparisonSort", "#InPlace", "#StableSort", "#EarlyTermination"],
                  "stepByStep": [
                    { "step": 1, "text": "Obtain array length 'n' and initialize outer pass boundary." },
                    { "step": 2, "text": "Set swapped flag to false before entering inner traversal." },
                    { "step": 3, "text": "Scan adjacent elements and swap if left element is strictly greater." },
                    { "step": 4, "text": "If zero inversions occurred in a full pass, break immediately in O(n) time." }
                  ],
                  "lineByLine": [
                    { "line": 2, "code": "int n = arr.length;", "explanation": "Caches array length to avoid repeated dereferencing." },
                    { "line": 4, "code": "for (int i = 0; i < n - 1; i++)", "explanation": "Outer loop controlling the bubbling phases." },
                    { "line": 6, "code": "if (arr[j] > arr[j + 1])", "explanation": "Inversion detection condition." }
                  ],
                  "methodExplanations": [
                    { "methodName": "sort", "purpose": "Sorts input array directly in memory", "complexity": "O(n) best, O(n²) worst" }
                  ],
                  "variableExplanations": [
                    { "variableName": "swapped", "type": "boolean", "role": "Sentinel flag for adaptive early exit" },
                    { "variableName": "n", "type": "int", "role": "Array length boundary" }
                  ],
                  "conceptChips": ["Inversion Swapping", "Adaptive Complexity", "Loop Sentinel", "Stable Ordering"]
                }
                """.formatted(summary);
    }

    private String generateMockDebug(String prompt) {
        return """
                {
                  "status": "success",
                  "issuesCount": 2,
                  "warnings": 1,
                  "infos": 1,
                  "issues": [
                    {
                      "id": "ISSUE-1",
                      "type": "WARNING",
                      "title": "Redundant Inner Loop Bound Traversal",
                      "line": 7,
                      "endLine": 7,
                      "codeSnippet": "for (int j = 0; j < n - 1; j++)",
                      "explanation": "Inner loop scans elements that have already bubbled to their final sorted positions.",
                      "whyItHappens": "Each pass guarantees that the i-th largest element is placed at index n - i - 1. Checking past it wastes CPU cycles.",
                      "suggestedFix": "Subtract outer index 'i' in the inner loop condition.",
                      "fixCode": "for (int j = 0; j < n - i - 1; j++) {",
                      "beforeAfterDiff": "- for (int j = 0; j < n - 1; j++)\\n+ for (int j = 0; j < n - i - 1; j++)"
                    },
                    {
                      "id": "ISSUE-2",
                      "type": "INFO",
                      "title": "Missing Null Pointer Guard",
                      "line": 2,
                      "endLine": 2,
                      "codeSnippet": "public static void sort(int[] arr)",
                      "explanation": "Directly dereferencing arr.length without validating if arr is null throws NullPointerException.",
                      "whyItHappens": "JVM cannot access object headers on null references.",
                      "suggestedFix": "Add defensive guard condition at method entry.",
                      "fixCode": "if (arr == null || arr.length <= 1) return;",
                      "beforeAfterDiff": "+ if (arr == null || arr.length <= 1) return;"
                    }
                  ]
                }
                """;
    }

    private String generateMockOptimization(String prompt) {
        return """
                {
                  "jitStatus": "JIT Friendly (C2 Inlining Eligible)",
                  "suggestionTitle": "Adaptive Sentinel & Micro-Method Inlining",
                  "description": "Introducing an early-exit boolean flag reduces comparisons on sorted or near-sorted arrays from O(n²) to O(n). Moving the swap logic into a helper method allows HotSpot C2 compiler register allocation.",
                  "refactoredSnippet": "// Optimized with early exit and micro-method\\nif (arr == null || arr.length <= 1) return;\\nboolean swapped;\\nfor (int i = 0; i < n - 1; i++) {\\n    swapped = false;\\n    for (int j = 0; j < n - i - 1; j++) {\\n        if (arr[j] > arr[j + 1]) {\\n            swap(arr, j, j + 1);\\n            swapped = true;\\n        }\\n    }\\n    if (!swapped) break;\\n}",
                  "tradeOffs": {
                    "time": "Best-case execution reduced from O(n²) to O(n)",
                    "memory": "O(1) constant auxiliary memory maintained",
                    "readability": "Clear intent with defensive guard and structured helper method",
                    "rationale": "Adaptive detection provides near-instantaneous completion on already-sorted sequences."
                  },
                  "benchmark": {
                    "originalCycles": "1,420 ns",
                    "optimizedCycles": "1,110 ns",
                    "improvement": "21.8% faster"
                  }
                }
                """;
    }

    private String generateMockDocs(String prompt) {
        return """
                {
                  "javadoc": "/**\\n * Sorts the specified integer array into non-decreasing order using\\n * an adaptive Bubble Sort algorithm.\\n *\\n * <p><strong>Complexity:</strong></p>\\n * <ul>\\n *   <li>Time: O(n) best-case, O(n²) average and worst-case</li>\\n *   <li>Space: O(1) auxiliary in-place</li>\\n * </ul>\\n *\\n * @param arr the array to be sorted\\n * @throws NullPointerException if {@code arr} is null and unguarded\\n */",
                  "markdown": "### DevLens Component Overview\\n- **Language**: Java 17\\n- **Category**: Comparison Sort\\n- **Time Complexity**: Best O(n), Worst O(n²)\\n- **Space**: O(1) In-Place\\n- **Thread Safety**: Stateless utility procedure",
                  "projectSummary": "Educational sorting utility demonstrating loop bounding, sentinel termination, and array mutation."
                }
                """;
    }

    private String generateMockLearning(String prompt) {
        return """
                {
                  "concept": {
                    "title": "Bubble Sort & Inversion Counting",
                    "category": "Comparison-Based Sorting",
                    "difficulty": "Beginner",
                    "description": "Bubble Sort is an elementary sorting algorithm that repeatedly steps through an array, compares adjacent elements, and swaps them if they are in the wrong numerical order."
                  },
                  "steps": [
                    {
                      "step": 1,
                      "title": "Pass Initialisation & Setup",
                      "content": "Begin at index 0 and inspect adjacent pair (arr[0], arr[1]). Reset swapped flag to false."
                    },
                    {
                      "step": 2,
                      "title": "Local Inversion Check & Swap",
                      "content": "If arr[j] > arr[j + 1], swap them in place and set swapped = true."
                    },
                    {
                      "step": 3,
                      "title": "The Bubbling Invariant",
                      "content": "After pass i completes, the i-th largest element is locked at index n - i - 1."
                    },
                    {
                      "step": 4,
                      "title": "Early Termination Optimization",
                      "content": "If an entire pass caused zero swaps (!swapped), exit immediately in O(n) time."
                    }
                  ],
                  "quiz": [
                    {
                      "id": 1,
                      "question": "What is the best-case time complexity of an optimized Bubble Sort on an already sorted array?",
                      "options": ["O(1)", "O(n)", "O(n log n)", "O(n²)"],
                      "answerIndex": 1,
                      "explanation": "Because of the swapped flag, the outer loop breaks after the very first pass of n - 1 comparisons when no swaps occur."
                    },
                    {
                      "id": 2,
                      "question": "Why is Bubble Sort classified as a stable sorting algorithm?",
                      "options": [
                        "It uses O(1) auxiliary memory.",
                        "It only swaps when arr[j] > arr[j+1], keeping equal elements in their original relative positions.",
                        "It has a consistent runtime across all inputs.",
                        "It can be executed recursively without stack overflow."
                      ],
                      "answerIndex": 1,
                      "explanation": "Because elements are only swapped when one is strictly greater than the other, equal keys never cross."
                    },
                    {
                      "id": 3,
                      "question": "What is the maximum number of comparisons performed by unoptimized Bubble Sort on an array of size n?",
                      "options": ["n", "n(n - 1) / 2", "n²", "2ⁿ"],
                      "answerIndex": 1,
                      "explanation": "The sum of integers from 1 to n - 1 is n(n - 1) / 2 comparisons."
                    }
                  ],
                  "vivaQuestions": [
                    {
                      "question": "Why is Bubble Sort rarely used in production large-scale systems?",
                      "answer": "With quadratic time complexity O(n²), the number of operations explodes. For 100,000 items, it takes ~5 billion operations versus ~1.7 million for QuickSort."
                    },
                    {
                      "question": "How does the swapped boolean flag optimize the algorithm?",
                      "answer": "It detects when an entire pass caused zero inversions, proving the list is sorted and enabling an immediate O(n) exit."
                    },
                    {
                      "question": "Is Bubble Sort an in-place algorithm? Why?",
                      "answer": "Yes, it is in-place because its auxiliary space complexity is O(1), modifying the existing array buffer."
                    },
                    {
                      "question": "How does Bubble Sort compare with Selection Sort?",
                      "answer": "Both are O(n²) worst-case, but Bubble Sort is adaptive (O(n) on sorted data) and stable, while Selection Sort always performs O(n²) comparisons and is unstable."
                    }
                  ],
                  "relatedTopics": ["QuickSort", "Insertion Sort", "Inversion Counting", "Selection Sort"]
                }
                """;
    }

    private String generateMockCompare(String prompt) {
        return """
                {
                  "metrics": [
                    { "metric": "Worst-Case Time Complexity", "variantA": "O(n²)", "variantB": "O(n²)" },
                    { "metric": "Best-Case Time Complexity", "variantA": "O(n²)", "variantB": "O(n)" },
                    { "metric": "Auxiliary Space Complexity", "variantA": "O(1) In-Place", "variantB": "O(1) In-Place" },
                    { "metric": "Stability Guarantee", "variantA": "Stable", "variantB": "Stable" },
                    { "metric": "Sorted Input Comparisons", "variantA": "45 operations (n=10)", "variantB": "9 operations (n=10)" },
                    { "metric": "Early Exit Capability", "variantA": "No", "variantB": "Yes (swapped flag)" }
                  ],
                  "verdict": {
                    "winner": "Variant B (Optimized Bubble Sort)",
                    "summary": "Variant B improves performance drastically on partially or fully sorted arrays by incorporating an adaptive boolean swap sentinel flag.",
                    "recommendation": "Use Variant B for academic labs. For production collections larger than 50 elements, migrate to Dual-Pivot QuickSort or TimSort."
                  }
                }
                """;
    }

    private String generateMockChat(String userPrompt) {
        String lower = userPrompt.toLowerCase();
        if (lower.contains("swap") || lower.contains("temp") || lower.contains("line 9")) {
            return """
                    In Java, primitive assignments evaluate immediately. If you execute `arr[j] = arr[j+1]` directly, the original value in `arr[j]` is overwritten before it can be assigned to `arr[j+1]`.

                    A buffer variable preserves the value safely:
                    ```java
                    int temp = arr[j];      // Preserve original value
                    arr[j] = arr[j + 1];   // Overwrite with next element
                    arr[j + 1] = temp;     // Place preserved value
                    ```

                    In Java 17 primitive arrays, this three-step exchange is canonical.
                    """;
        } else if (lower.contains("complexity") || lower.contains("o(n)")) {
            return """
                    Bubble Sort exhibits quadratic time complexity **O(n²)** in the worst and average cases because of nested loops executing $\\frac{n(n-1)}{2}$ comparisons.

                    However, by introducing the `swapped` boolean sentinel, already-sorted input breaks after the very first pass, achieving linear **O(n)** time.
                    """;
        } else if (lower.contains("null") || lower.contains("error") || lower.contains("exception")) {
            return """
                    To make your method production-safe, guard against `null` pointers at entry:
                    ```java
                    if (arr == null || arr.length <= 1) {
                        return;
                    }
                    ```
                    This prevents a `NullPointerException` when calling `arr.length`.
                    """;
        } else {
            return """
                    I analyzed your query: "%s".

                    In Java 17, this implementation adheres to standard Data Structures & Algorithms coursework principles. You can inspect the **AST Visualizer** and **Complexity tabs** to explore loop counts and control flow graphs.
                    """.formatted(userPrompt.replace("\"", "\\\""));
        }
    }
}
