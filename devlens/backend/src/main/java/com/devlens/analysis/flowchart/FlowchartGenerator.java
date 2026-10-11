package com.devlens.analysis.flowchart;

import com.devlens.analysis.dto.FlowchartAnalysisResponse;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds Control-Flow Graphs (CFG) from AST MethodDeclarations
 * and converts them into structured node/edge models and Mermaid diagram text.
 */
@Component
public class FlowchartGenerator {

    public FlowchartAnalysisResponse generate(CompilationUnit cu) {
        List<MethodDeclaration> allMethods = cu.findAll(MethodDeclaration.class);
        List<String> methodNames = new ArrayList<>();
        MethodDeclaration selectedMethod = null;

        for (MethodDeclaration m : allMethods) {
            String name = m.getNameAsString();
            methodNames.add(name);
            if (!name.equals("main") && selectedMethod == null) {
                selectedMethod = m;
            }
        }

        if (selectedMethod == null && !allMethods.isEmpty()) {
            selectedMethod = allMethods.get(0);
        }

        if (selectedMethod == null || selectedMethod.getBody().isEmpty()) {
            return defaultEmptyFlowchart();
        }

        return generateForMethod(selectedMethod, methodNames);
    }

    private FlowchartAnalysisResponse generateForMethod(MethodDeclaration method, List<String> methods) {
        String methodName = method.getNameAsString();
        List<FlowchartAnalysisResponse.FlowNodeDto> nodes = new ArrayList<>();
        List<FlowchartAnalysisResponse.FlowEdgeDto> edges = new ArrayList<>();
        StringBuilder mermaid = new StringBuilder("graph TD\n");

        int[] counter = {1};

        // 1. Start Node
        String startId = "N" + (counter[0]++);
        String startLabel = "Start: " + methodName;
        nodes.add(FlowchartAnalysisResponse.FlowNodeDto.builder()
                .id(startId)
                .label(startLabel)
                .type("terminal")
                .line(method.getBegin().map(p -> p.line).orElse(1))
                .build());
        mermaid.append("    ").append(startId).append("([\"").append(startLabel).append("\"])\n");

        // 2. End Node
        String endId = "Z_END";
        String endLabel = "End: " + methodName;

        String prevId = startId;

        // Process statements in block
        BlockStmt body = method.getBody().get();
        for (Statement stmt : body.getStatements()) {
            prevId = processStatement(stmt, prevId, endId, nodes, edges, mermaid, counter);
        }

        // Connect last node to End
        if (!prevId.equals(endId)) {
            edges.add(FlowchartAnalysisResponse.FlowEdgeDto.builder()
                    .from(prevId)
                    .to(endId)
                    .label("")
                    .build());
            mermaid.append("    ").append(prevId).append(" --> ").append(endId).append("\n");
        }

        nodes.add(FlowchartAnalysisResponse.FlowNodeDto.builder()
                .id(endId)
                .label(endLabel)
                .type("terminal")
                .line(method.getEnd().map(p -> p.line).orElse(1))
                .build());
        mermaid.append("    ").append(endId).append("([\"").append(endLabel).append("\"])");

        return FlowchartAnalysisResponse.builder()
                .mermaid(mermaid.toString())
                .nodes(nodes)
                .edges(edges)
                .selectedMethod(methodName)
                .methods(methods)
                .build();
    }

