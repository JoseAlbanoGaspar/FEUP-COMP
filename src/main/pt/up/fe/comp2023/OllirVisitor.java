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
            case "int" -> "i32";
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
        addVisit("VarDeclaration", this::dealWithVarDeclaration);
        addVisit("MainMethod", this::dealWithMainMethod);
        addVisit("Method", this::dealWithMethod);
        addVisit("Type", this::dealWithType);
        addVisit("BlockCode", this::dealWithBlockCode); //later
        addVisit("If", this::dealWithIf); //later
        addVisit("While", this::dealWithWhile); //later
        addVisit("StatementExpression", this::dealWithStatementExpression);
        addVisit("Assignment", this::dealWithAssignment);
        addVisit("Array", this::dealWithAssignment);
        addVisit("Not", this::dealWithNegation); //later
        addVisit("Parenthesis", this::dealWithParenthesis);
        addVisit("BinaryOp", this::dealWithBinaryOp);
        addVisit("Compare", this::dealWithCompare); //later
        addVisit("LogicalAnd", this::dealWithLogicalAnd); //later
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

    private String dealWithAssignment(JmmNode jmmNode, String s) {
        StringBuilder ret = new StringBuilder(s);

        Symbol var = null;
        boolean isField = false;
        if(jmmNode.getJmmParent().getKind().equals("MainMethod")){
            for (Symbol vari : symbolTable.getLocalVariables("main")) { //check if local
                if (vari.getName().equals(jmmNode.get("var"))) var = vari;
            }
        }else {
            for (Symbol vari : symbolTable.getLocalVariables(jmmNode.getJmmParent().get("name"))) { //check if local
                if (vari.getName().equals(jmmNode.get("var"))) var = vari;
            }
            if (var == null) {
                for (Symbol vari : symbolTable.getParameters(jmmNode.getJmmParent().get("name"))) { //check if parameter
                    if (vari.getName().equals(jmmNode.get("var"))) var = vari;
                }
            }
            if (var == null) {
                for (Symbol symbol1 : this.symbolTable.getFields()) { //check if field
                    if (symbol1.getName().equals(jmmNode.get("var"))) var = symbol1;
                }
                isField = true;
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
            if (assig.getKind().equals("BinaryOp")) {
                String op = visit(assig, "");
                List<String> rows = List.of(op.split("\n"));
                for (int i = 0; i < rows.size(); i++) {
                    if (i == rows.size() - 1) {
                        ret.append(varAux(jmmNode, var, isArray));

                        ret.append(rows.get(i)).append(";\n");
                    } else {
                        ret.append(rows.get(i)).append("\n");
                    }
                }
            } else if (assig.getKind().equals("NewArray") || assig.getKind().equals("NewClass")) {
                String txt = var.getType().isArray() ? ".array" : "";
                String data = var.getName() + txt + "." + typesSwap(var.getType().getName());
                String assignmentString = visit(assig, data);
                ret.append(varAux(jmmNode, var, isArray));
                if (assignmentString.contains("\n")) {
                    ret.append(assignmentString, 0, assignmentString.indexOf("\n")).append("\n").append(s);
                    assignmentString = assignmentString.substring(assignmentString.indexOf("\n") + 1);
                }
                ret.append(assignmentString).append(";\n");
                if (assig.getNumChildren() != 0 && !assig.getChildren().get(0).getKind().equals("ARRAY"))
                    ret.append("\t\tinvokespecial(").append(var.getName()).append(".").append(typesSwap(var.getType().isArray() ? var.getType().getName() + " array" : var.getType().getName()))
                            .append(", \"<init>\").V;\n");
            } else {
                if (assig.getKind().equals("FunctionCall"))
                    this.functionRets.put(jmmNode.getJmmChild(0), typesSwap(var.getType().getName()));
                String assignString = visit(assig, "");
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
                    this.functionRets.put(jmmNode.getJmmChild(0), typesSwap(var.getType().getName()));
                    if (assignString.contains("\n")) {
                        ret.append(assignString, 0, assignString.lastIndexOf("\n"));
                        assignString = assignString.substring(assignString.lastIndexOf("\n") + 1);
                    } else if (assignString.contains(":=.")) {
                        ret.append(assignString);
                        assignString = assignString.substring(0, assignString.indexOf(" ")) + ";";
                    }
                }
                if (assignString.contains("\n")) {
                    ret.append(assignString, 0, assignString.lastIndexOf("\n") + 1);
                    assignString = assignString.substring(assignString.lastIndexOf("\n") + 1);
                }

                ret.append(varAux(jmmNode, var, isArray))
                        .append(assignString)
                        .append(";\n");
            }
        }
        return ret.toString();
    }

    private String dealWithStatementExpression(JmmNode jmmNode, String s) {
        if (jmmNode.getJmmChild(0).getKind().equals("FunctionCall"))
            this.functionRets.put(jmmNode.getJmmChild(0), typesSwap("void"));
        return visit(jmmNode.getJmmChild(0), s) + ";\n";
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
                ret.append(s).append("\tret.").append(typesSwap(returnType)).append(" ")
                        .append(visit(child, ""))
                        .append(";\n");
                break;
            }
            ret.append(visit(child, s + "\t"));
        }
        if (!returnType.equals("V")) {
            JmmNode lastNode = jmmNode.getChildren().get(i);
            if (lastNode.getKind().equals("Identifier") || lastNode.getKind().equals("Integer") || lastNode.getKind().equals("BoolLiteral")) {
                ret.append(s).append("\tret.").append(typesSwap(returnType)).append(" ").append(visit(lastNode, ""))
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

    private String dealWithVarDeclaration(JmmNode jmmNode, String s) {
        return "";
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
        StringBuilder ret = new StringBuilder(s);
        Symbol var = null;
        boolean isField = false, isInMethod = false, isImported = false;

        for (Symbol vari : symbolTable.getFields()) {
            if (vari.getName().equals(jmmNode.get("value"))) {
                isField = true;
                var = vari;
            }
        }
        JmmNode parent = jmmNode;
        String parentName = null;
        if (!isField) {
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
                    if (vari.getName().equals(jmmNode.get("value"))) var = vari;
            }
            if (var == null) { //is from import
                for (String imp : this.symbolTable.getImports()) {
                    if (imp.equals(jmmNode.get("value"))) {
                        isImported = true;
                        var = new Symbol(new Type("import", false), jmmNode.get("value"));
                    }
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
            this.tempCnt++;
            ret.append(s)
                    .append("t")
                    .append(this.tempCnt++)
                    .append(txt)
                    .append(".")
                    .append(type);
            return ret.toString();
        } else if (isInMethod) {
            return ret.append(var.getName())
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

        JmmNode expression = jmmNode.getJmmChild(1);
        String expressionString = visit(expression, "");
        if (expression.getKind().equals("FunctionCall")) {
            this.functionRets.put(jmmNode.getJmmChild(0), "i32");
            ret.append("\n");
            expressionString = expressionString.substring(0, expressionString.indexOf(" "));
        }
        return ret.append("new(array, ").append(expressionString).append(").i32").toString();
    }

    private String dealWithFunctionCall(JmmNode jmmNode, String s) {
        StringBuilder ret = new StringBuilder(s);
//
        String name = jmmNode.get("methodName");
        //check if method or import
        String objectString = visit(jmmNode.getJmmChild(0), "");

        List<String> visitedArgs = new ArrayList<>();
        for (int i = 1; i < jmmNode.getNumChildren(); i++) {
            if (jmmNode.getJmmChild(i).getKind().equals("FunctionCall")) {
                if (!this.symbolTable.getImports().contains(objectString) && !objectString.equals("this"))
                    this.functionRets.put(jmmNode.getJmmChild(i), this.symbolTable.getParameters(jmmNode.get("methodName")).get(i).getType().getName()); //get from list of args
            }
            String childString = visit(jmmNode.getJmmChild(i), "");
            List<String> nested = getNested(jmmNode.getJmmChild(i), childString, s);

            if (!nested.get(0).contains("\n")) ret.append(nested.get(0));
            else ret.append(nested.get(0)).append("\n");
            visitedArgs.add(nested.get(1));
        }

        ret.append(this.symbolTable.getImports().contains(objectString) ? "invokestatic(" : "invokevirtual(")
                .append(objectString)
                .append(", \"")
                .append(name)
                .append("\"");

        for (String arg : visitedArgs) {
            ret.append(", ").append(arg);
        }
        ret.append(").");

        //return type
        String retType = this.functionRets.get(jmmNode);
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
                .append(leftSplit.get(1));

        return ret.toString();
    }

    private String dealWithLogicalAnd(JmmNode jmmNode, String s) {
        return "";
    }

    private String dealWithCompare(JmmNode jmmNode, String s) {
        return "";
    }

    private String dealWithBinaryOp(JmmNode jmmNode, String s) {
        StringBuilder ret = new StringBuilder(s);
        if (jmmNode.getJmmChild(0).getKind().equals("FunctionCall"))
            this.functionRets.put(jmmNode.getJmmChild(0), "i32");
        if (jmmNode.getJmmChild(1).getKind().equals("FunctionCall"))
            this.functionRets.put(jmmNode.getJmmChild(1), "i32");
        JmmNode left = jmmNode.getJmmChild(0), right = jmmNode.getJmmChild(1);
        String leftString = visit(left, ""), rightString = visit(right, "");


        List<String> nestedLeft = getNested(left, leftString, s), nestedRight = getNested(right, rightString, s);

        if (!nestedLeft.get(0).contains("\n")) ret.append(nestedLeft.get(0));
        else ret.append(nestedLeft.get(0)).append("\n");
        leftString = nestedLeft.get(1);


        if (!nestedRight.get(0).contains("\n")) ret.append(nestedRight.get(0));
        else ret.append(nestedRight.get(0)).append("\n");
        rightString = nestedRight.get(1);

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
                sub = str.substring(lastIndDot, str.indexOf(" "));
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

    private String dealWithNegation(JmmNode jmmNode, String s) {
        return "";
    }

    private String varAux(JmmNode jmmNode, Symbol var, boolean isArray) {
        if (!isArray) {
            String txt = var.getType().isArray() ? ".array" : "";
            return var.getName() + txt + "." + typesSwap(var.getType().getName()) + " :=." + typesSwap(var.getType().getName()) + txt + " ";
        }
        String access = visit(jmmNode, "");
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

    public String getOllirCode() {
        return ollirString;
    }

    private String getRetType(String name) {
        return symbolTable.getReturnType(name).getName();
    }
}
