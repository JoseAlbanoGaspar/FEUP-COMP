package pt.up.fe.comp2023.ollir;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.max;

public class OllirUtils {
    private final OllirVisitor visitor;
    public OllirUtils(OllirVisitor visitor){
        this.visitor  = visitor;
    }

    public String typesSwap(String str) {
        return switch (str) {
            case "int" -> "i32";
            case "boolean" -> "bool";
            case "void" -> "V";
            case "false" -> "0.bool";
            case "true" -> "1.bool";
            case "import" -> "";
            case "int array" -> "array.i32";
            default -> str;
        };
    }
    public String nestedAppend(JmmNode jmmNode, String s, StringBuilder ret){
        return nestedAppend(jmmNode, s, ret, "");
    }

    public String nestedAppend(JmmNode jmmNode, String s, StringBuilder ret, String data){
        String lastString = visitor.visit(jmmNode, data);
        List<String> lasStringList = getNested(jmmNode, lastString, s);

        if(!(lasStringList.get(0).equals("") || lasStringList.get(0).contains("\n"))){
            lasStringList.set(0, lasStringList.get(0)+"\n");
        }

        if (!lasStringList.get(0).contains("\n")) ret.append(lasStringList.get(0));
        else ret.append(lasStringList.get(0));

        lastString = lasStringList.get(1);
        if(jmmNode.getKind().equals("SquareBrackets")){
            ret.append("t").append(visitor.tempCnt).append(".i32 :=.i32 ")
                    .append(lastString)
                    .append(";\n").append(s);
            return "t" + visitor.tempCnt++ + ".i32";
        }
        return lastString;
    }

    public String assignChildrenCases(JmmNode jmmNode, JmmNode assig, String s, StringBuilder ret, Symbol var, boolean isParameter, int parNum) {
        String assignString="";
        if (assig.getKind().equals("BinaryOp") ||assig.getKind().equals("Compare") || assig.getKind().equals("LogicalAnd")) {
            String op = visitor.visit(assig, "");
            List<String> rows = List.of(op.split("\n"));
            for (int i = 0; i < rows.size(); i++) {
                if (i == rows.size() - 1) {
                    ret.append(s).append(varAux(var, isParameter, parNum));

                    ret.append(rows.get(i)).append(";\n");
                } else {
                    ret.append(rows.get(i)).append("\n");
                }
            }
        }else if(assig.getKind().equals("NewArray")){
            ret.append(varAux(var, isParameter, parNum));
            String assignmentString = nestedAppend(assig, s, ret);
            ret.append(assignmentString).append(";\n");
        } else if (assig.getKind().equals("NewClass")) {
            ret.append(varAux(var, isParameter, parNum));
            String data = var.getName() + "." + typesSwap(var.getType().getName());
            String assignmentString = nestedAppend(assig, s, ret, data);
            ret.append(assignmentString).append(";\n");
        } else {
            if (assig.getKind().equals("FunctionCall")){
                String varToSwap = var.getType().isArray() ? "int array" : var.getType().getName();
                visitor.functionRets.put(jmmNode.getJmmChild(0), typesSwap(varToSwap));
            }
            assignString = visitor.visit(assig, "");
            if (assig.getKind().equals("SquareBrackets")) {
                assignString = nestedAppend(assig, s, ret);
            } else if (assig.getKind().equals("FunctionCall")) {
                assignString = nestedAppend(assig, s, ret);
            }
        }
        return assignString;
    }

