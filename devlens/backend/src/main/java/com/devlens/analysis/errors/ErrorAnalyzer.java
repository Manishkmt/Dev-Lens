package com.devlens.analysis.errors;

import com.devlens.analysis.dto.ErrorAnalysisResponse;
import com.devlens.analysis.dto.IssueDto;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Rule-based static analyzer for common bugs, security hazards, and code smells
 */
@Component
public class ErrorAnalyzer {

    public ErrorAnalysisResponse analyze(CompilationUnit cu) {
        List<IssueDto> issues = new ArrayList<>();
        int[] idCounter = {1};

        // 1. String comparison using == or !=
        checkStringEquality(cu, issues, idCounter);

        // 2. Empty catch blocks
        checkEmptyCatchBlocks(cu, issues, idCounter);

        // 3. Infinite loops (while(true) without break, loop counter never modified)
        checkInfiniteLoops(cu, issues, idCounter);

        // 4. Possible null dereference & missing null guards
        checkNullDereferenceAndGuards(cu, issues, idCounter);

        // 5. Unused local variables
        checkUnusedVariables(cu, issues, idCounter);

        // 6. Unreachable statements
        checkUnreachableCode(cu, issues, idCounter);

        // 7. Array boundary risks & off-by-one errors
        checkArrayBoundaries(cu, issues, idCounter);

        // Sort issues by line number
        issues.sort(Comparator.comparingInt(IssueDto::getLine));

        int warnings = 0;
        int infos = 0;
        int errors = 0;
        for (IssueDto i : issues) {
            if ("ERROR".equalsIgnoreCase(i.getSeverity())) errors++;
            else if ("WARNING".equalsIgnoreCase(i.getSeverity())) warnings++;
            else infos++;
        }

        return ErrorAnalysisResponse.builder()
                .status("success")
                .issuesCount(issues.size())
                .warnings(warnings)
                .infos(infos)
                .errors(errors)
                .issues(issues)
                .build();
    }

