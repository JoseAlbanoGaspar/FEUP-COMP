package pt.up.fe.comp2023.optimization;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.JmmNodeImpl;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;

import java.util.*;


public class ASToptimization extends PreorderJmmVisitor<Void, Void>{
    protected boolean isOptimized;
    protected SymbolTable simpleTable;
    protected Map<String, Map<String, String>> assignMap;
    protected List<Set<String>> whileInvalidProp;
    protected List<List<Set<String>>> ifInvalidProp;
    protected int w_idx;
    protected int if_idx;
    protected int if_reasign_idx;
    protected List<List<String>> ifReassign;

    public ASToptimization(SymbolTable symbolTable, List<Set<String>> whileInvalidProp, List<List<Set<String>>> ifInvalidProp){
        this.simpleTable = symbolTable;
        this.isOptimized = false;
        this.assignMap = new HashMap<>();
        this.whileInvalidProp = whileInvalidProp;
        this.ifInvalidProp = ifInvalidProp;
        this.w_idx = 0;
        this.if_idx = 0;
        this.if_reasign_idx = 0;
        for (String method : simpleTable.getMethods()) {
            assignMap.put(method, new HashMap<>());
        }
        this.ifReassign = new ArrayList<>();
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
        addVisit("Identifier", this::dealWithIdentifier);
        addVisit("This", this::defaultVisitor);
        addVisit("MethodArgs", this::defaultVisitor);
    }

    private Void defaultVisitor(JmmNode jmmNode, Void _void){
        return null;
    }

    private Void dealWithWhile(JmmNode jmmNode, Void unused) {
        w_idx++;
        return null;
    }

    private Void dealWithIf(JmmNode jmmNode, Void unused) {
        ifReassign.add(new ArrayList<>());
        if_reasign_idx++;
        if_idx++;
        return null;
    }

    private void replace(JmmNode node, String kind, String value){
        JmmNode newNode = new JmmNodeImpl(kind);
        newNode.put("value", value);
        node.replace(newNode);
    }

    private Void dealWithIdentifier(JmmNode node, Void unused) {
        // check if it's safe to propagate
        for (int i = 0; i < this.w_idx; i++) {
            if (this.whileInvalidProp.get(i).contains(node.get("value"))) {
                return null;
            }
        }
        Optional<JmmNode> ifAncestor = node.getAncestor("If");
        Optional<JmmNode> blockCodeAncestor = node.getAncestor("BlockCode");
        if (ifAncestor.isPresent() && blockCodeAncestor.isPresent() && blockCodeAncestor.get().getJmmParent().getKind().equals("If")) { // condition variables don't enter this if

            Optional<JmmNode> assign = node.getAncestor("Assignment");
            if (assign.isPresent()) {
                for (JmmNode child : blockCodeAncestor.get().getChildren()){
                    if (child.getKind().equals("Assignment") && child.get("var").equals(node.get("value")) && child != assign.get()) {
                        return null;
                    }
                    if (child == assign.get()) break;
                }
            }
        }
        if (!ifAncestor.isPresent()) {
            for (int i = 0; i < this.if_idx; i++) {
                if (this.ifInvalidProp.get(i).get(0).contains(node.get("value")) || this.ifInvalidProp.get(i).get(1).contains(node.get("value"))) {
                    return null;
                }
            }
        }
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
            System.out.println("replacing " + node + " with value " + value );

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
        this.w_idx = 0;
        this.if_idx = 0;
        return super.visit(jmmNode);
    }

}
