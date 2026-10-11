/**
 * DevLens Mock Project Files Module
 */

let initialFiles = [
  {
    id: 101,
    projectId: 1,
    name: "BubbleSort.java",
    path: "src/algorithms/BubbleSort.java",
    size: 756,
    qualityScore: 88,
    issuesCount: 2,
    content: `package com.algorithms;

/**
 * Bubble Sort algorithm implementation with early termination optimization.
 */
public class BubbleSort {
    public static void bubbleSort(int[] arr) {
        if (arr == null || arr.length <= 1) return;
        
        int n = arr.length;
        boolean swapped;
        for (int i = 0; i < n - 1; i++) {
            swapped = false;
            for (int j = 0; j < n - i - 1; j++) {
                if (arr[j] > arr[j + 1]) {
                    // Swap adjacent elements
                    int temp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = temp;
                    swapped = true;
                }
            }
            // If no elements were swapped, break early
            if (!swapped) break;
        }
    }
}`,
    lastAnalyzed: "2026-10-10T14:20:00Z"
  },
  {
    id: 102,
    projectId: 1,
    name: "QuickSort.java",
    path: "src/algorithms/QuickSort.java",
    size: 1240,
    qualityScore: 96,
    issuesCount: 0,
    content: `package com.algorithms;

public class QuickSort {
    public static void quickSort(int[] arr, int low, int high) {
        if (low < high) {
            int pi = partition(arr, low, high);
            quickSort(arr, low, pi - 1);
            quickSort(arr, pi + 1, high);
        }
    }

    private static int partition(int[] arr, int low, int high) {
        int pivot = arr[high];
        int i = (low - 1);
        for (int j = low; j < high; j++) {
            if (arr[j] <= pivot) {
                i++;
                int temp = arr[i];
                arr[i] = arr[j];
                arr[j] = temp;
            }
        }
        int temp = arr[i + 1];
        arr[i + 1] = arr[high];
        arr[high] = temp;
        return i + 1;
    }
}`,
    lastAnalyzed: "2026-10-10T14:22:00Z"
  },
  {
    id: 103,
    projectId: 1,
    name: "BinarySearchTree.java",
    path: "src/structures/BinarySearchTree.java",
    size: 1680,
    qualityScore: 91,
    issuesCount: 1,
    content: `package com.structures;

public class BinarySearchTree {
    static class Node {
        int key;
        Node left, right;
        public Node(int item) { key = item; }
    }
    Node root;

    public void insert(int key) {
        root = insertRec(root, key);
    }

    private Node insertRec(Node root, int key) {
        if (root == null) return new Node(key);
        if (key < root.key) root.left = insertRec(root.left, key);
        else if (key > root.key) root.right = insertRec(root.right, key);
        return root;
    }
}`,
    lastAnalyzed: "2026-10-09T16:15:00Z"
  },
  {
    id: 104,
    projectId: 1,
    name: "DijkstraGraph.java",
    path: "src/graph/DijkstraGraph.java",
    size: 2150,
    qualityScore: 89,
    issuesCount: 1,
    content: `package com.graph;
import java.util.*;

public class DijkstraGraph {
    private int V;
    private List<List<Node>> adj;

    static class Node implements Comparable<Node> {
        int target, cost;
        Node(int target, int cost) { this.target = target; this.cost = cost; }
        public int compareTo(Node o) { return Integer.compare(this.cost, o.cost); }
    }
}`,
    lastAnalyzed: "2026-10-08T11:45:00Z"
  }
];

export function mockGetProjectFiles(projectId) {
  return initialFiles.filter(f => f.projectId === Number(projectId));
}

export function mockGetFile(id) {
  const file = initialFiles.find(f => f.id === Number(id));
  if (!file) {
    throw { status: 404, error: "File not found" };
  }
  return { ...file };
}

export function mockAddFile(projectId, { name, path, content }) {
  if (!name || !name.trim()) {
    throw {
      status: 400,
      error: "Validation failed",
      fields: { name: "File name is required" }
    };
  }

  const newFile = {
    id: Math.max(...initialFiles.map(f => f.id), 100) + 1,
    projectId: Number(projectId),
    name: name.trim(),
    path: path || `src/${name.trim()}`,
    size: content ? content.length : 0,
    qualityScore: 85,
    issuesCount: 0,
    content: content || "// New file content",
    lastAnalyzed: new Date().toISOString()
  };

  initialFiles.push(newFile);
  return newFile;
}

export function mockUpdateFile(id, { content, name }) {
  const index = initialFiles.findIndex(f => f.id === Number(id));
  if (index === -1) {
    throw { status: 404, error: "File not found" };
  }

  if (content !== undefined) {
    initialFiles[index].content = content;
    initialFiles[index].size = content.length;
  }
  if (name) initialFiles[index].name = name;
  initialFiles[index].lastAnalyzed = new Date().toISOString();

  return { ...initialFiles[index] };
}

export function mockDeleteFile(id) {
  const index = initialFiles.findIndex(f => f.id === Number(id));
  if (index === -1) {
    throw { status: 404, error: "File not found" };
  }
  initialFiles.splice(index, 1);
  return { success: true };
}