    public List<String> getNested(JmmNode jmmNode, String str, String s) {
        List<String> retList = new ArrayList<>();
        String kind = jmmNode.getKind();
        StringBuilder newStr = new StringBuilder(str);
        StringBuilder auxString = new StringBuilder();
        if (kind.equals("BinaryOp") || kind.equals("FunctionCall")
                || kind.equals("SquareBrackets")
                || kind.equals("Length") || kind.equals("Compare") || kind.equals("LogicalAnd")
                || kind.equals("Not")
                || (kind.equals("Parenthesis") && jmmNode.getJmmChild(0).getKind().equals("FunctionCall"))){
            String sub, before = "";
            int lastIndDot = str.lastIndexOf(".");
            if (str.contains(":=.")) {
                sub = str.substring(lastIndDot);
                if(kind.equals("FunctionCall")){
                    sub = str.substring(str.lastIndexOf(").")+1);
                }
                if (str.contains("\n")) {
                    int lastIndN = str.lastIndexOf("\n");
                    before = str.substring(0, lastIndN + 1);
                    newStr = new StringBuilder(str.substring(lastIndN + 1));
                } else newStr = new StringBuilder(str.substring(str.lastIndexOf(" ")));
            } else sub = str.substring(lastIndDot);

            if (jmmNode.getKind().equals("SquareBrackets")) {
                auxString = new StringBuilder(before);
                if (newStr.toString().contains(":=."))
                    newStr = new StringBuilder(newStr.substring(0, newStr.indexOf(" ")));
                else newStr = new StringBuilder(newStr.substring(0, max(newStr.length() - 1, 0)));
            } else {
                sub = jmmNode.getKind().equals("Compare") || jmmNode.getKind().equals("LogicalAnd") ? ".bool" : sub;
                auxString.append(before)
                        .append("t").append(visitor.tempCnt)
                        .append(sub)
                        .append(" :=")
                        .append(sub)
                        .append(" ")
                        .append(newStr)
                        .append(";\n");
                newStr = new StringBuilder(s + "t" +
                        visitor.tempCnt++
                        + sub);
            }
        } else if (kind.equals("Parenthesis") &&
                (jmmNode.getJmmChild(0).getKind().equals("NewClass") || jmmNode.getJmmChild(0).getKind().equals("NewArray"))) {
            auxString = newStr;
            auxString.append(";\n");
            int index = newStr.indexOf(":=.");
            newStr = new StringBuilder(newStr.substring(0, index));

        } else if(kind.equals("Parenthesis")){
            if(newStr.toString().contains("\n")){
                String[] strings = newStr.toString().split("\n");
                newStr = new StringBuilder(strings[0]);
            }
            auxString = newStr;
            if(!auxString.toString().endsWith(";"))
                auxString.append(";\n");
            int index = newStr.indexOf(":=.");
            newStr = new StringBuilder(newStr.substring(0, index));
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
    public String varAux(Symbol var, boolean isParameter, int parNumber) {
        String txt = var.getType().isArray() ? ".array" : "";
        String paramStr = isParameter ? "$"+parNumber+"." : "";
        return paramStr + var.getName() + txt + "." + typesSwap(var.getType().getName()) + " :=" + txt + "." + typesSwap(var.getType().getName())  + " ";
    }
    public String getRetType(String name) {
        if(visitor.symbolTable.getReturnType(name).isArray()) return "array.i32";
        return visitor.symbolTable.getReturnType(name).getName();
    }

    public String findParentName(JmmNode jmmNode) {
        JmmNode parent = jmmNode;
        String parentName;

        do {
            parent = parent.getJmmParent();
            if (parent.getKind().equals("Method") || parent.getKind().equals("MainMethod"))
                break;
        } while (!parent.getKind().equals("ClassDeclaration"));
        parentName = parent.getKind().equals("MainMethod") ? "main" : parent.get("name");
        return parentName;
    }

    public VarRecord findVar(JmmNode jmmNode, String parentName, String searchString){
        Symbol var = null;
        int parNum=0;
        boolean isField = false, isParameter = false, isImported = false, isInMethod = false;

        if(jmmNode.getJmmParent().getKind().equals("MainMethod")){
            for (Symbol vari : visitor.symbolTable.getLocalVariables("main")) { //check if local
                if (vari.getName().equals(jmmNode.get(searchString))){
                    var = vari;
                    return new VarRecord(var, parNum, isField, isParameter, isImported, isInMethod);
                }
            }

            for (Symbol symbol1 : visitor.symbolTable.getFields()) { //check if field
                if (symbol1.getName().equals(jmmNode.get(searchString))){
                    var = symbol1;
                    isField = true;
                    return new VarRecord(var, parNum, isField, isParameter, isImported, isInMethod);
                }
            }

        }

        //check local
        for (Symbol vari : visitor.symbolTable.getLocalVariables(parentName)) { //check if local
            if (vari.getName().equals(jmmNode.get(searchString))){
                var = vari;
                isInMethod = true;
                return new VarRecord(var, parNum, isField, isParameter, isImported, isInMethod);
            }
        }

        //check parameter
        for (Symbol vari : visitor.symbolTable.getParameters(parentName)) { //check if parameter
            if (vari.getName().equals(jmmNode.get(searchString))){
                var = vari;
                isParameter = true;
                parNum++;
                return new VarRecord(var, parNum, isField, isParameter, isImported, isInMethod);
            }
        }

        //check field
        for (Symbol symbol1 : visitor.symbolTable.getFields()) { //check if field
            if (symbol1.getName().equals(jmmNode.get(searchString))){
                var = symbol1;
                isField = true;
                return new VarRecord(var, parNum, isField, isParameter, isImported, isInMethod);
            }
        }

        //chek is import
        for (String imp : visitor.symbolTable.getImports()) {
            if (imp.equals(jmmNode.get(searchString))) {
                isImported = true;
                var = new Symbol(new Type("import", false), jmmNode.get("value"));
            }
        }
        return new VarRecord(var, parNum, isField, isParameter, isImported, isInMethod);
    }
}
