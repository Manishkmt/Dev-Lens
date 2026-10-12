package com.devlens.common.config;

import com.devlens.analysis.entity.Analysis;
import com.devlens.analysis.entity.AnalysisType;
import com.devlens.analysis.repository.AnalysisRepository;
import com.devlens.project.entity.Project;
import com.devlens.project.entity.SourceFile;
import com.devlens.project.repository.ProjectRepository;
import com.devlens.project.repository.SourceFileRepository;
import com.devlens.user.entity.User;
import com.devlens.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Seeds demo user (demo@devlens.com / demo123) and sample projects/files/analyses
 * only when the users database table is empty.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DemoDataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final SourceFileRepository sourceFileRepository;
    private final AnalysisRepository analysisRepository;
    private final com.devlens.ai.repository.ChatSessionRepository chatSessionRepository;
    private final com.devlens.ai.repository.ChatMessageRepository chatMessageRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Database already contains users. Skipping demo data seeding.");
            return;
        }

        log.info("Seeding initial demo user (demo@devlens.com / demo123) and sample workspace...");

        // 1. Create Demo User
        User demoUser = User.builder()
                .name("Alex Chen")
                .email("demo@devlens.com")
                .password(passwordEncoder.encode("demo123"))
                .role("USER")
                .defaultLevel("INTERMEDIATE")
                .theme("dark")
                .build();
        demoUser = userRepository.save(demoUser);

        // 2. Create Project 1: Data Structures
        Project project1 = Project.builder()
                .name("CS201: Data Structures & Algorithms")
                .description("Core curriculum benchmark with AVL Trees, Dijkstra graph traversal, and dual-pivot quicksort.")
                .language("java")
                .owner(demoUser)
                .build();
        project1 = projectRepository.save(project1);

        String bubbleSortCode = "public class BubbleSort {\n" +
                "    public static void bubbleSort(int[] arr) {\n" +
                "        int n = arr.length;\n" +
                "        boolean swapped;\n" +
                "        for (int i = 0; i < n - 1; i++) {\n" +
                "            swapped = false;\n" +
                "            for (int j = 0; j < n - i - 1; j++) {\n" +
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

        SourceFile file1 = SourceFile.builder()
                .fileName("BubbleSort.java")
                .path("src/algorithms/BubbleSort.java")
                .language("java")
                .content(bubbleSortCode)
                .qualityScore(88)
                .issuesCount(2)
                .project(project1)
                .build();
        file1 = sourceFileRepository.save(file1);

        String quickSortCode = "public class QuickSort {\n" +
                "    public static void quickSort(int[] arr, int low, int high) {\n" +
                "        if (low < high) {\n" +
                "            int pi = partition(arr, low, high);\n" +
                "            quickSort(arr, low, pi - 1);\n" +
                "            quickSort(arr, pi + 1, high);\n" +
                "        }\n" +
                "    }\n" +
                "    private static int partition(int[] arr, int low, int high) {\n" +
                "        int pivot = arr[high];\n" +
                "        int i = low - 1;\n" +
                "        for (int j = low; j < high; j++) {\n" +
                "            if (arr[j] < pivot) {\n" +
                "                i++;\n" +
                "                int temp = arr[i]; arr[i] = arr[j]; arr[j] = temp;\n" +
                "            }\n" +
                "        }\n" +
                "        int temp = arr[i + 1]; arr[i + 1] = arr[high]; arr[high] = temp;\n" +
                "        return i + 1;\n" +
                "    }\n" +
                "}";

        SourceFile file2 = SourceFile.builder()
                .fileName("QuickSort.java")
                .path("src/algorithms/QuickSort.java")
                .language("java")
                .content(quickSortCode)
                .qualityScore(96)
                .issuesCount(0)
                .project(project1)
                .build();
        file2 = sourceFileRepository.save(file2);

        // 3. Create Project 2: Distributed Task Scheduler
        Project project2 = Project.builder()
                .name("Distributed Task Scheduler")
                .description("Thread pool executor and priority queue implementation in Java with bounded buffers.")
                .language("java")
                .owner(demoUser)
                .build();
        project2 = projectRepository.save(project2);

        String taskWorkerCode = "import java.util.concurrent.BlockingQueue;\n\n" +
                "public class TaskWorker implements Runnable {\n" +
                "    private final BlockingQueue<Runnable> queue;\n" +
                "    public TaskWorker(BlockingQueue<Runnable> q) { this.queue = q; }\n" +
                "    public void run() {\n" +
                "        try {\n" +
                "            while (!Thread.currentThread().isInterrupted()) {\n" +
                "                Runnable task = queue.take();\n" +
                "                task.run();\n" +
                "            }\n" +
                "        } catch (InterruptedException e) {\n" +
                "            Thread.currentThread().interrupt();\n" +
                "        }\n" +
                "    }\n" +
                "}";

        SourceFile file3 = SourceFile.builder()
                .fileName("TaskWorker.java")
                .path("src/concurrency/TaskWorker.java")
                .language("java")
                .content(taskWorkerCode)
                .qualityScore(86)
                .issuesCount(1)
                .project(project2)
                .build();
        file3 = sourceFileRepository.save(file3);

        // 4. Create Sample Analyses
        Analysis analysis1 = Analysis.builder()
                .user(demoUser)
                .project(project1)
                .sourceFile(file1)
                .codeSnapshot(bubbleSortCode)
                .language("java")
                .type(AnalysisType.COMPREHENSIVE)
                .qualityScore(88)
                .notes("Optimal early exit verified for CS201 lab.")
                .resultJson("{\"qualityScore\":88,\"issuesCount\":2,\"summary\":\"Bubble sort analyzed with quadratic worst-case and linear best-case.\"}")
                .build();
        analysisRepository.save(analysis1);

        Analysis analysis2 = Analysis.builder()
                .user(demoUser)
                .project(project1)
                .sourceFile(file2)
                .codeSnapshot(quickSortCode)
                .language("java")
                .type(AnalysisType.COMPLEXITY)
                .qualityScore(96)
                .notes("Partition verified O(n log n).")
                .resultJson("{\"qualityScore\":96,\"issuesCount\":0,\"summary\":\"Dual-pivot recursive partitioning achieves average O(n log n).\"}")
                .build();
        analysisRepository.save(analysis2);

        Analysis analysis3 = Analysis.builder()
                .user(demoUser)
                .project(project2)
                .sourceFile(file3)
                .codeSnapshot(taskWorkerCode)
                .language("java")
                .type(AnalysisType.REVIEW)
                .qualityScore(86)
                .notes("Verified clean thread interruption handling.")
                .resultJson("{\"qualityScore\":86,\"issuesCount\":1,\"summary\":\"Reliable queue consumer loop properly re-asserts thread interruption.\"}")
                .build();
        analysisRepository.save(analysis3);

        // 6. Seed Demo AI Chat Sessions & Messages
        com.devlens.ai.entity.ChatSession chatSession1 = com.devlens.ai.entity.ChatSession.builder()
                .id("session-101")
                .title("BubbleSort Inversion & Optimization")
                .user(demoUser)
                .createdAt(LocalDateTime.now().minusHours(2))
                .updatedAt(LocalDateTime.now().minusHours(1))
                .build();
        chatSessionRepository.save(chatSession1);

        com.devlens.ai.entity.ChatMessage msg1 = com.devlens.ai.entity.ChatMessage.builder()
                .id("msg-1")
                .session(chatSession1)
                .sender("user")
                .text("Why does line 9 need a temporary variable during swap in Java?")
                .timestamp(LocalDateTime.now().minusMinutes(50))
                .build();
        chatMessageRepository.save(msg1);

        com.devlens.ai.entity.ChatMessage msg2 = com.devlens.ai.entity.ChatMessage.builder()
                .id("msg-2")
                .session(chatSession1)
                .sender("ai")
                .text("In Java, primitive assignments evaluate immediately. If you execute `arr[j] = arr[j+1]` directly, the original value in `arr[j]` is overwritten before it can be assigned to `arr[j+1]`.\n\nA buffer variable holds the value safely:\n```java\nint temp = arr[j];      // Preserve original value\narr[j] = arr[j + 1];   // Overwrite with next element\narr[j + 1] = temp;     // Place preserved value\n```\n\nIn Java 17 primitive arrays, this three-step exchange is canonical.")
                .timestamp(LocalDateTime.now().minusMinutes(49))
                .build();
        chatMessageRepository.save(msg2);

        com.devlens.ai.entity.ChatSession chatSession2 = com.devlens.ai.entity.ChatSession.builder()
                .id("session-102")
                .title("Java 17 Pattern Matching & Sealed Classes")
                .user(demoUser)
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now().minusDays(1))
                .build();
        chatSessionRepository.save(chatSession2);

        com.devlens.ai.entity.ChatMessage msg3 = com.devlens.ai.entity.ChatMessage.builder()
                .id("msg-3")
                .session(chatSession2)
                .sender("user")
                .text("Can I use pattern matching for instanceof in Java 17?")
                .timestamp(LocalDateTime.now().minusDays(1))
                .build();
        chatMessageRepository.save(msg3);

        com.devlens.ai.entity.ChatMessage msg4 = com.devlens.ai.entity.ChatMessage.builder()
                .id("msg-4")
                .session(chatSession2)
                .sender("ai")
                .text("Yes! Java 17 finalised Pattern Matching for `instanceof` (JEP 394). You no longer need manual casting:\n\n```java\nif (obj instanceof String s) {\n    System.out.println(\"String length: \" + s.length());\n}\n```")
                .timestamp(LocalDateTime.now().minusDays(1))
                .build();
        chatMessageRepository.save(msg4);

        log.info("Demo data seeding complete! Demo credentials: demo@devlens.com / demo123");
    }
}
