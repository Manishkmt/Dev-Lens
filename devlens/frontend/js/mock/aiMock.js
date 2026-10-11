/**
 * DevLens Mock AI Assistant & Learning Module
 */

let chatSessions = [
  {
    id: "session-101",
    title: "BubbleSort Inversion & Optimization",
    updatedAt: "2026-10-11T12:45:00Z",
    messages: [
      {
        id: "msg-1",
        sender: "user",
        text: "Why does line 9 need a temporary variable during swap in Java?",
        timestamp: "2026-10-11T12:44:00Z"
      },
      {
        id: "msg-2",
        sender: "ai",
        text: `In Java, primitive assignments evaluate immediately. If you execute \`arr[j] = arr[j+1]\` directly, the original value in \`arr[j]\` is overwritten before it can be assigned to \`arr[j+1]\`.

A buffer variable holds the value safely:
\`\`\`java
int temp = arr[j];      // Preserve original value
arr[j] = arr[j + 1];   // Overwrite with next element
arr[j + 1] = temp;     // Place preserved value
\`\`\`

Alternatively, in modern languages with tuple destructuring you can swap directly, but in Java 17 primitive arrays, this three-step exchange is canonical.`,
        timestamp: "2026-10-11T12:45:00Z"
      }
    ]
  },
  {
    id: "session-102",
    title: "Java 17 Pattern Matching & Sealed Classes",
    updatedAt: "2026-10-09T16:10:00Z",
    messages: [
      {
        id: "msg-3",
        sender: "user",
        text: "Can I use pattern matching for instanceof in Java 17?",
        timestamp: "2026-10-09T16:08:00Z"
      },
      {
        id: "msg-4",
        sender: "ai",
        text: `Yes! Java 17 finalised Pattern Matching for \`instanceof\` (JEP 394). You no longer need manual casting:

\`\`\`java
if (obj instanceof String s) {
    System.out.println("String length: " + s.length());
}
\`\`\``,
        timestamp: "2026-10-09T16:10:00Z"
      }
    ]
  }
];

export function mockGetChatSessions() {
  return [...chatSessions];
}

export function mockGetChatSessionDetail(id) {
  const session = chatSessions.find(s => s.id === id);
  if (!session) {
    throw { status: 404, error: "Chat session not found" };
  }
  return { ...session };
}

export function mockAiChat({ sessionId, message, includeContext, code }) {
  if (!message || !message.trim()) {
    throw { status: 400, error: "Message cannot be empty" };
  }

  let session = chatSessions.find(s => s.id === sessionId);
  if (!session) {
    session = {
      id: `session-${Date.now()}`,
      title: message.slice(0, 32) + (message.length > 32 ? "..." : ""),
      updatedAt: new Date().toISOString(),
      messages: []
    };
    chatSessions.unshift(session);
  }

  const userMsg = {
    id: `msg-${Date.now()}-u`,
    sender: "user",
    text: message,
    timestamp: new Date().toISOString()
  };
  session.messages.push(userMsg);

  // Generate contextual AI reply
  let replyText = `I analyzed your query: "${message}". `;
  if (message.toLowerCase().includes("swap") || message.toLowerCase().includes("line 9")) {
    replyText += `The swap mechanism requires \`int temp\` as an in-memory buffer. In Java bytecode, this translates to \`ILOAD\`, \`ISTORE\` operations on the local variable array.`;
  } else if (message.toLowerCase().includes("complexity") || message.toLowerCase().includes("o(n)")) {
    replyText += `Bubble sort exhibits quadratic time complexity **O(n²)** in worst and average cases. However, with the early-exit check, an already-sorted array achieves linear time **O(n)**.`;
  } else if (message.toLowerCase().includes("null") || message.toLowerCase().includes("error")) {
    replyText += `To make your method production-safe, guard against \`null\` pointers at entry:\n\`\`\`java\nif (arr == null || arr.length <= 1) return;\n\`\`\``;
  } else {
    replyText += `In Java 17, this implementation is clean and adheres to standard DSA conventions. Consider modularizing helper procedures if expanding to dual-pivot sorting algorithms.`;
  }

  const aiMsg = {
    id: `msg-${Date.now()}-a`,
    sender: "ai",
    text: replyText,
    timestamp: new Date().toISOString()
  };
  session.messages.push(aiMsg);
  session.updatedAt = new Date().toISOString();

  return {
    sessionId: session.id,
    messageId: aiMsg.id,
    reply: replyText,
    timestamp: aiMsg.timestamp
  };
}

