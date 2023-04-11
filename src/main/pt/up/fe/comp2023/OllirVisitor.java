package pt.up.fe.comp2023;


import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;

public class OllirVisitor extends AJmmVisitor<String, String> {
    private String ollirString;
    private SymbolTable symbolTable;

    public OllirVisitor(SymbolTable symbolTable){
        this.symbolTable = symbolTable;
        ollirString = "";
    }
    @Override
    protected void buildVisitor() {
        addVisit("Program", this::dealWithProgram);
        addVisit("ImportDeclaration", this::dealWithImportDeclaration);
        addVisit("ClassDeclaration", this::dealWithClassDeclaration);
        addVisit("VarDeclaration", this::dealWithVarDeclaration);
        addVisit("MainMethod", this::dealWithMainMethod);
        addVisit("Method", this::dealWithMethod);
        addVisit("Type", this::dealWithType);
        addVisit("BlockCode", this::dealWithBlockCode);
        addVisit("If",this::dealWithIf);
        addVisit("While", this::dealWithWhile);
        addVisit("StatementExpression", this::dealWithStatementExpression);
        addVisit("Assignment", this::dealWithAssignment);
        addVisit("Array", this::dealWithArray);
        addVisit("Not", this::dealWithNegation);
        addVisit("Parenthesis", this::dealWithParenthesis);
        addVisit("Multiplicative", this::dealWithBinaryOp);
        addVisit("Compare", this::dealWithCompare);
        addVisit("LogicalAnd", this::dealWithLogicalAnd);
        addVisit("SquareBrackets", this::dealWithSquareBrackets);
        addVisit("Length", this::dealWithLength);
        addVisit("FunctionCall", this::dealWithFunctionCall);
        addVisit("NewArray", this::dealWithNewArray);
        addVisit("NewClass", this::dealWithNewClass);
        addVisit("Integer", this::dealWithInteger);
        addVisit("BoolLiteral", this::dealWithBool);
        addVisit("Identifier", this::dealWithIdentifier);
        addVisit("This", this::dealWithThis);
        addVisit("MethodArgs", this::dealWithMethodArgs);
    }

    private String dealWithMethodArgs(JmmNode jmmNode, String s) {
    }

    private String dealWithThis(JmmNode jmmNode, String s) {
        return null;
    }

    private String dealWithIdentifier(JmmNode jmmNode, String s) {
        return null;
    }

    private String dealWithBool(JmmNode jmmNode, String s) {
        return null;
    }

    private String dealWithInteger(JmmNode jmmNode, String s) {
        return null;
    }

    private String dealWithNewClass(JmmNode jmmNode, String s) {
        return null;
    }

    private String dealWithNewArray(JmmNode jmmNode, String s) {
        return null;
    }

    private String dealWithFunctionCall(JmmNode jmmNode, String s) {
        return null;
    }

    private String dealWithLength(JmmNode jmmNode, String s) {
        return null;
    }

    private String dealWithSquareBrackets(JmmNode jmmNode, String s) {
        return null;
    }

    private String dealWithLogicalAnd(JmmNode jmmNode, String s) {
        return null;
    }

    private String dealWithCompare(JmmNode jmmNode, String s) {
        return null;
    }

    private String dealWithBinaryOp(JmmNode jmmNode, String s) {
        return null;
    }

    private String dealWithParenthesis(JmmNode jmmNode, String s) {
        return null;
    }

    private String dealWithNegation(JmmNode jmmNode, String s) {
        return null;
    }

    private String dealWithArray(JmmNode jmmNode, String s) {
        return null;
    }

    private String dealWithAssignment(JmmNode jmmNode, String s) {
        return null;
    }

    private String dealWithStatementExpression(JmmNode jmmNode, String s) {
        return null;
    }

    private String dealWithWhile(JmmNode jmmNode, String s) {
        return null;
    }

    private String dealWithIf(JmmNode jmmNode, String s) {
        return null;
    }

    private String dealWithBlockCode(JmmNode jmmNode, String s) {
        return null;
    }

    private String dealWithType(JmmNode jmmNode, String s) {
        return null;
    }

    private String dealWithMethod(JmmNode jmmNode, String s) {
        return null;
    }

    private String dealWithMainMethod(JmmNode jmmNode, String s) {
        return null;
    }

    private String dealWithVarDeclaration(JmmNode jmmNode, String s) {
        return null;
    }

    private String dealWithClassDeclaration(JmmNode jmmNode, String s) {
        String ret = s;
        String name = jmmNode.get("name");
        ret+= name;
        ret+=" {\n";

        for (JmmNode child: jmmNode.getChildren()){
            if(child.getKind().equals("VarDeclaration")){
                ret+=visit(child);
            }
        }
        ret+=s+"\t.construct" + name + "().V {\n";
        ret+=s+"\t\tinvokespecial(this, \"<init>\".V;\n";
        ret+=s+"\t}\n";

        for (JmmNode child: jmmNode.getChildren()){
            if(child.getKind().equals("VarDeclaration")){
                continue;
            }
            else{
                ret+=visit(child);
            }
        }
        return ret+=s+"}";
    }

    private String dealWithImportDeclaration(JmmNode jmmNode, String s) {
    }

    private String dealWithProgram(JmmNode jmmNode, String s) {
    }

    public String getOllirCode() {
        return ollirString;
    }
}
