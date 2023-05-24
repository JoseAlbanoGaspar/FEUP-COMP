package pt.up.fe.comp2023.optimization;

import pt.up.fe.comp.jmm.ast.JmmNode;

import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;

import java.util.*;


public class WhileInfo extends PreorderJmmVisitor<Void, Void>{

    protected List<Set<String>> whileData;
    protected int w_idx;

    protected List<List<Set<String>>> ifData;
    protected int if_idx;

    protected final int IF = 0;
    protected final int ELSE = 1;

    public WhileInfo(){
        this.whileData = new ArrayList<>();
        this.w_idx = -1;
        this.ifData = new ArrayList<>();
        this.if_idx = -1;
    }

    @Override
    protected void buildVisitor() {
        addVisit("Program", this::defaultVisitor);
        addVisit("ImportDeclaration", this::defaultVisitor);
        addVisit("ClassDeclaration", this::defaultVisitor);
        addVisit("VarDeclaration", this::defaultVisitor);
        addVisit("MainMethod", this::defaultVisitor);
        addVisit("Method", this::defaultVisitor);
        addVisit("Type", this::defaultVisitor);
        addVisit("BlockCode", this::defaultVisitor);
        addVisit("If",this::dealWithIf);
        addVisit("While", this::dealWithWhile);
        addVisit("StatementExpression", this::defaultVisitor);
        addVisit("Assignment", this::dealWithAssignment);
        addVisit("Array", this::defaultVisitor);
        addVisit("Not", this::defaultVisitor);
        addVisit("Parenthesis", this::defaultVisitor);
        addVisit("BinaryOp", this::defaultVisitor);
        addVisit("Compare", this::defaultVisitor);
        addVisit("LogicalAnd", this::defaultVisitor);
        addVisit("SquareBrackets", this::defaultVisitor);
        addVisit("Length", this::defaultVisitor);
        addVisit("FunctionCall", this::defaultVisitor);
        addVisit("NewArray", this::defaultVisitor);
        addVisit("NewClass", this::defaultVisitor);
        addVisit("Integer", this::defaultVisitor);
        addVisit("BoolLiteral", this::defaultVisitor);
        addVisit("Identifier", this::defaultVisitor);
        addVisit("This", this::defaultVisitor);
        addVisit("MethodArgs", this::defaultVisitor);
    }

    private Void dealWithIf(JmmNode node, Void unused) {
        this.ifData.add(List.of(new HashSet<>(), new HashSet<>() ));
        this.if_idx++;

        // this node is an assign inside an if clause
        for(int i = 0; i < 2; i++) {
            JmmNode _if = node.getJmmChild(i + 1);
            System.out.println(_if);
            if (_if.getKind().equals("BlockCode")) {
                System.out.println("here");

                for (JmmNode statement : _if.getChildren()) {
                    if (statement.getKind().equals("Assignment")) {
                        this.ifData.get(this.if_idx).get(i).add(statement.get("var"));
                    }
                }

            }
        }

        return null;
    }

    private Void dealWithWhile(JmmNode jmmNode, Void unused) {
        this.whileData.add(new HashSet<>());
        this.w_idx++;
        return null;
    }



    private Void dealWithAssignment(JmmNode node, Void unused) {
        Optional<JmmNode> whileAncestor = node.getAncestor("While");
        if (whileAncestor.isPresent()) {
            // this node is an assign inside a while loop
            this.whileData.get(this.w_idx).add(node.get("var"));
        }

        return null;
    }

    private Void defaultVisitor(JmmNode node, Void unused) {
        return null;
    }

    public List<Set<String>> getWhileData() {
        return this.whileData;
    }

    public List<List<Set<String>>> getIfData() {
        return this.ifData;
    }

    @Override
    public Void visit(JmmNode jmmNode) {
        return super.visit(jmmNode);
    }

}