export function mockAiLearning({ topic = "Bubble Sort", language = "java", level = "BEGINNER" } = {}) {
  return {
    concept: {
      title: "Bubble Sort & Inversion Counting",
      category: "Comparison-Based Sorting",
      difficulty: "Beginner",
      description: "Bubble Sort is an elementary sorting algorithm that repeatedly steps through an array, compares adjacent elements, and swaps them if they are in the wrong numerical order. The pass through the list is repeated until no swaps are needed."
    },
    steps: [
      {
        step: 1,
        title: "Pass Initialisation & Setup",
        content: "We begin at index 0 and inspect the first pair (arr[0], arr[1]). We declare a boolean flag `swapped = false` before entering the inner loop to track state changes."
      },
      {
        step: 2,
        title: "Local Inversion Check & Swap",
        content: "If the left element is strictly greater than the right element (`arr[j] > arr[j + 1]`), they violate sorted order. We swap them using a temporary variable `temp` and set `swapped = true`."
      },
      {
        step: 3,
        title: "The 'Bubbling' Invariant",
        content: "After pass `i` completes, the `i-th` largest element has bubbled into its final resting position at the end of the array. Hence, the inner loop only needs to iterate up to `n - i - 1`."
      },
      {
        step: 4,
        title: "Early Termination Optimization",
        content: "If an entire pass completes without a single swap (`!swapped`), the array is already completely sorted. We break out immediately, transforming best-case time from O(n²) to O(n)."
      }
    ],
    quiz: [
      {
        id: 1,
        question: "What is the best-case time complexity of an optimized Bubble Sort on an already sorted array?",
        options: ["O(1)", "O(n)", "O(n log n)", "O(n²)"],
        answerIndex: 1,
        explanation: "Because of the `swapped` flag, the outer loop breaks after the very first pass of n - 1 comparisons when no swaps occur."
      },
      {
        id: 2,
        question: "Why is Bubble Sort classified as a 'stable' sorting algorithm?",
        options: [
          "It uses O(1) auxiliary memory.",
          "It only swaps when arr[j] > arr[j+1], keeping equal elements in their original relative positions.",
          "It has a consistent runtime across all inputs.",
          "It can be executed recursively without stack overflow."
        ],
        answerIndex: 1,
        explanation: "Because elements are only swapped when one is strictly greater than the other, two identical values maintain their original relative order."
      },
      {
        id: 3,
        question: "What is the maximum number of comparisons performed by unoptimized Bubble Sort on an array of size n?",
        options: ["n", "n(n - 1) / 2", "n²", "2ⁿ"],
        answerIndex: 1,
        explanation: "Sum of integers from 1 to n - 1 is n(n - 1) / 2 comparisons."
      }
    ],
    vivaQuestions: [
      {
        question: "Why is Bubble Sort not recommended for large datasets in production systems?",
        answer: "With a quadratic time complexity of O(n²), the number of operations grows prohibitively fast. For 100,000 elements, it requires approximately 5 billion comparisons, whereas O(n log n) algorithms like QuickSort or MergeSort require only around 1.7 million."
      },
      {
        question: "How does the 'swapped' boolean flag optimize the algorithm?",
        answer: "Without the flag, Bubble Sort always executes all n(n-1)/2 iterations even if the input is sorted. The flag detects when an entire pass caused zero inversions, indicating that the sequence is sorted and allowing an immediate O(n) exit."
      },
      {
        question: "Is Bubble Sort an in-place sorting algorithm? Why?",
        answer: "Yes, it is in-place because its auxiliary space complexity is O(1). It sorts the array by modifying elements directly within the existing allocated array buffer using only a single temporary variable."
      },
      {
        question: "How does Bubble Sort compare with Selection Sort?",
        answer: "Both have O(n²) worst-case time complexity and O(1) space. However, Bubble Sort is adaptive (O(n) on sorted data with the flag) and stable, while Selection Sort always does O(n²) comparisons regardless of input and is typically unstable."
      }
    ]
  };
}

export function mockAiCompare({ language = "java", codeA = "", codeB = "" } = {}) {
  return {
    metrics: [
      { metric: "Worst-Case Time Complexity", variantA: "O(n²)", variantB: "O(n²)" },
      { metric: "Best-Case Time Complexity", variantA: "O(n²)", variantB: "O(n)" },
      { metric: "Average-Case Time Complexity", variantA: "O(n²)", variantB: "O(n²)" },
      { metric: "Auxiliary Space Complexity", variantA: "O(1) In-Place", variantB: "O(1) In-Place" },
      { metric: "Stability Guarantee", variantA: "Stable", variantB: "Stable" },
      { metric: "Sorted Input Comparisons", variantA: "45 operations (n=10)", variantB: "9 operations (n=10)" },
      { metric: "Early Exit Capability", variantA: "No", variantB: "Yes (swapped flag)" }
    ],
    verdict: {
      winner: "Variant B (Optimized Bubble Sort)",
      summary: "Variant B improves performance drastically on partially or fully sorted arrays by incorporating an adaptive boolean swap sentinel flag.",
      recommendation: "Use Variant B for academic labs. For production collections larger than 50 elements, migrate to Dual-Pivot QuickSort or TimSort."
    }
  };
}
