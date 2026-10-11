/**
 * DevLens Mock Code Analysis Module
 * Rich, realistic Java analysis data matching Stitch designs
 */

export function mockAnalyzeErrors(language = "java", code = "", level = "INTERMEDIATE") {
  return {
    status: "success",
    issuesCount: 2,
    warnings: 1,
    infos: 1,
    issues: [
      {
        id: "ISSUE-1",
        type: "WARNING",
        severityClass: "badge-warning",
        title: "Boundary Inefficiency in Inner Loop",
        line: 9,
        endLine: 9,
        codeSnippet: "for (int j = 0; j < n - 1; j++)",
        explanation: "The inner comparison loop scans the entire array without subtracting 'i', checking already-sorted tail elements.",
        whyItHappens: "Each outer loop pass guarantees that the i-th largest element settles into its final indexed position. Scanning past 'n - i - 1' performs redundant checks.",
        suggestedFix: "Limit inner scan loop bound to 'n - i - 1' to avoid unnecessary comparison cycles.",
        fixCode: "            for (int j = 0; j < n - i - 1; j++) {"
      },
      {
        id: "ISSUE-2",
        type: "INFO",
        severityClass: "badge-ai",
        title: "Missing Defensive Null Guard",
        line: 2,
        endLine: 2,
        codeSnippet: "public static void bubbleSort(int[] arr)",
        explanation: "Method dereferences 'arr.length' directly without validating whether the input array reference is null.",
        whyItHappens: "Invoking array length on a null parameter will throw a NullPointerException at runtime during real execution.",
        suggestedFix: "Add a null check at the start of the method.",
        fixCode: "        if (arr == null || arr.length <= 1) return;"
      }
    ]
  };
}

export function mockAnalyzeComplexity(language = "java", code = "", level = "INTERMEDIATE") {
  return {
    timeComplexity: {
      worst: "O(n²)",
      average: "O(n²)",
      best: "O(n)"
    },
    spaceComplexity: {
      auxiliary: "O(1)",
      inPlace: true
    },
    explanation: "Nested loops execute quadratic comparisons in worst/average case. With the boolean swapped flag, already-sorted input terminates after one linear scan.",
    loopMultiplier: {
      lines: "Lines 6 - 15",
      operationsFormula: "½(n² - n) operations",
      description: "The outer loop operates at index i < n - 1 while inner comparison spans j < n - i - 1. Coupling these iterations produces quadratic polynomial growth."
    },
    metrics: [
      { name: "Worst-Case Comparisons", value: "n(n-1) / 2" },
      { name: "Worst-Case Swaps", value: "n(n-1) / 2" },
      { name: "Best-Case Comparisons", value: "n - 1" },
      { name: "Auxiliary Space", value: "O(1) in-place" }
    ]
  };
}

export function mockAnalyzeReview(language = "java", code = "", level = "INTERMEDIATE") {
  return {
    overallScore: 88,
    breakdown: {
      readability: { score: 92, note: "Clean variable naming, concise layout, intuitive flow." },
      maintainability: { score: 74, note: "Modular method, but loop body could extract swap function." },
      reliability: { score: 68, note: "Missing defensive edge case check for null or empty array." },
      bestPractices: { score: 80, note: "Adheres to standard Java convention and early termination pattern." }
    },
    recommendations: [
      {
        priority: "HIGH",
        line: 2,
        title: "Add defensive input check",
        description: "Verify array pointer validity before dereferencing array length."
      },
      {
        priority: "MEDIUM",
        line: 9,
        title: "Extract swap logic into helper method",
        description: "Improves readability and hot loop inline opportunities."
      },
      {
        priority: "LOW",
        line: 16,
        title: "Add JavaDoc explanation",
        description: "Document early-break optimization behavior and stability guarantee."
      }
    ]
  };
}

