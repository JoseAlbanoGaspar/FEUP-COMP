package pt.up.fe.comp2023;


import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;

public class OllirVisitor extends AJmmVisitor<String, String> {
    private String ollirString;
    private final SymbolTable symbolTable;

    private boolean importsHandled;
    private String typesSwap(String str){
        return switch (str) {
            case "int" -> "i32";
            case "boolean" -> "bool";
            default -> str;
        };
    }

    public OllirVisitor(SymbolTable symbolTable){
        this.symbolTable = symbolTable;
        this.ollirString = "";
        importsHandled = false;
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
        addVisit("BinaryOp", this::dealWithBinaryOp);
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
        StringBuilder ret = new StringBuilder();
        String methodName = jmmNode.getJmmParent().get("name");
        var args = symbolTable.getParameters(methodName);
        boolean first = true;
        for (Symbol symbol: args){
            String comma = first ? "" : ", ";
            first = false;
            String array = symbol.getType().isArray() ? ".array" : "";
            ret.append(comma).append(symbol.getName()).append(array).append(".").append(typesSwap(symbol.getType().getName()));
        }
        return ret.toString();
    }

    private String dealWithThis(JmmNode jmmNode, String s) {
        return "";
    }

    private String dealWithIdentifier(JmmNode jmmNode, String s) {
        return "";
    }

    private String dealWithBool(JmmNode jmmNode, String s) {
        return "";
    }

    private String dealWithInteger(JmmNode jmmNode, String s) {
        return "";
    }

    private String dealWithNewClass(JmmNode jmmNode, String s) {
        return "";
    }

    private String dealWithNewArray(JmmNode jmmNode, String s) {
        return "";
    }

    private String dealWithFunctionCall(JmmNode jmmNode, String s) {
        return "";
    }

    private String dealWithLength(JmmNode jmmNode, String s) {
        return "";
    }

    private String dealWithSquareBrackets(JmmNode jmmNode, String s) {
        return "";
    }

    private String dealWithLogicalAnd(JmmNode jmmNode, String s) {
        return "";
    }

    private String dealWithCompare(JmmNode jmmNode, String s) {
        return "";
    }

    private String dealWithBinaryOp(JmmNode jmmNode, String s) {
        return "";
    }

    private String dealWithParenthesis(JmmNode jmmNode, String s) {
        return "";
    }

    private String dealWithNegation(JmmNode jmmNode, String s) {
        return "";
    }

    private String dealWithArray(JmmNode jmmNode, String s) {
        return "";
    }

    private String dealWithAssignment(JmmNode jmmNode, String s) {
        StringBuilder ret = new StringBuilder(s);
        Symbol symbol = null;
        if(jmmNode.getJmmParent().getKind().equals("ClassDeclaration")){ //class variable
            for(Symbol symbol1 : this.symbolTable.getFields()){
                if(symbol1.getName().equals(jmmNode.get("var"))){
                    symbol = symbol1;
                }
            }
            assert symbol != null;

            String txt = symbol.getType().isArray()? ".array" : "";

            ret.append("putfield(this, ")
                    .append(jmmNode.get("var"))
                    .append(".")
                    .append(txt)
                    .append(typesSwap(symbol.getType().getName()));
        }


        return ret.toString();
    }

    private String dealWithStatementExpression(JmmNode jmmNode, String s) {
        return "";
    }

    private String dealWithWhile(JmmNode jmmNode, String s) {
        return "";
    }

    private String dealWithIf(JmmNode jmmNode, String s) {
        return "";
    }

    private String dealWithBlockCode(JmmNode jmmNode, String s) {
        return "";
    }

    private String dealWithType(JmmNode jmmNode, String s) {
        return "";
    }

    private String dealWithMethod(JmmNode jmmNode, String s) {
        StringBuilder ret = new StringBuilder();
        String name = jmmNode.get("name");
        ret.append(s).append(".method public ").append(name).append("(");

        String s2;
        boolean first = true;
        for(JmmNode child: jmmNode.getChildren()){
            if(first){
                s2 = "";
                first = false;
            }else{
                s2=", ";
            }
            if(child.getKind().equals("MethodArgs")){
                ret.append(visit(child, s2));
            }
        }
        String returnType = getRetType(name);
        ret.append(").").append(returnType).append(" {\n");

        for (JmmNode child : jmmNode.getChildren()){
            if(child.getKind().equals("MethodArgs")) continue;
            if(child.getKind().equals("Expression")){
                ret.append(s).append("\tret.").append(returnType).append(" ")
                        .append(visit(child, ""))
                        .append(";\n");
                break;
            }
            ret.append(visit(child, s + "\t"));
        }
        ret.append(s).append("}\n");
        return ret.toString();
    }

    private String dealWithMainMethod(JmmNode jmmNode, String s) {
        StringBuilder ret = new StringBuilder();
        ret.append(s).append("\n\t.method public static main(args.array.String).V {\n");

        for (JmmNode child : jmmNode.getChildren()){
            ret.append(visit(child, s + "\t"));
        }

        ret.append(s).append("}\n");
        return ret.toString();
    }

    private String dealWithVarDeclaration(JmmNode jmmNode, String s) {
        return "";
    }

    private String dealWithClassDeclaration(JmmNode jmmNode, String s) {
        StringBuilder ret = new StringBuilder(s);
        String name = jmmNode.get("name");
        ret.append(name).append(" {\n");

        for (JmmNode child: jmmNode.getChildren()){
            if(child.getKind().equals("VarDeclaration")){
                for(Symbol symbol: this.symbolTable.getFields()) {
                    String array = symbol.getType().isArray() ? ".array" : "";
                    ret.append(s).append("\t.field private ")
                            .append(symbol.getName()).append(array).append(".").append(typesSwap(symbol.getType().getName()))
                            .append(";\n");
                }
                break;
            }
        }
        ret.append(s).append("\t.construct").append(name).append("().V {\n")
                .append(s).append("\t\tinvokespecial(this, \"<init>\").V;\n")
                .append(s).append("\t}\n");

        for (JmmNode child: jmmNode.getChildren()){
            if(child.getKind().equals("VarDeclaration")) continue;

            ret.append(visit(child, s + "\t"));
        }
        return ret.append(s).append("}").toString();
    }

    private String dealWithImportDeclaration(JmmNode jmmNode, String s) {
        if(this.importsHandled) return "";
        this.importsHandled = true;
        StringBuilder ret = new StringBuilder(s);

        for(String str : this.symbolTable.getImports()){
            ret.append("import ").append(str).append(";\n");
        }
        return ret.toString();
    }

    private String dealWithProgram(JmmNode jmmNode, String s) {
        StringBuilder ret = new StringBuilder();
        for (JmmNode child: jmmNode.getChildren()){
            ret.append(visit(child, ""));
        }
        this.ollirString = ret.toString();
        return ret.toString();
    }

    public String getOllirCode() {
        return ollirString;
    }

    private String getRetType(String name){
        return symbolTable.getReturnType(name).getName();
    }
}
