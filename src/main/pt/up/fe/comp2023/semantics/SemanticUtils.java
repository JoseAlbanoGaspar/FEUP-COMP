package pt.up.fe.comp2023.semantics;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2023.SimpleTable;

import java.util.ArrayList;
import java.util.List;

public class SemanticUtils {
    protected SimpleTable simpleTable;
    protected List<Report> reports;

    public SemanticUtils(SimpleTable simpleTable){
        this.simpleTable = simpleTable;
        this.reports = new ArrayList<>();
    }

    public List<Report> getReports(){ return this.reports;}

    public void createReport(JmmNode node, String message){
        reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC,Integer.parseInt(node.get("lineStart")),Integer.parseInt(node.get("colStart")), message));
    }

    public Type varCheck(JmmNode node, String attribute) {
        // check localVariables and parameters
        JmmNode aux = node;
        while(!aux.getKind().equals("Method") && !aux.getKind().equals("MainMethod") ){
            aux = aux.getJmmParent();
        }
        if(aux.getKind().equals("Method")) {
            for( Symbol symb : simpleTable.getLocalVariables(aux.get("name"))) {
                if (symb.getName().equals(node.get(attribute))) {
                    return symb.getType();
                }
            }
            for(Symbol symb : simpleTable.getParameters(aux.get("name"))){
                if (symb.getName().equals(node.get(attribute))) {
                    return symb.getType();
                }
            }
        }
        else{
            for( Symbol symb : simpleTable.getLocalVariables("main")) {
                if (symb.getName().equals(node.get(attribute))) {
                    return symb.getType();
                }
            }
            for(Symbol symb : simpleTable.getParameters("main")){
                if (symb.getName().equals(node.get(attribute))) {
                    return symb.getType();
                }
            }
        }

        // check if it is in fields
        for(Symbol symb : simpleTable.getFields()){
            if(symb.getName().equals(node.get(attribute))) {
                aux = node;
                while(!aux.getKind().equals("Method") && !aux.getKind().equals("MainMethod") ){
                    aux = aux.getJmmParent();
                }
                if(aux.getKind().equals("MainMethod")){
                    createReport(node, "Cannot access fields in static methods");
                }
                return symb.getType();
            }
        }
        // check imports
        for (String s : simpleTable.getImports()) {
            String[] parts = s.split("\\."); // split the string on "." character
            if(parts[parts.length-1].equals(node.get(attribute))) {
                return new Type(node.get(attribute), false);
            }

        }
        //check super
        if(node.get(attribute).equals(simpleTable.getSuper())){
            return new Type(simpleTable.getSuper(), false);
        }
        //check class name
        if(node.get(attribute).equals(simpleTable.getClassName())){
            return new Type(simpleTable.getClassName(), false);
        }

        return new Type("NotFound", false);
    }

    public Type getType(JmmNode node){
        switch (node.getKind()) {
            case "Not", "Compare", "LogicalAnd", "BoolLiteral" -> {
                return new Type("boolean", false);
            }
            case "BinaryOp", "Length", "Integer" -> {
                return new Type("int", false);
            }
            case "Parenthesis", "SquareBrackets" -> {
                return getType(node.getJmmChild(0));
            }
            case "NewArray" -> {
                return new Type(node.getJmmChild(0).get("typeName"), true);
            }
            case "NewClass" -> {
                return new Type(node.get("className"), false);
            }
            case "Identifier" -> {
                Type type = varCheck(node, "value");
                if(node.getJmmParent().getKind().equals("SquareBrackets")){
                    return new Type(type.getName(), false);
                }
                return type;
            }
            case "This" -> {
                while (!node.getKind().equals("ClassDeclaration"))
                    node = node.getJmmParent();
                return new Type(node.get("name"), false);
            }
            case "FunctionCall" -> {
                return dealWithFuncCall(node);
            }
            default -> {
                return new Type("", false);
            }
        }
    }

    private Type dealWithFuncCall(JmmNode node){
        Type calleeType = getType(node.getJmmChild(0));
        if(calleeType.getName().equals("NotFound")) {
            System.out.println("not exists");
            return calleeType;
        }
        if (!(simpleTable.getMethods().contains(node.get("methodName")) && calleeType.getName().equals(simpleTable.getClassName()))) {
            // check imports
            for (String s : simpleTable.getImports()) {
                String[] parts = s.split("\\."); // split the string on "." character
                if (parts[parts.length - 1].equals(calleeType.getName())) {
                    return getInferedFuncType(node);
                }
            }
            if (simpleTable.getSuper() != null && calleeType.getName().equals(simpleTable.getSuper())) {
                return getInferedFuncType(node);
            }
            else if(simpleTable.getSuper() != null && calleeType.getName().equals(simpleTable.getClassName())){
                return getInferedFuncType(node);
            }
            else {
                createReport(node, "Method " + node.get(("methodName")) + " does not exist!");
            }
        }
        return new Type("", false);
    }
    private Type getInferedFuncType(JmmNode node){
        if(node.getJmmParent().getKind().equals("StatementExpression"))
            return new Type("void", false);
        else if (node.getJmmParent().getKind().equals("Method")){  // check return type ??
            return simpleTable.getReturnType(node.getJmmParent().get("name"));
        }
        else if(!node.getJmmParent().getKind().equals("Assignment") && !node.getJmmParent().getKind().equals("Array")) {
            return getParentType(node);
        }
        else
            return varCheck(node.getJmmParent(), "var");
    }
    private Type getParentType(JmmNode node) {
        Type type = new Type("", false);
        switch (node.getJmmParent().getKind()) {
            case "Not", "LogicalAnd" -> type = new Type("boolean", false);
            case "BinaryOp", "Compare", "NewArray", "SquareBrackets" -> type = new Type("int", false);
            case "Parenthesis" -> type = getParentType(node.getJmmParent());
            case "Length" -> type = new Type("int", true);
            case "FunctionCall" ->{
                if(simpleTable.getMethods().contains(node.get("methodName"))){
                    for(int i = 0; i < node.getJmmParent().getChildren().size() - 1; i++){
                        if(node.getJmmParent().getChildren().get(i).equals(node)){
                            if(simpleTable.getParameters(node.get("methodName")).size() > i)
                                return simpleTable.getParameters(node.get("methodName")).get(i).getType();
                        }
                    }
                }
            }
        }
        return type;
    }
}
