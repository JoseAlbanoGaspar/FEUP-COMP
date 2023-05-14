package pt.up.fe.comp2023;


import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OllirVisitor extends AJmmVisitor<String, String> {
    private final SymbolTable symbolTable;
    private String ollirString;
    private int tempCnt;
    private final Map<JmmNode, String> functionRets;
    private boolean importsHandled;

    public OllirVisitor(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        this.ollirString = "";
        importsHandled = false;
        this.tempCnt = 1;
        this.functionRets = new HashMap<>();
    }

    private String typesSwap(String str) {
        return switch (str) {
            case "int", "int array" -> "i32";
            case "boolean" -> "bool";
            case "void" -> "V";
            case "false" -> "0.bool";
            case "true" -> "1.bool";
            case "import" -> "";
            default -> str;
        };
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
        addVisit("BlockCode", this::dealWithDefault); //TODO
        addVisit("If", this::dealWithDefault); //TODO
        addVisit("While", this::dealWithDefault); //TODO
        addVisit("StatementExpression", this::dealWithStatementExpression);
        addVisit("Assignment", this::dealWithAssignment);
        addVisit("Array", this::dealWithAssignment);
        addVisit("Not", this::dealWithDefault); //TODO
        addVisit("Parenthesis", this::dealWithParenthesis);
        addVisit("BinaryOp", this::dealWithBinaryOp);
        addVisit("Compare", this::dealWithDefault); //TODO
        addVisit("LogicalAnd", this::dealWithDefault); //TODO
        addVisit("SquareBrackets", this::dealWithSquareBrackets);
        addVisit("Length", this::dealWithLength);
        addVisit("FunctionCall", this::dealWithFunctionCall);
        addVisit("NewArray", this::dealWithNewArray);
        addVisit("NewClass", this::dealWithNewClass);
        addVisit("Integer", this::dealWithInteger);
        addVisit("BoolLiteral", this::dealWithBool);
        addVisit("Identifier", this::dealWithIdentifier);
        addVisit("This", this::dealWithThis); //??
        addVisit("MethodArgs", this::dealWithMethodArgs);
    }

    private String nestedAppend(JmmNode jmmNode, String s, StringBuilder ret){
        String lastString = visit(jmmNode, "");
        List<String> lasStringList = getNested(jmmNode, lastString, s);



        if (!lasStringList.get(0).contains("\n")) ret.append(lasStringList.get(0));
        else ret.append(lasStringList.get(0));

        lastString = lasStringList.get(1);

        if(jmmNode.getKind().equals("SquareBrackets")){
            ret.append("t").append(tempCnt).append(".i32 :=.i32 ")
                    .append(lastString)
                    .append(";\n").append(s);
            return "t" + tempCnt++ + ".i32";
        }

        return lastString;
    }

    private String dealWithAssignment(JmmNode jmmNode, String s) {
        StringBuilder ret = new StringBuilder(s);

        Symbol var = null;
        int parNum=0;
        boolean isField = false, isParameter = false;
        if(jmmNode.getJmmParent().getKind().equals("MainMethod")){
            for (Symbol vari : symbolTable.getLocalVariables("main")) { //check if local
                if (vari.getName().equals(jmmNode.get("var"))) var = vari;
            }

            if (var == null) {
                for (Symbol symbol1 : this.symbolTable.getFields()) { //check if field
                    if (symbol1.getName().equals(jmmNode.get("var"))){
                        var = symbol1;
                        isField = true;
                        break;
                    }
                }
            }
        }else {
            for (Symbol vari : symbolTable.getLocalVariables(jmmNode.getJmmParent().get("name"))) { //check if local
                if (vari.getName().equals(jmmNode.get("var"))){
                    var = vari;
                    break;
                }
            }
            if (var == null) {
                for (Symbol vari : symbolTable.getParameters(jmmNode.getJmmParent().get("name"))) { //check if parameter
                    if (vari.getName().equals(jmmNode.get("var"))){
                        var = vari;
                        isParameter = true;
                        parNum++;
                        break;
                    }
                }
            }
            if (var == null) {
                for (Symbol symbol1 : this.symbolTable.getFields()) { //check if field
                    if (symbol1.getName().equals(jmmNode.get("var"))){
                        var = symbol1;
                        isField = true;
                        break;
                    }
                }
            }
        }

        assert var != null;
        if (isField) { //class variable
            String txt = var.getType().isArray() ? ".array" : "";

            ret.append("putfield(this, ")
                    .append(jmmNode.get("var"))
                    .append(".")
                    .append(txt)
                    .append(typesSwap(var.getType().getName())).append(", ")
                    .append(visit(jmmNode.getChildren().get(0), ""))
                    .append(").V;\n");
        } else {
            boolean isArray = jmmNode.getKind().equals("Array");

            JmmNode assig = jmmNode.getChildren().get(0);
            String assignString = assignChildrenCases(jmmNode, assig, s, ret, var, isArray, isParameter, parNum);


            if(isArray){
                String assing2String = assignChildrenCases(jmmNode, jmmNode.getJmmChild(1), s, ret, var, isArray, isParameter, parNum);
                String aux = varAux(jmmNode, var, isArray, isParameter, parNum);
                aux = aux.substring(0, aux.indexOf(".array"));
                ret.append(s).append(aux).append("[")
                        .append(assignString)
                        .append("].i32 :=.i32 ")
                        .append(assing2String)
                        .append(";\n");
            }else if(!assignString.equals("")) {
                ret.append(s).append(varAux(jmmNode, var, isArray, isParameter, parNum))
                        .append(assignString)
                        .append(";\n");
            }
        }
        return ret.toString();
    }

    private String assignChildrenCases(JmmNode jmmNode, JmmNode assig, String s, StringBuilder ret, Symbol var, boolean isArray, boolean isParameter, int parNum) {
        String assignString="";
        if (assig.getKind().equals("BinaryOp")) {
            String op = visit(assig, "");
            List<String> rows = List.of(op.split("\n"));
            for (int i = 0; i < rows.size(); i++) {
                if (i == rows.size() - 1) {
                    ret.append(s).append(varAux(jmmNode, var, isArray, isParameter, parNum));

                    ret.append(rows.get(i)).append(";\n");
                } else {
                    ret.append(rows.get(i)).append("\n");
                }
            }
        } else if (assig.getKind().equals("NewArray") || assig.getKind().equals("NewClass")) {
            ret.append(varAux(jmmNode, var, isArray, isParameter, parNum));

            String assignmentString = nestedAppend(assig, s, ret);
            ret.append(assignmentString).append(";\n");
        } else {
            if (assig.getKind().equals("FunctionCall"))
                this.functionRets.put(jmmNode.getJmmChild(0), typesSwap(var.getType().getName()));
            assignString = visit(assig, "");
            String index = "";

            if (assig.getKind().equals("SquareBrackets")) {
                assignString = nestedAppend(assig, s, ret);
            } else if (assig.getKind().equals("FunctionCall")) {
                this.functionRets.put(jmmNode.getJmmChild(0), typesSwap(var.getType().getName()));
                assignString = nestedAppend(assig, s, ret);
            }
        }
        return assignString;
    }

    private String dealWithStatementExpression(JmmNode jmmNode, String s) {
        if (jmmNode.getJmmChild(0).getKind().equals("FunctionCall"))
            this.functionRets.put(jmmNode.getJmmChild(0), typesSwap("void"));
        return visit(jmmNode.getJmmChild(0), s) + ";\n";
    }

    private String dealWithDefault(JmmNode jmmNode, String s) {
        return "";
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
        String returnType = getRetType(name);
        ret.append(").").append(typesSwap(returnType)).append(" {\n");
        int i;
        for (i = 0; i < jmmNode.getNumChildren() - 1; i++) {
            JmmNode child = jmmNode.getChildren().get(i);
            if (child.getKind().equals("MethodArgs")) continue;
            if (child.getKind().equals("Expression")) {

                ret.append(s).append(typesSwap(returnType)).append(" ")
                        .append(visit(child, ""))
                        .append(";\n");
                break;
            }
            ret.append(visit(child, s + "\t"));
        }
        if (!returnType.equals("V")) {
            JmmNode lastNode = jmmNode.getChildren().get(i);
            if (lastNode.getKind().equals("Identifier") || lastNode.getKind().equals("Integer") || lastNode.getKind().equals("BoolLiteral")) {

                String lastString = nestedAppend(lastNode, s, ret);

                ret.append("\n").append(s).append("\tret.").append(typesSwap(returnType)).append(" ").append(lastString)
                        .append(";\n\t}\n");
            } else { //op, need to make temp
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
                        .append(typesSwap(returnType))
                        .append(" :=.")
                        .append(typesSwap(returnType))
                        .append(" ")
                        .append(lastString)
                        .append(";\n")
                        .append(s)
                        .append("\tret.")
                        .append(typesSwap(returnType))
                        .append(" t")
                        .append(this.tempCnt)
                        .append(".")
                        .append(typesSwap(returnType))
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
                            .append(symbol.getName()).append(array).append(".").append(typesSwap(symbol.getType().getName()))
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
            ret.append(comma).append(symbol.getName()).append(array).append(".").append(typesSwap(symbol.getType().getName()));
        }
        return ret.toString();
    }

    private String dealWithThis(JmmNode jmmNode, String s) {
        return "this";
    }

    private String dealWithIdentifier(JmmNode jmmNode, String s) {
        if(s==null) s = "";
        StringBuilder ret = new StringBuilder(s);
        Symbol var = null;
        boolean isField = false, isInMethod = false, isImported = false;


        JmmNode parent = jmmNode;
        String parentName;

        do {
            parent = parent.getJmmParent();
            if (parent.getKind().equals("Method") || parent.getKind().equals("MainMethod"))
                break;
        } while (!parent.getKind().equals("ClassDeclaration"));
        parentName = parent.getKind().equals("MainMethod") ? "main" : parent.get("name");

        //check if local
        for (Symbol vari : symbolTable.getLocalVariables(parentName))
            if (vari.getName().equals(jmmNode.get("value"))) {
                var = vari;
                isInMethod = true;
            }
        if (var == null) {
            //check if parameter
            for (Symbol vari : symbolTable.getParameters(parentName))
                if (vari.getName().equals(jmmNode.get("value"))){
                    var = vari;
                }
        }
        if (var == null) {
            //check if field
            for (Symbol vari : symbolTable.getFields()) {
                if (vari.getName().equals(jmmNode.get("value"))) {
                    isField = true;
                    var = vari;
                    break;
                }
            }
        }
        if (var == null) { //chek is import
            for (String imp : this.symbolTable.getImports()) {
                if (imp.equals(jmmNode.get("value"))) {
                    isImported = true;
                    var = new Symbol(new Type("import", false), jmmNode.get("value"));
                }
            }
        }
        assert var != null;

        String txt = var.getType().isArray() ? ".array" : "";
        String type = typesSwap(var.getType().getName());
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
                .append(typesSwap(var.getType().getName()))
                .toString();
    }

    private String dealWithBool(JmmNode jmmNode, String s) {
        return typesSwap(jmmNode.get("value"));
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
        String expressionString = nestedAppend(expression, s, ret);
        return ret.append("new(array, ").append(expressionString).append(").array.i32").toString();
    }

    private String dealWithFunctionCall(JmmNode jmmNode, String s) {
        StringBuilder ret = new StringBuilder(s);

        String name = jmmNode.get("methodName");
        //check if method or import

        String objectString = nestedAppend(jmmNode.getJmmChild(0), s, ret);

        Map<JmmNode, String> visitedArgs = new HashMap<>();
        for (int i = 1; i < jmmNode.getNumChildren(); i++) {
            if (jmmNode.getJmmChild(i).getKind().equals("FunctionCall")) {
                if (!this.symbolTable.getImports().contains(objectString) && !objectString.equals("this"))
                    this.functionRets.put(jmmNode.getJmmChild(i), this.symbolTable.getParameters(jmmNode.get("methodName")).get(i).getType().getName()); //get from list of args
            }

            visitedArgs.put(jmmNode.getJmmChild(i) ,nestedAppend(jmmNode.getJmmChild(i), s, ret));
        }

        for(Map.Entry<JmmNode, String> entry: visitedArgs.entrySet()){
            if(entry.getKey().getKind().equals("Length")){
                ret.append("\n").append(s).append("t").append(tempCnt).append(".i32 :=.i32 ").append(entry.getValue()).append(";\n");
                entry.setValue("t"+tempCnt++ +".i32");
            }
            else if(entry.getKey().getKind().equals("FunctionCall")){
                String typeF = typesSwap(this.functionRets.get(entry.getKey()));
                ret.append("\n").append(s).append("t").append(tempCnt).append(typeF).append(":=").append(typeF).append(entry.getValue()).append(";\n");
                entry.setValue("t"+tempCnt++ +typeF);
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
        String retType = this.symbolTable.getImports().contains(objectString.substring(objectString.indexOf(".")+1)) ? this.functionRets.get(jmmNode)
                : typesSwap(this.symbolTable.getReturnType(name).getName());
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
            this.functionRets.put(jmmNode.getJmmChild(1), typesSwap("i32"));
        String left = visit(jmmNode.getJmmChild(0), "");
        String right = visit(jmmNode.getJmmChild(1), "");


        List<String> leftSplit = List.of(left.split(".array"));

        ret.append(leftSplit.get(0))
                .append("[")
                .append(right)
                .append("]")
                .append(leftSplit.get(1))
                .append("\n");

        return ret.toString();
    }

    private String dealWithBinaryOp(JmmNode jmmNode, String s) {
        StringBuilder ret = new StringBuilder(s);
        if (jmmNode.getJmmChild(0).getKind().equals("FunctionCall"))
            this.functionRets.put(jmmNode.getJmmChild(0), "i32");
        if (jmmNode.getJmmChild(1).getKind().equals("FunctionCall"))
            this.functionRets.put(jmmNode.getJmmChild(1), "i32");
        JmmNode left = jmmNode.getJmmChild(0), right = jmmNode.getJmmChild(1);

        String leftString = nestedAppend(left, s, ret);
        String rightString = nestedAppend(right, s, ret);

        ret.append(leftString)
                .append(" ")
                .append(jmmNode.get("op"))
                .append(".i32 ")
                .append(rightString);
        return ret.toString();
    }

    private List<String> getNested(JmmNode jmmNode, String str, String s) {
        List<String> retList = new ArrayList<>();
        String kind = jmmNode.getKind();
        StringBuilder newStr = new StringBuilder(str);
        StringBuilder auxString = new StringBuilder();
        if (kind.equals("BinaryOp") || kind.equals("FunctionCall") || kind.equals("SquareBrackets") || kind.equals("Parenthesis")) {
            String sub, before = "";
            int lastIndDot = str.lastIndexOf(".");
            if (str.contains(":=.")) {
                sub = str.substring(lastIndDot);
                if (str.contains("\n")) {
                    int lastIndN = str.lastIndexOf("\n");
                    before = str.substring(0, lastIndN + 1);
                    newStr = new StringBuilder(str.substring(lastIndN + 1));
                } else newStr = new StringBuilder(str.substring(str.lastIndexOf(" ")));
            } else sub = str.substring(lastIndDot);

            if (jmmNode.getKind().equals("SquareBrackets")) {
                if (newStr.toString().contains(":=."))
                    newStr = new StringBuilder(newStr.substring(0, newStr.indexOf(" ")));
                else newStr = new StringBuilder(newStr.substring(0, newStr.length() - 1));
            } else {
                auxString.append(before)
                        .append("t").append(this.tempCnt)
                        .append(sub)
                        .append(" :=")
                        .append(sub)
                        .append(" ")
                        .append(newStr)
                        .append(";\n");
                newStr = new StringBuilder(s +"t" +
                        this.tempCnt++
                        + sub);
            }
        } else { //not operation, array, functioncall or parenthesis
            if (str.contains("\n")) {
                List<String> strings = List.of(str.split("\n"));
                if (strings.get(strings.toArray().length - 1).contains("\n")) {
                    for (String line : strings) auxString.append(line);
                    newStr = new StringBuilder(strings.get(strings.toArray().length - 1).substring(0, strings.get(strings.toArray().length - 1).indexOf(" ")));
                } else {
                    for (int i = 0; i < strings.toArray().length - 1; i++) auxString.append(strings.get(i));

                    newStr = new StringBuilder(strings.get(strings.toArray().length - 1));
                }
            } else if (str.contains(":=.")) {
                auxString = new StringBuilder(str);
                newStr = new StringBuilder(str.substring(0, str.indexOf(" ")));
            }
        }
        retList.add(auxString.toString());
        retList.add(newStr.toString());
        return retList;
    }

    private String dealWithParenthesis(JmmNode jmmNode, String s) {
        if (jmmNode.getJmmChild(0).getKind().equals("FunctionCall"))
            this.functionRets.put(jmmNode.getJmmChild(0), typesSwap("void"));
        return visit(jmmNode.getJmmChild(0), "");
    }

    private String varAux(JmmNode jmmNode, Symbol var, boolean isArray, boolean isParameter, int parNumber) {

            String txt = var.getType().isArray() ? ".array" : "";
            String paramStr = isParameter ? "$"+parNumber+"." : "";
            return paramStr + var.getName() + txt + "." + typesSwap(var.getType().getName()) + " :=" + txt + "." + typesSwap(var.getType().getName())  + " ";
    }

    public String getOllirCode() {
        return ollirString;
    }

    private String getRetType(String name) {
        if(symbolTable.getReturnType(name).isArray()) return "array.i32";
        return symbolTable.getReturnType(name).getName();
    }
}