    private String processStatement(
            Statement stmt,
            String prevId,
            String endId,
            List<FlowchartAnalysisResponse.FlowNodeDto> nodes,
            List<FlowchartAnalysisResponse.FlowEdgeDto> edges,
            StringBuilder mermaid,
            int[] counter) {

        int line = stmt.getBegin().map(p -> p.line).orElse(1);

        if (stmt.isForStmt() || stmt.isWhileStmt()) {
            String loopId = "N" + (counter[0]++);
            String conditionText = stmt.isForStmt() ?
                    stmt.asForStmt().getCompare().map(Object::toString).orElse("For Loop") :
                    stmt.asWhileStmt().getCondition().toString();

            String loopLabel = "Loop Condition: " + sanitize(conditionText);
            nodes.add(FlowchartAnalysisResponse.FlowNodeDto.builder()
                    .id(loopId)
                    .label(loopLabel)
                    .type("loop")
                    .line(line)
                    .build());
            mermaid.append("    ").append(loopId).append("{\"").append(loopLabel).append("\"}\n");

            // Edge from prev to loop condition
            edges.add(FlowchartAnalysisResponse.FlowEdgeDto.builder().from(prevId).to(loopId).label("").build());
            mermaid.append("    ").append(prevId).append(" --> ").append(loopId).append("\n");

            // Process loop body
            Statement loopBody = stmt.isForStmt() ? stmt.asForStmt().getBody() : stmt.asWhileStmt().getBody();
            String bodyLastId = loopId;

            if (loopBody.isBlockStmt()) {
                String subPrev = loopId;
                boolean isFirstInLoop = true;
                for (Statement subStmt : loopBody.asBlockStmt().getStatements()) {
                    if (isFirstInLoop) {
                        subPrev = processSubStatement(subStmt, subPrev, loopId, nodes, edges, mermaid, counter, "Yes");
                        isFirstInLoop = false;
                    } else {
                        subPrev = processStatement(subStmt, subPrev, endId, nodes, edges, mermaid, counter);
                    }
                }
                bodyLastId = subPrev;
            } else {
                bodyLastId = processSubStatement(loopBody, loopId, loopId, nodes, edges, mermaid, counter, "Yes");
            }

            // Loop back edge
            if (!bodyLastId.equals(endId)) {
                edges.add(FlowchartAnalysisResponse.FlowEdgeDto.builder().from(bodyLastId).to(loopId).label("Repeat").build());
                mermaid.append("    ").append(bodyLastId).append(" -- Repeat --> ").append(loopId).append("\n");
            }

            // Exit node
            String exitId = "N" + (counter[0]++);
            String exitLabel = "Exit Loop";
            nodes.add(FlowchartAnalysisResponse.FlowNodeDto.builder()
                    .id(exitId)
                    .label(exitLabel)
                    .type("action")
                    .line(line)
                    .build());
            mermaid.append("    ").append(exitId).append("[\"").append(exitLabel).append("\"]\n");

            edges.add(FlowchartAnalysisResponse.FlowEdgeDto.builder().from(loopId).to(exitId).label("No").build());
            mermaid.append("    ").append(loopId).append(" -- No --> ").append(exitId).append("\n");

            return exitId;

        } else if (stmt.isIfStmt()) {
            IfStmt ifStmt = stmt.asIfStmt();
            String ifId = "N" + (counter[0]++);
            String ifLabel = "Check: " + sanitize(ifStmt.getCondition().toString());

            nodes.add(FlowchartAnalysisResponse.FlowNodeDto.builder()
                    .id(ifId)
                    .label(ifLabel)
                    .type("decision")
                    .line(line)
                    .build());
            mermaid.append("    ").append(ifId).append("{\"").append(ifLabel).append("\"}\n");

            edges.add(FlowchartAnalysisResponse.FlowEdgeDto.builder().from(prevId).to(ifId).label("").build());
            mermaid.append("    ").append(prevId).append(" --> ").append(ifId).append("\n");

            // Then branch
            String thenLast = processSubStatement(ifStmt.getThenStmt(), ifId, endId, nodes, edges, mermaid, counter, "Yes");

            // Merge node
            String mergeId = "N" + (counter[0]++);
            String mergeLabel = "Merge Path";
            nodes.add(FlowchartAnalysisResponse.FlowNodeDto.builder().id(mergeId).label(mergeLabel).type("action").line(line).build());
            mermaid.append("    ").append(mergeId).append("[\"").append(mergeLabel).append("\"]\n");

            if (!thenLast.equals(endId)) {
                edges.add(FlowchartAnalysisResponse.FlowEdgeDto.builder().from(thenLast).to(mergeId).label("").build());
                mermaid.append("    ").append(thenLast).append(" --> ").append(mergeId).append("\n");
            }

            if (ifStmt.getElseStmt().isPresent()) {
                String elseLast = processSubStatement(ifStmt.getElseStmt().get(), ifId, endId, nodes, edges, mermaid, counter, "No");
                if (!elseLast.equals(endId)) {
                    edges.add(FlowchartAnalysisResponse.FlowEdgeDto.builder().from(elseLast).to(mergeId).label("").build());
                    mermaid.append("    ").append(elseLast).append(" --> ").append(mergeId).append("\n");
                }
            } else {
                edges.add(FlowchartAnalysisResponse.FlowEdgeDto.builder().from(ifId).to(mergeId).label("No").build());
                mermaid.append("    ").append(ifId).append(" -- No --> ").append(mergeId).append("\n");
            }

            return mergeId;

        } else if (stmt.isReturnStmt()) {
            String retId = "N" + (counter[0]++);
            String retLabel = "Return: " + sanitize(stmt.toString().trim());

            nodes.add(FlowchartAnalysisResponse.FlowNodeDto.builder().id(retId).label(retLabel).type("terminal").line(line).build());
            mermaid.append("    ").append(retId).append("([\"").append(retLabel).append("\"])\n");

            edges.add(FlowchartAnalysisResponse.FlowEdgeDto.builder().from(prevId).to(retId).label("").build());
            mermaid.append("    ").append(prevId).append(" --> ").append(retId).append("\n");

            edges.add(FlowchartAnalysisResponse.FlowEdgeDto.builder().from(retId).to(endId).label("").build());
            mermaid.append("    ").append(retId).append(" --> ").append(endId).append("\n");

            return endId;

        } else {
            // General action / statement
            String actId = "N" + (counter[0]++);
            String actLabel = sanitize(stmt.toString().trim().replaceAll(";", ""));
            if (actLabel.length() > 45) {
                actLabel = actLabel.substring(0, 42) + "...";
            }

            nodes.add(FlowchartAnalysisResponse.FlowNodeDto.builder().id(actId).label(actLabel).type("action").line(line).build());
            mermaid.append("    ").append(actId).append("[\"").append(actLabel).append("\"]\n");

            edges.add(FlowchartAnalysisResponse.FlowEdgeDto.builder().from(prevId).to(actId).label("").build());
            mermaid.append("    ").append(prevId).append(" --> ").append(actId).append("\n");

            return actId;
        }
    }