export function mockAnalyzeFlowchart(language = "java", code = "", level = "INTERMEDIATE") {
  return {
    mermaid: `graph TD
    A([ENTRY: bubbleSort(arr)]) --> B{arr == null or len <= 1?}
    B -- Yes --> C([RETURN: Exit])
    B -- No --> D[int i = 0]
    D --> E{i < n - 1?}
    E -- No --> C
    E -- Yes --> F[swapped = false; j = 0]
    F --> G{j < n - i - 1?}
    G -- Yes --> H{arr[j] > arr[j + 1]?}
    H -- Yes (Swap) --> I[swap(arr, j, j+1)<br/>swapped = true]
    H -- No --> J[j++]
    I --> J
    J --> G
    G -- No --> K{!swapped?}
    K -- Yes (Early Exit) --> C
    K -- No --> L[i++]
    L --> E`,
    nodes: [
      { id: "A", line: 2, label: "ENTRY: bubbleSort(arr)", type: "terminal" },
      { id: "E", line: 6, label: "Outer Loop Condition (i < n - 1)", type: "decision" },
      { id: "H", line: 9, label: "Comparison Test (arr[j] > arr[j + 1])", type: "decision" },
      { id: "I", line: 11, label: "Swap Operation", type: "action" },
      { id: "K", line: 17, label: "Early Exit Check (!swapped)", type: "decision" }
    ]
  };
}

export function mockAnalyzeOptimization(language = "java", code = "", level = "INTERMEDIATE") {
  return {
    jitStatus: "JIT Friendly",
    suggestionTitle: "Scalar Replacement & Inlining",
    description: "Extracting the swap logic into a micro-method allows the HotSpot C2 compiler to inline and optimize registers effectively.",
    refactoredSnippet: `// Suggested Micro-Method Optimization:
private static void swap(int[] arr, int i, int j) {
    int temp = arr[i];
    arr[i] = arr[j];
    arr[j] = temp;
}`,
    benchmark: {
      originalCycles: "1,420 ns",
      optimizedCycles: "1,110 ns",
      improvement: "21.8% faster"
    }
  };
}

export function mockAnalyzeDocs(language = "java", code = "", level = "INTERMEDIATE") {
  return {
    javadoc: `/**
 * Sorts the specified integer array into ascending numerical order
 * using the optimized Bubble Sort algorithm.
 *
 * <p><strong>Time Complexity:</strong></p>
 * <ul>
 *   <li>Best Case: O(n) when array is already sorted</li>
 *   <li>Average Case: O(n²)</li>
 *   <li>Worst Case: O(n²)</li>
 * </ul>
 *
 * <p><strong>Space Complexity:</strong> O(1) auxiliary (in-place).</p>
 *
 * @param arr the array to be sorted
 * @throws NullPointerException if arr is null (unless guarded)
 */`,
    markdown: `### Bubble Sort (Java 17)
- **Stability**: Stable sort
- **Method**: \`bubbleSort(int[] arr)\`
- **Space**: O(1) in-place
- **Early Break**: Yes, enabled via boolean \`swapped\` flag.`
  };
}

export function mockAnalyzeExplanation(language = "java", code = "", level = "INTERMEDIATE") {
  return {
    summary: "Optimized Bubble Sort implementation with early termination flag. Correctly sorts an array of integers in ascending order with O(n²) worst-case and O(n) best-case complexity.",
    confidence: "99.4%",
    signature: "public static void bubbleSort(int[] arr)",
    returnType: "void (in-place)",
    precondition: "Valid non-null int[] array",
    postcondition: "Sorted in ascending numerical order",
    tags: ["#ComparisonSort", "#InPlace", "#StableSort", "#EarlyBreak"],
    stepByStep: [
      { step: 1, text: "Calculates array length 'n' and prepares outer iteration counter 'i'." },
      { step: 2, text: "Resets 'swapped' flag to false before each pass to detect sorted states." },
      { step: 3, text: "Scans adjacent elements from j = 0 to n - i - 1 and compares pairs." },
      { step: 4, text: "Swaps elements when arr[j] > arr[j + 1] and marks swapped = true." },
      { step: 5, text: "If no swaps occurred throughout a complete pass, terminates early." }
    ]
  };
}

export function mockAnalyzeAll(language = "java", code = "", level = "INTERMEDIATE") {
  return {
    language,
    level,
    explanation: mockAnalyzeExplanation(language, code, level),
    errors: mockAnalyzeErrors(language, code, level),
    complexity: mockAnalyzeComplexity(language, code, level),
    review: mockAnalyzeReview(language, code, level),
    flowchart: mockAnalyzeFlowchart(language, code, level),
    optimization: mockAnalyzeOptimization(language, code, level),
    docs: mockAnalyzeDocs(language, code, level)
  };
}

export function mockSaveAnalysis(data) {
  return {
    id: Math.floor(Math.random() * 1000) + 500,
    type: data.type || "COMPREHENSIVE",
    language: data.language || "java",
    qualityScore: 88,
    createdAt: new Date().toISOString()
  };
}
