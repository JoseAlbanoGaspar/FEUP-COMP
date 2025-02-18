package pt.up.fe.comp2023.ollir;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OllirVisitor extends AJmmVisitor<String, String> {
    private String ollirString;
    private boolean importsHandled;
    private final OllirUtils utils;

    public final SymbolTable symbolTable;
    public final Map<JmmNode, String> functionRets;
    public int tempCnt, gotoCnt;

    public OllirVisitor(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        this.ollirString = "";
        importsHandled = false;
        this.tempCnt = 1;
        this.gotoCnt = 1;
        this.functionRets = new HashMap<>();
        utils = new OllirUtils(this);
    }

    @Override
    protected void buildVisitor() {
        addVisit("Program", this::dealWithProgram);
        addVisit("ImportDeclaration", this::dealWithImportDeclaration);
        addVisit("ClassDeclaration", this::dealWithClassDeclaration);
        addVisit("VarDeclaration", this::dealWithDefault);
        addVisit("MainMethod", this::dealWithMainMethod);
        addVisit("Method", this::dealWithMethod);
        addVisit("Type", this::dealWithDefault);
        addVisit("BlockCode", this::dealWithBlockCode);
        addVisit("If", this::dealwithIf);
        addVisit("While", this::dealWithWhile);
        addVisit("StatementExpression", this::dealWithStatementExpression);
        addVisit("Assignment", this::dealWithAssignment);
        addVisit("Array", this::dealWithAssignment);
        addVisit("Not", this::dealWithNot);
        addVisit("Parenthesis", this::dealWithParenthesis);
        addVisit("BinaryOp", this::dealWithBinaryOp);
        addVisit("Compare", this::dealWithBinaryOp);
        addVisit("LogicalAnd", this::dealWithBinaryOp);
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
    private String dealWithDefault(JmmNode jmmNode, String s) {
        return "";
    }

    private String dealWithWhile(JmmNode jmmNode, String s) {
        StringBuilder ret = new StringBuilder();

        JmmNode expr = jmmNode.getJmmChild(0),
                stat = jmmNode.getJmmChild(1);

        int gto = gotoCnt++;

        ret.append(s).append("BODY_").append(gto).append(":\n");
        String exprString = utils.nestedAppend(expr, s, ret);

        //temp for negation
        if (!(jmmNode.getJmmChild(0).getKind().equals("Identifier") || jmmNode.getJmmChild(0).getKind().equals("BoolLiteral"))){
            ret.append(s).append("t").append(tempCnt).append(".bool :=.bool ").append(exprString).append(";\n");
            exprString ="t" + tempCnt++ + ".bool";
        }

        //negate conditional expression
        ret.append(s).append("if (").append("!.bool ").append(exprString).append(") goto ENDLOOP_").append(gto).append(";\n")
                .append(visit(stat, ""));
        ret.append(s).append("goto BODY_").append(gto).append(";\n")
                .append(s).append("ENDLOOP_").append(gto).append(":\n");

        return ret.toString();
    }

    private String dealWithBlockCode(JmmNode jmmNode, String s) {
        StringBuilder ret = new StringBuilder();
        for(JmmNode child : jmmNode.getChildren()){
            ret.append(visit(child, s));
        }
        return ret.toString();
    }

    private String dealwithIf(JmmNode jmmNode, String s) {
        StringBuilder ret = new StringBuilder(s);
        JmmNode expr = jmmNode.getJmmChild(0),
            ifStat = jmmNode.getJmmChild(1),
            elseStat = jmmNode.getJmmChild(2);

        int gto = gotoCnt++;

        String exprString = utils.nestedAppend(expr, s, ret);
        ret.append("if (").append(exprString).append(") goto THEN_").append(gto).append(";\n")
                .append(visit(elseStat, s))
                .append(s).append("goto ENDIF_").append(gto).append(";\n")
                .append(s).append("THEN_").append(gto).append(":\n")
                .append(visit(ifStat, s))
                .append(s).append("ENDIF_").append(gto).append(":\n");
        return ret.toString();
    }

    private String dealWithNot(JmmNode jmmNode, String s) {
        StringBuilder ret = new StringBuilder();
        String expr = utils.nestedAppend(jmmNode.getJmmChild(0), s, ret);

        //temp
        if (!(jmmNode.getJmmChild(0).getKind().equals("Identifier") || jmmNode.getJmmChild(0).getKind().equals("BoolLiteral"))){
            ret.append(s).append("t").append(tempCnt).append(".bool :=.bool ").append(expr).append(";\n");
            expr = s + "t" + tempCnt++ + ".bool";
        }
        ret.append(s).append("!.bool ").append(expr);
        return ret.toString();
    }

    private String dealWithAssignment(JmmNode jmmNode, String s) {
        StringBuilder ret = new StringBuilder(s);

        String parentName = utils.findParentName(jmmNode);
        VarRecord varRecord = utils.findVar(jmmNode, parentName, "var");
        Symbol var = varRecord.var();
        boolean isField = varRecord.isField();
        boolean isParameter = varRecord.isParameter();
        int parNum = varRecord.parNum();

        assert var != null;
        if (isField) { //class variable
            String txt = var.getType().isArray() ? ".array" : "";
            String assigString = utils.nestedAppend(jmmNode.getChildren().get(0), s, ret);
            ret.append("putfield(this, ")
                    .append(jmmNode.get("var"))
                    .append(".")
                    .append(txt)
                    .append(utils.typesSwap(var.getType().getName())).append(", ")
                    .append(assigString)
                    .append(").V;\n");
        } else {
            boolean isArray = jmmNode.getKind().equals("Array");

            JmmNode assig = jmmNode.getChildren().get(0);
            String assignString = utils.assignChildrenCases(jmmNode, assig, s, ret, var, isParameter, parNum);


            if(isArray){
                String assing2String = utils.nestedAppend(jmmNode.getJmmChild(1), s, ret);
                //String assing2String = assignChildrenCases(jmmNode, jmmNode.getJmmChild(1), s, ret, var, isParameter, parNum);
                String aux = utils.varAux(var, isParameter, parNum);
                aux = aux.substring(0, aux.indexOf(".array"));
                ret.append(s).append(aux).append("[")
                        .append(assignString)
                        .append("].i32 :=.i32 ")
                        .append(assing2String)
                        .append(";\n");
            }else if(!assignString.equals("")) {
                ret.append(s).append(utils.varAux(var, isParameter, parNum))
                        .append(assignString)
                        .append(";\n");
            }
        }
        return ret.toString();
    }

    private String dealWithStatementExpression(JmmNode jmmNode, String s) {
        if (jmmNode.getJmmChild(0).getKind().equals("FunctionCall"))
            this.functionRets.put(jmmNode.getJmmChild(0), utils.typesSwap("void"));
        return visit(jmmNode.getJmmChild(0), s) + ";\n";
    }

    private String dealWithMethod(JmmNode jmmNode, String s) {
        StringBuilder ret = new StringBuilder();
        String name = jmmNode.get("name");
        ret.append(s).append(".method public ").append(name).append("(");

        String s2;
        boolean first = true;
        for (JmmNode child : jmmNode.getChildren()) {
            if (first) {
                s2 = "";
                first = false;
            } else {
                s2 = ", ";
            }
            if (child.getKind().equals("MethodArgs")) {
                ret.append(visit(child, s2));
            }
        }
        String returnType = utils.getRetType(name);
        ret.append(").").append(utils.typesSwap(returnType)).append(" {\n");
        int i;
        for (i = 0; i < jmmNode.getNumChildren() - 1; i++) {
            JmmNode child = jmmNode.getChildren().get(i);
            if (child.getKind().equals("MethodArgs")) continue;
            if (child.getKind().equals("Expression")) {

                ret.append(s).append(utils.typesSwap(returnType)).append(" ")
                        .append(visit(child, ""))
                        .append(";\n");
                break;
            }
            ret.append(visit(child, s + "\t"));
        }
        if (!returnType.equals("V")) {
            JmmNode lastNode = jmmNode.getChildren().get(i);
            if (lastNode.getKind().equals("Identifier") || lastNode.getKind().equals("Integer") || lastNode.getKind().equals("BoolLiteral")) {

                String lastString = utils.nestedAppend(lastNode, s, ret);

                ret.append("\n").append(s).append("\tret.").append(utils.typesSwap(returnType)).append(" ").append(lastString)
                        .append(";\n\t}\n");
            } else { //need to make temp
                String lastString = visit(lastNode, "");

                if (lastString.contains("\n")) {
                    List<String> lines = List.of(lastString.split("\n"));
                    lines = lines.stream().filter(x -> !x.isEmpty()).collect(Collectors.toList());
                    int j;
                    for (j = 0; j < lines.size() - 1; j++) {
                        ret.append(s)
                                .append("\t")
                                .append(lines.get(j))
                                .append("\n");
                    }
                    lastString = lines.get(j);
                }

                ret.append(s)
                        .append("\tt")
                        .append(this.tempCnt)
                        .append(".")
                        .append(utils.typesSwap(returnType))
                        .append(" :=.")
                        .append(utils.typesSwap(returnType))
                        .append(" ")
                        .append(lastString)
                        .append(";\n")
                        .append(s)
                        .append("\tret.")
                        .append(utils.typesSwap(returnType))
                        .append(" t")
                        .append(this.tempCnt)
                        .append(".")
                        .append(utils.typesSwap(returnType))
                        .append(";\n\t}\n");
                this.tempCnt++;
            }
            return ret.toString();
        }
        ret.append("\n\t}\n");
        return ret.toString();
    }

    private String dealWithMainMethod(JmmNode jmmNode, String s) {
        StringBuilder ret = new StringBuilder();
        ret.append(s).append("\n\t.method public static main(args.array.String).V {\n");

        for (JmmNode child : jmmNode.getChildren()) {
            ret.append(visit(child, s + "\t"));
        }

        ret.append(s).append("\tret.V;\n");
        ret.append(s).append("}\n");
        return ret.toString();
    }

    private String dealWithClassDeclaration(JmmNode jmmNode, String s) {
        StringBuilder ret = new StringBuilder(s);
        String name = jmmNode.get("name");

        String superName = jmmNode.hasAttribute("superName") ? " extends " + jmmNode.get("superName") : "";

        ret.append(name).append(superName).append(" {\n");

        for (JmmNode child : jmmNode.getChildren()) {
            if (child.getKind().equals("VarDeclaration")) {
                for (Symbol symbol : this.symbolTable.getFields()) {
                    String array = symbol.getType().isArray() ? ".array" : "";
                    ret.append(s).append("\t.field private ")
                            .append(symbol.getName()).append(array).append(".").append(utils.typesSwap(symbol.getType().getName()))
                            .append(";\n");
                }
                break;
            }
        }
        ret.append(s).append("\t.construct").append(name).append("().V {\n")
                .append(s).append("\t\tinvokespecial(this, \"<init>\").V;\n")
                .append(s).append("\t}\n");

        for (JmmNode child : jmmNode.getChildren()) {
            if (child.getKind().equals("VarDeclaration")) continue;

            ret.append(visit(child, s + "\t"));
        }
        return ret.append(s).append("}").toString();
    }

    private String dealWithImportDeclaration(JmmNode jmmNode, String s) {
        if (this.importsHandled) return "";
        this.importsHandled = true;
        StringBuilder ret = new StringBuilder(s);

        for (String str : this.symbolTable.getImports()) {
            ret.append("import ").append(str).append(";\n");
        }
        return ret.toString();
    }

    private String dealWithProgram(JmmNode jmmNode, String s) {
        StringBuilder ret = new StringBuilder();
        for (JmmNode child : jmmNode.getChildren()) {
            ret.append(visit(child, ""));
        }
        this.ollirString = ret.toString();
        return ret.toString();
    }

    private String dealWithMethodArgs(JmmNode jmmNode, String s) {
        StringBuilder ret = new StringBuilder();
        String methodName = jmmNode.getJmmParent().get("name");
        var args = symbolTable.getParameters(methodName);
        boolean first = true;
        for (Symbol symbol : args) {
            String comma = first ? "" : ", ";
            first = false;
            String array = symbol.getType().isArray() ? ".array" : "";
            ret.append(comma).append(symbol.getName()).append(array).append(".").append(utils.typesSwap(symbol.getType().getName()));
        }
        return ret.toString();
    }

    private String dealWithThis(JmmNode jmmNode, String s) {
        return "this."+symbolTable.getClassName();
    }

    private String dealWithIdentifier(JmmNode jmmNode, String s) {
        if(s==null) s = "";
        StringBuilder ret = new StringBuilder(s);
        String parentName = utils.findParentName(jmmNode);
        VarRecord varRecord = utils.findVar(jmmNode, parentName, "value");
        Symbol var = varRecord.var();
        boolean isField = varRecord.isField();
        boolean isImported = varRecord.isImported();
        boolean isInMethod = varRecord.isInMethod();

        assert var != null;

        String txt = var.getType().isArray() ? ".array" : "";
        String type = utils.typesSwap(var.getType().getName());
        if (isField) {
            ret.append("t")
                    .append(this.tempCnt)
                    .append(txt)
                    .append(".")
                    .append(type)
                    .append(" :=")
                    .append(txt)
                    .append(".")
                    .append(type)
                    .append(" getfield(this, ")
                    .append(var.getName())
                    .append(txt)
                    .append(".")
                    .append(type)
                    .append(")")
                    .append(txt)
                    .append(".")
                    .append(type)
                    .append(";\n");

            ret
                    .append("t")
                    .append(this.tempCnt++)
                    .append(txt)
                    .append(".")
                    .append(type);
            return ret.toString();
        } else if (isInMethod) {
            return ret.append(var.getName())
                    .append(txt)
                    .append(".")
                    .append(type)
                    .toString();
        } else if (isImported) {
            return ret.append(var.getName()).toString();
        }
        //param
        //get param index
        int i;
        for (i = 0; i < symbolTable.getParameters(parentName).size(); i++)
            if (symbolTable.getParameters(parentName).get(i).getName().equals(var.getName())) break;

        return ret.append("$")
                .append(i + 1)
                .append(".")
                .append(var.getName())
                .append(txt)
                .append(".")
                .append(utils.typesSwap(var.getType().getName()))
                .toString();
    }

    private String dealWithBool(JmmNode jmmNode, String s) {
        return utils.typesSwap(jmmNode.get("value"));
    }

    private String dealWithInteger(JmmNode jmmNode, String s) {
        return jmmNode.get("value") + ".i32";
    }

    private String dealWithNewClass(JmmNode jmmNode, String s) {
        return "new(" +
                jmmNode.get("className") +
                ")." +
                jmmNode.get("className") +
                ";\n" +
                "invokespecial(" +
                s +
                ", \"<init>\").V";
    }

    private String dealWithNewArray(JmmNode jmmNode, String s) {
        StringBuilder ret = new StringBuilder(s);
        ret.append(" ");
        JmmNode expression = jmmNode.getJmmChild(1);
        String expressionString = utils.nestedAppend(expression, s, ret);
        return ret.append("new(array, ").append(expressionString).append(").array.i32").toString();
    }

    private String dealWithFunctionCall(JmmNode jmmNode, String s) {
        StringBuilder ret = new StringBuilder(s);

        String name = jmmNode.get("methodName");

        //check if local method or import
        String objectString = utils.nestedAppend(jmmNode.getJmmChild(0), s, ret);

        Map<JmmNode, String> visitedArgs = new HashMap<>();
        for (int i = 1; i < jmmNode.getNumChildren(); i++) {
            if (jmmNode.getJmmChild(i).getKind().equals("FunctionCall")) {
                if (!this.symbolTable.getImports().contains(objectString) && !objectString.equals("this")){
                    String typeName = this.symbolTable.getReturnType(jmmNode.get("methodName")).getName();
                    this.functionRets.put(jmmNode.getJmmChild(i), utils.typesSwap(typeName));
                }
            }

            visitedArgs.put(jmmNode.getJmmChild(i) ,utils.nestedAppend(jmmNode.getJmmChild(i), s, ret));
        }

        for(Map.Entry<JmmNode, String> entry: visitedArgs.entrySet()){
            if(entry.getKey().getKind().equals("Length")){
                ret.append("\n").append(s).append("t").append(tempCnt).append(".i32 :=.i32 ").append(entry.getValue()).append(";\n");
                entry.setValue("t"+tempCnt++ +".i32");
            }
            else if(entry.getKey().getKind().equals("FunctionCall")){
                String typeF = utils.typesSwap(this.functionRets.get(entry.getKey()));
                ret.append("\n").append(s).append("t").append(tempCnt).append(".").append(typeF).append(" :=.").append(typeF).append(" ").append(entry.getValue()).append(";\n");
                entry.setValue("t"+tempCnt++ +"."+typeF);
            }
        }

        ret.append(this.symbolTable.getImports().contains(objectString) ? "invokestatic(" : "invokevirtual(")
                .append(objectString)
                .append(", \"")
                .append(name)
                .append("\"");



        for (Map.Entry<JmmNode, String> entry : visitedArgs.entrySet()) {
            ret.append(", ").append(entry.getValue());
        }
        ret.append(").");

        //return type
        String retType;
        if(this.symbolTable.getImports().contains(objectString.substring(objectString.indexOf(".")+1)))
            retType= this.functionRets.get(jmmNode);
        else if(this.symbolTable.getMethods().contains(name)){
            String typeToSwap = this.symbolTable.getReturnType(name).isArray() ?
                    "int array" :
                    this.symbolTable.getReturnType(name).getName();
            retType = utils.typesSwap(typeToSwap);
        }
        else{
            retType= this.functionRets.get(jmmNode);
        }
        ret.append(retType);

        return ret.toString();
    }

    private String dealWithLength(JmmNode jmmNode, String s) {
        if (jmmNode.getJmmChild(0).getKind().equals("FunctionCall"))
            this.functionRets.put(jmmNode.getJmmChild(0), "array.i32");
        return s + "arraylength(" +
                visit(jmmNode.getJmmChild(0)) +
                ").i32";
    }

    private String dealWithSquareBrackets(JmmNode jmmNode, String s) {
        if(jmmNode.getJmmParent().getKind().equals("StatementExpression")) return "";
        StringBuilder ret = new StringBuilder(s);
        if (jmmNode.getJmmChild(0).getKind().equals("FunctionCall"))
            this.functionRets.put(jmmNode.getJmmChild(0), "array.i32");
        if (jmmNode.getJmmChild(1).getKind().equals("FunctionCall"))
            this.functionRets.put(jmmNode.getJmmChild(1), utils.typesSwap("i32"));

        String left = utils.nestedAppend(jmmNode.getJmmChild(0), s, ret);
        String right = utils.nestedAppend(jmmNode.getJmmChild(1), s, ret);

        if(!(jmmNode.getJmmChild(1).getKind().equals("Identifier") || jmmNode.getJmmChild(1).getKind().equals("Integer"))){
            ret.append(s).append("t").append(tempCnt).append(".i32 :=.i32 ").append(right).append(";\n");
            right = "t"+tempCnt++ + ".i32 ";
        }

        List<String> leftSplit = List.of(left.split(".array"));

        ret.append(leftSplit.get(0)).append(".array").append(leftSplit.get(1))
                .append("[")
                .append(right)
                .append("]")
                .append(leftSplit.get(1))
                .append(" ");

        return ret.toString();
    }

    private String dealWithBinaryOp(JmmNode jmmNode, String s) {
        StringBuilder ret = new StringBuilder(s);
        if (jmmNode.getJmmChild(0).getKind().equals("FunctionCall"))
            this.functionRets.put(jmmNode.getJmmChild(0), "i32");
        if (jmmNode.getJmmChild(1).getKind().equals("FunctionCall"))
            this.functionRets.put(jmmNode.getJmmChild(1), "i32");
        JmmNode left = jmmNode.getJmmChild(0), right = jmmNode.getJmmChild(1);

        String leftString = utils.nestedAppend(left, s, ret);
        String rightString = utils.nestedAppend(right, s, ret);

        String opType = jmmNode.getKind().equals("BinaryOp") ?
            ".i32 " : ".bool ";
        String opString;

        switch (jmmNode.getKind()) {
            case "BinaryOp" -> opString = jmmNode.get("op");
            case "LogicalAnd" -> opString = "&&";
            case "Compare" -> opString = "<";
            default -> opString = "ERROR";
        }

        ret.append(leftString)
                .append(" ")
                .append(opString)
                .append(opType)
                .append(rightString);
        return ret.toString();
    }

    private String dealWithParenthesis(JmmNode jmmNode, String s) {
        StringBuilder ret = new StringBuilder(s);
        String data = "";

        if (jmmNode.getJmmChild(0).getKind().equals("FunctionCall")){
            JmmNode objGrandChild = jmmNode.getJmmChild(0).getJmmChild(0);
            if(objGrandChild.getKind().equals("NewClass")){
                this.functionRets.put(jmmNode.getJmmChild(0), objGrandChild.get("className"));
            }
            else{
                this.functionRets.put(jmmNode.getJmmChild(0), utils.typesSwap("void"));
            }
        }
        else if(jmmNode.getJmmChild(0).getKind().equals("NewClass")){
            ret.append("t").append(tempCnt).append(".")
                    .append(jmmNode.getJmmChild(0).get("className"))
                    .append(" :=.")
                    .append(jmmNode.getJmmChild(0).get("className"))
                    .append(" ");
            data = "t"+tempCnt++ + "." + jmmNode.getJmmChild(0).get("className");
        }
        String nestedString = utils.nestedAppend(jmmNode.getJmmChild(0), s, ret, data);
        return ret.append(nestedString).toString();
    }

    public String getOllirCode() {
        return ollirString;
    }
}
