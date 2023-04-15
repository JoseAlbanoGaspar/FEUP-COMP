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
            case "void" -> "V";
            case "false" -> "0.bool";
            case "true" -> "1.bool";
            case "int array" -> "array.i32";
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
        addVisit("Array", this::dealWithAssignment);
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
        return typesSwap(jmmNode.get("value"));
    }

    private String dealWithInteger(JmmNode jmmNode, String s) {
        return jmmNode.get("value") + ".i32";
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
        else{
            boolean isArray = false;
            if(jmmNode.getKind().equals("Array")) isArray = true;

            Symbol var = null;
            for (Symbol vari : symbolTable.getLocalVariables(jmmNode.getJmmParent().get("name"))){
                if (vari.getName().equals(jmmNode.get("var"))) var = vari;
            }
            JmmNode assig= jmmNode.getChildren().get(0);
            if(assig.getKind().equals("BinaryOp") || assig.getKind().equals("Not") || assig.getKind().equals("LogicalAnd") || assig.getKind().equals("Compare")){
                String op = visit(assig);
                String[] rows = op.split("\n");
                for(int i = 0; i<rows.length; i++){
                    if (i == rows.length - 1) {
                        ret.append(varAux(jmmNode, var, isArray));

                        ret.append(rows[i]).append("\n");
                    } else
                        ret.append("\t\t").append(rows[i]).append("\n");
                }
            }
            else if (assig.getKind().equals("NewArray") || assig.getKind().equals("NewClass")){
                String assignmentString = visit(assig);
                if (assignmentString.contains("\n")) {
                    ret.append(assignmentString, 0, assignmentString.indexOf("\n"));
                    assignmentString = assignmentString.substring(assignmentString.indexOf("\n") + 1);
                }
                ret.append(varAux(jmmNode, var, isArray));
                ret.append(assignmentString).append(";\n");
                if (!assig.getChildren().get(0).getKind().equals("ARRAY"))
                    ret.append("\t\tinvokespecial(").append(var.getName()).append(".").append(typesSwap(var.getType().isArray() ? var.getType().getName() + " array" : var.getType().getName()))
                            .append(", \"<init>\").V;\n");
            }
            else{
                String assignString = visit(assig);
                if (assig.getKind().equals("SquareBrackets")) {
                    String before;
                    if (assignString.contains("\n")) {
                        before = assignString.substring(0, assignString.lastIndexOf("\n"));
                        if (assignString.lastIndexOf("\n") < assignString.lastIndexOf(":=.")) { //multiple lines
                            before += assignString.substring(assignString.lastIndexOf("\n"));
                            assignString = assignString.substring(assignString.lastIndexOf("\n") + 1, assignString.lastIndexOf(" :=."));
                        } else
                            assignString = assignString.substring(assignString.lastIndexOf("\n") + 1, assignString.lastIndexOf(";"));
                    } else {
                        before = assignString;
                        assignString = assignString.substring(0, assignString.indexOf(' '));
                    }
                    ret.append(before)
                            .append("\n");
                } else if (assig.getKind().equals("FunctionCall")) {
                    if (assignString.contains("\n")) {
                        ret.append(assignString, 0, assignString.lastIndexOf("\n"));
                        assignString = assignString.substring(assignString.lastIndexOf("\n") + 1);
                    } else if (assignString.contains(":=.")) {
                        ret.append(assignString);
                        assignString = assignString.substring(0, assignString.indexOf(" ")) + ";";
                    }
                }
                if (assignString.contains("\n")) {
                    ret.append(assignString.substring(0, assignString.lastIndexOf("\n") + 1));
                    assignString = assignString.substring(assignString.lastIndexOf("\n") + 1);
                }

                ret.append(varAux(jmmNode, var, isArray))
                    .append(assignString);

                if (!assig.getKind().equals("FunctionCall"))
                    ret.append(";");
                ret.append("\n");
            }
        }

        return ret.toString();
    }

    private String varAux(JmmNode jmmNode, Symbol var, boolean isArray){
        if(!isArray){
            String txt = var.getType().isArray()? ".array" : "";
            return var.getName()+txt+"."+typesSwap(var.getType().getName())+ " :=." +typesSwap(var.getType().getName())+txt +" ";
        }
        String access = visit(jmmNode);
        String before = "";
        String ret = "";
        if (access.contains("\n")) {
            before = access.substring(0, access.lastIndexOf("\n"));
            access = access.substring(access.lastIndexOf("\n"));
        }
        if (access.contains(":=."))
            access = access.substring(access.lastIndexOf(" "), access.lastIndexOf(";"));
        if (access.contains(";"))
            access = access.substring(0, access.lastIndexOf(";"));
        ret += before + "\t\t" + access + " :=.i32 ";
        return ret;
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