    private void checkStringEquality(CompilationUnit cu, List<IssueDto> issues, int[] idCounter) {
        cu.accept(new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(BinaryExpr n, Void arg) {
                super.visit(n, arg);
                if (n.getOperator() == BinaryExpr.Operator.EQUALS || n.getOperator() == BinaryExpr.Operator.NOT_EQUALS) {
                    Expression left = n.getLeft();
                    Expression right = n.getRight();
                    boolean isStringComp = left.isStringLiteralExpr() || right.isStringLiteralExpr() ||
                            left.toString().endsWith(".toString()") || right.toString().endsWith(".toString()");

                    if (isStringComp) {
                        int line = n.getBegin().map(p -> p.line).orElse(1);
                        String op = n.getOperator() == BinaryExpr.Operator.EQUALS ? "==" : "!=";
                        String suggested = n.getOperator() == BinaryExpr.Operator.EQUALS ?
                                left + ".equals(" + right + ")" :
                                "!" + left + ".equals(" + right + ")";

                        issues.add(IssueDto.builder()
                                .id("ISSUE-" + (idCounter[0]++))
                                .type("WARNING")
                                .severity("WARNING")
                                .severityClass("badge-warning")
                                .title("String Reference Comparison with " + op)
                                .line(line)
                                .endLine(line)
                                .codeSnippet(n.toString())
                                .explanation("Strings are being compared using reference identity operator '" + op + "' instead of value equality '.equals()'.")
                                .whyItHappens("In Java, '==' compares object memory references rather than String character content. Two identical strings may evaluate to false if they reside in different heap allocations.")
                                .suggestedFix("Replace with '" + suggested + "'.")
                                .fixCode(suggested)
                                .build());
                    }
                }
            }
        }, null);
    }

    private void checkEmptyCatchBlocks(CompilationUnit cu, List<IssueDto> issues, int[] idCounter) {
        cu.accept(new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(CatchClause n, Void arg) {
                super.visit(n, arg);
                BlockStmt body = n.getBody();
                if (body.isEmpty() || body.getStatements().isEmpty()) {
                    int line = n.getBegin().map(p -> p.line).orElse(1);
                    issues.add(IssueDto.builder()
                            .id("ISSUE-" + (idCounter[0]++))
                            .type("WARNING")
                            .severity("WARNING")
                            .severityClass("badge-warning")
                            .title("Empty Catch Block")
                            .line(line)
                            .endLine(line)
                            .codeSnippet(n.getParameter().toString())
                            .explanation("Exception is caught but silently swallowed with no handling, logging, or rethrowing.")
                            .whyItHappens("Empty catch blocks conceal runtime failures, making debugging and troubleshooting upstream exceptions nearly impossible.")
                            .suggestedFix("Log the exception or throw a RuntimeException.")
                            .fixCode("e.printStackTrace(); // or logger.error(\"Operation failed\", e);")
                            .build());
                }
            }
        }, null);
    }

    private void checkInfiniteLoops(CompilationUnit cu, List<IssueDto> issues, int[] idCounter) {
        // While loops
        cu.accept(new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(WhileStmt n, Void arg) {
                super.visit(n, arg);
                Expression cond = n.getCondition();
                Statement body = n.getBody();

                // 1. while (true) without break or return
                if (cond.isBooleanLiteralExpr() && cond.asBooleanLiteralExpr().getValue()) {
                    if (!containsExitStatement(body)) {
                        int line = n.getBegin().map(p -> p.line).orElse(1);
                        issues.add(IssueDto.builder()
                                .id("ISSUE-" + (idCounter[0]++))
                                .type("ERROR")
                                .severity("ERROR")
                                .severityClass("badge-danger")
                                .title("Definite Infinite Loop")
                                .line(line)
                                .endLine(line)
                                .codeSnippet("while (true)")
                                .explanation("Loop condition is unconditionally true and the body contains no 'break', 'return', or termination branch.")
                                .whyItHappens("The loop execution thread will spin indefinitely, exhausting CPU resources and freezing the program execution.")
                                .suggestedFix("Add an explicit termination check with 'break;' or a finite condition.")
                                .fixCode("if (terminationCondition) break;")
                                .build());
                    }
                } else if (cond.isBinaryExpr()) {
                    // Check if loop condition variable is modified in body
                    Set<String> condVars = extractVariableNames(cond);
                    if (!condVars.isEmpty() && !bodyModifiesAnyVariable(body, condVars)) {
                        int line = n.getBegin().map(p -> p.line).orElse(1);
                        issues.add(IssueDto.builder()
                                .id("ISSUE-" + (idCounter[0]++))
                                .type("ERROR")
                                .severity("ERROR")
                                .severityClass("badge-danger")
                                .title("Loop Variable Never Modified")
                                .line(line)
                                .endLine(line)
                                .codeSnippet(cond.toString())
                                .explanation("Variables in the while loop condition " + condVars + " are never modified inside the loop body.")
                                .whyItHappens("If the loop condition is initially true, it will never evaluate to false, resulting in an infinite execution loop.")
                                .suggestedFix("Ensure condition variables are updated on each iteration.")
                                .build());
                    }
                }
            }
        }, null);

        // For loops
        cu.accept(new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(ForStmt n, Void arg) {
                super.visit(n, arg);
                if (n.getCompare().isEmpty() && n.getUpdate().isEmpty()) {
                    if (!containsExitStatement(n.getBody())) {
                        int line = n.getBegin().map(p -> p.line).orElse(1);
                        issues.add(IssueDto.builder()
                                .id("ISSUE-" + (idCounter[0]++))
                                .type("ERROR")
                                .severity("ERROR")
                                .severityClass("badge-danger")
                                .title("Infinite Loop in for(;;)")
                                .line(line)
                                .endLine(line)
                                .codeSnippet("for (;;)")
                                .explanation("Loop has no termination condition or update step and the body contains no break or return.")
                                .whyItHappens("The loop will execute endlessly, consuming CPU resources.")
                                .suggestedFix("Supply loop bounds or insert an exit branch.")
                                .build());
                    }
                }
            }
        }, null);
    }

    private void checkNullDereferenceAndGuards(CompilationUnit cu, List<IssueDto> issues, int[] idCounter) {
        // Method parameter null guards
        cu.accept(new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(MethodDeclaration n, Void arg) {
                super.visit(n, arg);
                if (n.getBody().isEmpty()) return;

                BlockStmt body = n.getBody().get();
                for (Parameter param : n.getParameters()) {
                    String paramName = param.getNameAsString();
                    boolean isArrayOrObject = param.getType().isArrayType() || !param.getType().isPrimitiveType();

                    if (isArrayOrObject) {
                        boolean dereferenced = isVariableDereferenced(body, paramName);
                        boolean hasNullGuard = hasNullCheckFor(body, paramName);

                        if (dereferenced && !hasNullGuard) {
                            int line = n.getBegin().map(p -> p.line).orElse(1);
                            String fix = "if (" + paramName + " == null) return;";
                            if (!n.getType().isVoidType()) {
                                fix = "if (" + paramName + " == null) return null;";
                            }

                            issues.add(IssueDto.builder()
                                    .id("ISSUE-" + (idCounter[0]++))
                                    .type("INFO")
                                    .severity("INFO")
                                    .severityClass("badge-info")
                                    .title("Potential Missing Null Guard for '" + paramName + "'")
                                    .line(line)
                                    .endLine(line)
                                    .codeSnippet(param.toString())
                                    .explanation("Method accepts reference parameter '" + paramName + "' and accesses its members without an initial null check.")
                                    .whyItHappens("If an external caller passes a null reference, accessing its fields or methods triggers a NullPointerException.")
                                    .suggestedFix("Add defensive check: '" + fix + "'.")
                                    .fixCode(fix)
                                    .build());
                        }
                    }
                }
            }
        }, null);

        // Explicit null assignments dereferenced
        cu.accept(new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(VariableDeclarator n, Void arg) {
                super.visit(n, arg);
                if (n.getInitializer().isPresent() && n.getInitializer().get().isNullLiteralExpr()) {
                    String varName = n.getNameAsString();
                    Node parent = n.getParentNode().orElse(null);
                    while (parent != null && !(parent instanceof BlockStmt)) {
                        parent = parent.getParentNode().orElse(null);
                    }
                    if (parent instanceof BlockStmt block) {
                        for (Statement stmt : block.getStatements()) {
                            if (stmt.toString().contains(varName + ".")) {
                                int line = stmt.getBegin().map(p -> p.line).orElse(1);
                                issues.add(IssueDto.builder()
                                        .id("ISSUE-" + (idCounter[0]++))
                                        .type("ERROR")
                                        .severity("ERROR")
                                        .severityClass("badge-danger")
                                        .title("Guaranteed Null Dereference on '" + varName + "'")
                                        .line(line)
                                        .endLine(line)
                                        .codeSnippet(stmt.toString().trim())
                                        .explanation("Variable '" + varName + "' is initialized to null and dereferenced without reassignment.")
                                        .whyItHappens("Invoking methods or fields on a null reference always triggers NullPointerException.")
                                        .suggestedFix("Instantiate '" + varName + "' before dereferencing it.")
                                        .build());
                                break;
                            }
                        }
                    }
                }
            }
        }, null);
    }

    private void checkUnusedVariables(CompilationUnit cu, List<IssueDto> issues, int[] idCounter) {
        cu.accept(new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(MethodDeclaration n, Void arg) {
                super.visit(n, arg);
                if (n.getBody().isEmpty()) return;

                BlockStmt body = n.getBody().get();
                List<VariableDeclarator> decls = body.findAll(VariableDeclarator.class);

                for (VariableDeclarator decl : decls) {
                    String name = decl.getNameAsString();
                    int count = 0;
                    for (SimpleName sn : body.findAll(SimpleName.class)) {
                        if (sn.asString().equals(name)) {
                            count++;
                        }
                    }

                    // Declared name itself counts as 1. If count == 1, it was never read or written again
                    if (count <= 1) {
                        int line = decl.getBegin().map(p -> p.line).orElse(1);
                        issues.add(IssueDto.builder()
                                .id("ISSUE-" + (idCounter[0]++))
                                .type("INFO")
                                .severity("INFO")
                                .severityClass("badge-info")
                                .title("Unused Local Variable '" + name + "'")
                                .line(line)
                                .endLine(line)
                                .codeSnippet(decl.toString())
                                .explanation("Variable '" + name + "' is declared but never referenced or read in this scope.")
                                .whyItHappens("Unused variables clutter source code, increase memory footprint slightly, and confuse maintenance engineers.")
                                .suggestedFix("Remove the unused variable declaration.")
                                .build());
                    }
                }
            }
        }, null);
    }

    private void checkUnreachableCode(CompilationUnit cu, List<IssueDto> issues, int[] idCounter) {
        cu.accept(new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(BlockStmt n, Void arg) {
                super.visit(n, arg);
                List<Statement> stmts = n.getStatements();
                for (int i = 0; i < stmts.size() - 1; i++) {
                    Statement current = stmts.get(i);
                    boolean isJump = current.isReturnStmt() || current.isThrowStmt() ||
                            current.isBreakStmt() || current.isContinueStmt();

                    if (isJump) {
                        Statement unreachable = stmts.get(i + 1);
                        int line = unreachable.getBegin().map(p -> p.line).orElse(1);
                        issues.add(IssueDto.builder()
                                .id("ISSUE-" + (idCounter[0]++))
                                .type("ERROR")
                                .severity("ERROR")
                                .severityClass("badge-danger")
                                .title("Unreachable Code")
                                .line(line)
                                .endLine(line)
                                .codeSnippet(unreachable.toString().trim())
                                .explanation("Statement is positioned immediately after an unconditional jump ('" + current.getClass().getSimpleName() + "') and can never be reached.")
                                .whyItHappens("Code execution halts or branches before control flow can ever reach this instruction.")
                                .suggestedFix("Remove unreachable statements or relocate them above the jump statement.")
                                .build());
                        break;
                    }
                }
            }
        }, null);
    }

    private void checkArrayBoundaries(CompilationUnit cu, List<IssueDto> issues, int[] idCounter) {
        cu.accept(new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(ForStmt n, Void arg) {
                super.visit(n, arg);
                // Check for sorting pattern: nested loop with j < n - 1 where it could be j < n - i - 1
                if (n.getParentNode().isPresent() && n.getParentNode().get() instanceof BlockStmt parentBlock) {
                    if (parentBlock.getParentNode().isPresent() && parentBlock.getParentNode().get() instanceof ForStmt outerLoop) {
                        String outerInit = outerLoop.getInitialization().toString();
                        String innerCompare = n.getCompare().map(Node::toString).orElse("");
                        String innerBody = n.getBody().toString();

                        if (innerBody.contains("[j + 1]") && innerCompare.contains("n - 1") && !innerCompare.contains("- i")) {
                            int line = n.getBegin().map(p -> p.line).orElse(1);
                            issues.add(IssueDto.builder()
                                    .id("ISSUE-" + (idCounter[0]++))
                                    .type("WARNING")
                                    .severity("WARNING")
                                    .severityClass("badge-warning")
                                    .title("Inner Loop Boundary Inefficiency")
                                    .line(line)
                                    .endLine(line)
                                    .codeSnippet(n.getCompare().map(Node::toString).orElse(""))
                                    .explanation("Inner sorting loop boundary does not subtract outer iteration variable 'i' (uses 'j < n - 1' instead of 'j < n - i - 1').")
                                    .whyItHappens("Each pass of bubble sort guarantees that the largest remaining element settles in its final position. Comparing already-sorted tail elements wastes unnecessary operations.")
                                    .suggestedFix("Change termination condition to 'j < n - i - 1'.")
                                    .fixCode("for (int j = 0; j < n - i - 1; j++) {")
                                    .build());
                        }
                    }
                }
            }
        }, null);
    }

    private boolean containsExitStatement(Statement body) {
        if (body == null) return false;
        List<BreakStmt> breaks = body.findAll(BreakStmt.class);
        List<ReturnStmt> returns = body.findAll(ReturnStmt.class);
        List<ThrowStmt> throwsStmts = body.findAll(ThrowStmt.class);
        return !breaks.isEmpty() || !returns.isEmpty() || !throwsStmts.isEmpty();
    }

    private Set<String> extractVariableNames(Expression expr) {
        Set<String> set = new HashSet<>();
        for (NameExpr ne : expr.findAll(NameExpr.class)) {
            set.add(ne.getNameAsString());
        }
        return set;
    }

    private boolean bodyModifiesAnyVariable(Statement body, Set<String> vars) {
        if (body == null) return false;
        for (AssignExpr ae : body.findAll(AssignExpr.class)) {
            if (vars.contains(ae.getTarget().toString())) return true;
        }
        for (UnaryExpr ue : body.findAll(UnaryExpr.class)) {
            if (ue.getOperator().isPostfix() || ue.getOperator().isPrefix()) {
                if (vars.contains(ue.getExpression().toString())) return true;
            }
        }
        return false;
    }

    private boolean isVariableDereferenced(BlockStmt body, String varName) {
        for (MethodCallExpr mc : body.findAll(MethodCallExpr.class)) {
            if (mc.getScope().isPresent() && mc.getScope().get().toString().equals(varName)) {
                return true;
            }
        }
        for (FieldAccessExpr fa : body.findAll(FieldAccessExpr.class)) {
            if (fa.getScope().toString().equals(varName)) {
                return true;
            }
        }
        for (ArrayAccessExpr aa : body.findAll(ArrayAccessExpr.class)) {
            if (aa.getName().toString().equals(varName)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasNullCheckFor(BlockStmt body, String varName) {
        for (IfStmt is : body.findAll(IfStmt.class)) {
            String cond = is.getCondition().toString();
            if (cond.contains(varName + " == null") || cond.contains(varName + " != null")) {
                return true;
            }
        }
        return false;
    }
}