    private String processSubStatement(
            Statement stmt,
            String sourceId,
            String endId,
            List<FlowchartAnalysisResponse.FlowNodeDto> nodes,
            List<FlowchartAnalysisResponse.FlowEdgeDto> edges,
            StringBuilder mermaid,
            int[] counter,
            String edgeLabel) {

        int line = stmt.getBegin().map(p -> p.line).orElse(1);
        String actId = "N" + (counter[0]++);
        String actLabel = sanitize(stmt.toString().trim().replaceAll(";", ""));
        if (actLabel.length() > 45) {
            actLabel = actLabel.substring(0, 42) + "...";
        }

        nodes.add(FlowchartAnalysisResponse.FlowNodeDto.builder().id(actId).label(actLabel).type("action").line(line).build());
        mermaid.append("    ").append(actId).append("[\"").append(actLabel).append("\"]\n");

        edges.add(FlowchartAnalysisResponse.FlowEdgeDto.builder().from(sourceId).to(actId).label(edgeLabel).build());
        mermaid.append("    ").append(sourceId).append(" -- ").append(edgeLabel).append(" --> ").append(actId).append("\n");

        return actId;
    }

    private String sanitize(String str) {
        if (str == null) return "";
        return str.replaceAll("[\"\n\r]", " ").trim();
    }

    private FlowchartAnalysisResponse defaultEmptyFlowchart() {
        return FlowchartAnalysisResponse.builder()
                .mermaid("graph TD\n    A([Start]) --> B[Process] --> C([End])")
                .nodes(List.of(
                        FlowchartAnalysisResponse.FlowNodeDto.builder().id("A").label("Start").type("terminal").line(1).build(),
                        FlowchartAnalysisResponse.FlowNodeDto.builder().id("B").label("Process").type("action").line(1).build(),
                        FlowchartAnalysisResponse.FlowNodeDto.builder().id("C").label("End").type("terminal").line(1).build()
                ))
                .edges(List.of(
                        FlowchartAnalysisResponse.FlowEdgeDto.builder().from("A").to("B").label("").build(),
                        FlowchartAnalysisResponse.FlowEdgeDto.builder().from("B").to("C").label("").build()
                ))
                .selectedMethod("default")
                .methods(List.of("default"))
                .build();
    }
}
