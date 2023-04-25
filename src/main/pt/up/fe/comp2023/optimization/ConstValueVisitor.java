package pt.up.fe.comp2023.optimization;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.JmmNodeImpl;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


public class ConstValueVisitor extends PreorderJmmVisitor<Void, Void>{
    protected boolean isOptimized;
    protected SymbolTable simpleTable;
    protected Map<String, Map<String, String>> assignMap;

    public ConstValueVisitor(SymbolTable symbolTable){
        this.simpleTable = symbolTable;
        this.isOptimized = false;
        this.assignMap = new HashMap<>();

        for (String method : simpleTable.getMethods()) {
            assignMap.put(method, new HashMap<>());
        }
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
        addVisit("Identifier", this::dealWithIdentifier);
        addVisit("This", this::defaultVisitor);
        addVisit("MethodArgs", this::defaultVisitor);
    }

    private Void defaultVisitor(JmmNode jmmNode, Void _void){
        return null;
    }

    private void replace(JmmNode node, String kind, String value){
        JmmNode newNode = new JmmNodeImpl(kind);
        newNode.put("value", value);
        node.replace(newNode);
    }

    private Void dealWithIdentifier(JmmNode node, Void unused) {
        Optional<JmmNode> ancestor = node.getAncestor("Method");
        String method = "main";
        if (ancestor.isPresent()) {
            method = ancestor.get().get("name");
        }
        if (assignMap.get(method).containsKey(node.get("value"))) {
           // replace this node with its constant value
            String kind = "Integer";
            String value = assignMap.get(method).get(node.get("value"));
            try {
                int val = Integer.parseInt(value);
            }
            catch (Exception e) {
                kind = "BoolLiteral";
            }
            replace(node, kind, value);
            isOptimized = true;
        }

        return null;
    }

    private Void dealWithAssignment(JmmNode node, Void unused) {
        // check if var belongs to local variables
        boolean isLocal = false;
        Optional<JmmNode> ancestor = node.getAncestor("Method");
        String method = "main";
        if (ancestor.isPresent()){
            method = ancestor.get().get("name");
        }

        for (Symbol symb : simpleTable.getLocalVariables(method)) {
            if (symb.getName().equals(node.get("var"))) {
                isLocal = true;
                break;
            }
        }

        if (isLocal && (node.getJmmChild(0).getKind().equals("Integer") || node.getJmmChild(0).getKind().equals("BoolLiteral"))) {
            assignMap.get(method).put(node.get("var"), node.getJmmChild(0).get("value"));
        }

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
