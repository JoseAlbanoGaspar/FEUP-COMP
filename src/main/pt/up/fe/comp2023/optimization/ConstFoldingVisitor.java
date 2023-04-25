package pt.up.fe.comp2023.optimization;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.JmmNodeImpl;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;


public class ConstFoldingVisitor extends PreorderJmmVisitor<Void, Void>{
    protected boolean isOptimized;

    public ConstFoldingVisitor(){
        this.isOptimized = false;
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
        addVisit("If",this::defaultVisitor);
        addVisit("While", this::defaultVisitor);
        addVisit("StatementExpression", this::defaultVisitor);
        addVisit("Assignment", this::defaultVisitor);
        addVisit("Array", this::defaultVisitor);
        addVisit("Not", this::dealWithNot);
        addVisit("Parenthesis", this::dealWithParenthesis);
        addVisit("BinaryOp", this::dealWithBinaryOp);
        addVisit("Compare", this::dealWithCompare);
        addVisit("LogicalAnd", this::dealWithLogicalAnd);
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

    private void replace(JmmNode node, String kind, String value){
        JmmNode newNode = new JmmNodeImpl(kind);
        newNode.put("value", value);
        node.replace(newNode);
    }

    private Void dealWithLogicalAnd(JmmNode node, Void unused) {
        if (node.getJmmChild(0).getKind().equals("BoolLiteral") &&
                node.getJmmChild(1).getKind().equals("BoolLiteral")) {
            // Compute new value
            String value = Boolean.toString(Boolean.parseBoolean(node.getJmmChild(0).get("value")) && Boolean.parseBoolean(node.getJmmChild(1).get("value")));
            // Replaces this node with a new one
            replace(node, "BoolLiteral", value);
            isOptimized = true;
        }
        return null;
    }

    private Void dealWithCompare(JmmNode node, Void unused) {
        if (node.getJmmChild(0).getKind().equals("Integer") &&
                node.getJmmChild(1).getKind().equals("Integer")) {
            // Compute new value
            String value = Boolean.toString(Integer.parseInt(node.getJmmChild(0).get("value")) < Integer.parseInt(node.getJmmChild(1).get("value")));
            // Replaces this node with a new one
            replace(node, "BoolLiteral", value);
            isOptimized = true;
        }
        return null;
    }

    private Void dealWithBinaryOp(JmmNode node, Void unused) {
        if (node.getJmmChild(0).getKind().equals("Integer") &&
                node.getJmmChild(1).getKind().equals("Integer")) {
            // Compute new value
            int left = Integer.parseInt(node.getJmmChild(0).get("value"));
            int right = Integer.parseInt(node.getJmmChild(1).get("value"));
            String value = "";
            switch (node.get("op")) {
                case "+":
                    value = String.valueOf(left + right);
                    break;
                case "-":
                    value = String.valueOf(left - right);
                    break;
                case "*":
                    value = String.valueOf(left * right);
                    break;
                case "/":
                    value = String.valueOf(left / right);
                    break;
            }
            // Replaces this node with a new one
            replace(node, "Integer", value);
            isOptimized = true;
            System.out.println("binOp");
        }
        return null;
    }

    private Void dealWithParenthesis(JmmNode node, Void unused) {
        if (node.getJmmChild(0).getKind().equals("Integer") || node.getJmmChild(0).getKind().equals("BoolLiteral")) {
            JmmNode parent = node.getJmmParent();
            int idx = node.getIndexOfSelf();
            JmmNode expression = node.getJmmChild(0);
            parent.setChild(expression, idx);
            node.delete();
            isOptimized = true;
            System.out.println("parenthesis");
        }
        return null;
    }

    private Void dealWithNot(JmmNode node, Void unused) {
        if (node.getJmmChild(0).getKind().equals("BoolLiteral")) {
            // Compute new value
            String value = Boolean.toString(!Boolean.parseBoolean(node.getJmmChild(0).get("value")));
            // Replaces this node with a new one
            replace(node, "BoolLiteral", value);
            isOptimized = true;
        }
        return null;
    }

    private Void defaultVisitor(JmmNode node, Void _void){
        return null;
    }

    public boolean wasOptimized(){
        if (isOptimized) {
            isOptimized = false;
            return true;
        }
        return false;
    }

    @Override
    public Void visit(JmmNode jmmNode) {
        return super.visit(jmmNode);
    }
}
