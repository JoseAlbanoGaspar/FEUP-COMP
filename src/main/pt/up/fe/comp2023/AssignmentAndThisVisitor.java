package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.ArrayList;
import java.util.List;

public class AssignmentAndThisVisitor extends PreorderJmmVisitor<Void, Void> implements Reporter{
    protected SimpleTable simpleTable;
    protected List<Report> reports;

    public AssignmentAndThisVisitor(SimpleTable simpleTable){
        this.simpleTable = simpleTable;
        this.reports = new ArrayList<>();
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

    private Void dealWithType(JmmNode jmmNode, Void unused) {
        return null;
    }

    private Void dealWithThis(JmmNode node, Void _void) {
        while (!node.getKind().equals("MethodDeclaration") && !node.getKind().equals("MainDeclaration"))
            node = node.getJmmParent();
        if(!node.getKind().equals("MainDeclaration"))
            createReport(node, "Can only use this in static methods");
        return null;
    }

    private Void dealWithIdentifier(JmmNode node, Void _void) {
        return null;
    }

    private Void dealWithBool(JmmNode node, Void _void) {
        return null;
    }

    private Void dealWithInteger(JmmNode node, Void _void) {
        return null;
    }

    private Void dealWithNewClass(JmmNode node, Void _void) {
        return null; }

    private Void dealWithNewArray(JmmNode node, Void _void) {
        return null;
    }

    private Void dealWithFunctionCall(JmmNode node, Void _void) {
        return null;
    }

    private Void dealWithLength(JmmNode node, Void _void) {

        return null;
    }

    private Void dealWithSquareBrackets(JmmNode node, Void _void) {
        return null;
    }

    private Void dealWithLogicalAnd(JmmNode node, Void _void) {
        return null;
    }

    private Void dealWithCompare(JmmNode node, Void _void) {
        return null;
    }

    private Void dealWithBinaryOp(JmmNode node, Void _void) {
        return null;
    }

    private Void dealWithParenthesis(JmmNode node, Void _void) {
        return null;
    }

    private Void dealWithNegation(JmmNode node, Void _void) {
        return null;
    }

    private Void dealWithArray(JmmNode node, Void _void) {
        Type left = varCheck(node, "var");
        Type right = getType(node.getJmmChild(0));
        boolean notImportedOrExtended = true;

        //check if right and left are both imported
        List<String> importedClasses = new ArrayList<>();
        for (String s : simpleTable.getImports()) {
            String[] parts = s.split("\\."); // split the string on "." character
            importedClasses.add(parts[parts.length - 1]);
        }
        if(importedClasses.contains(left.getName()) && importedClasses.contains(right.getName())){
            notImportedOrExtended = false;
        }
        //check if right extends left
        if(simpleTable.getSuper() != null && left.getName().equals(simpleTable.getSuper()) && importedClasses.contains(left.getName())) {
            notImportedOrExtended = false;
        }
        if(!left.getName().equals(right.getName()) && notImportedOrExtended){
            createReport(node, "Cannot assign to " + node.get("var") + " the type " + right.getName() + "!");
        }
        return null;
    }

    private Void dealWithAssignment(JmmNode node, Void _void) {
        Type left = varCheck(node, "var");
        Type right = getType(node.getJmmChild(0));
        boolean notImportedOrExtended = true;

        //check if right and left are both imported
        List<String> importedClasses = new ArrayList<>();
        for (String s : simpleTable.getImports()) {
            String[] parts = s.split("\\."); // split the string on "." character
            importedClasses.add(parts[parts.length - 1]);
        }
        if(importedClasses.contains(left.getName()) && importedClasses.contains(right.getName())){
            notImportedOrExtended = false;
        }
        //check if right extends left
        if(simpleTable.getSuper() != null && left.getName().equals(simpleTable.getSuper()) && importedClasses.contains(left.getName())) {
            notImportedOrExtended = false;
        }
        if(!left.equals(right) && notImportedOrExtended){
            createReport(node, "Cannot assign to " + node.get("var") + " the type " + right.getName() + "!");
        }
        return null;
    }

    private Void dealWithStatementExpression(JmmNode node, Void _void) {
        return null;
    }

    private Void dealWithWhile(JmmNode node, Void _void) {
        return null;
    }

    private Void dealWithIf(JmmNode node, Void _void) {
        return null;
    }

    private Void dealWithBlockCode(JmmNode node, Void _void) {
        return null;
    }

    private Void dealWithMethod(JmmNode node, Void _void) {
        return null;
    }
    private Void dealWithMethodArgs(JmmNode node, Void unused) {

        return null;
    }
    private Void dealWithMainMethod(JmmNode node, Void _void) {

        return null;
    }

    private Void dealWithVarDeclaration(JmmNode node, Void _void) {

        return null;
    }

    private Void dealWithClassDeclaration(JmmNode node, Void _void) {
        return null;
    }


    private Void dealWithImportDeclaration(JmmNode node, Void _void) {

        return null;
    }

    private Void dealWithProgram(JmmNode node, Void _void) {
        return null;
    }

    private Void createReport(JmmNode node, String message){
        reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC,Integer.parseInt(node.get("lineStart")),Integer.parseInt(node.get("colStart")), message));
        return null;
    }


    private Type varCheck(JmmNode node, String attribute) {
        // check if it is in fields
        for(Symbol symb : simpleTable.getFields()){
            if(symb.getName().equals(node.get(attribute))) {
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
        // check localVariables and parameters
        JmmNode aux = node;
        while(!aux.getKind().equals("Method") && !aux.getKind().equals("MainMethod") ){
            aux = aux.getJmmParent();
        }
        if(aux.getKind().equals("Method")) {
            for(Symbol symb : simpleTable.getParameters(aux.get("name"))){
                if (symb.getName().equals(node.get(attribute))) {
                    return symb.getType();
                }
            }
            for( Symbol symb : simpleTable.getLocalVariables(aux.get("name"))) {
                if (symb.getName().equals(node.get(attribute))) {
                    return symb.getType();
                }
            }
        }
        else{
            for(Symbol symb : simpleTable.getParameters("main")){
                if (symb.getName().equals(node.get(attribute))) {
                    return symb.getType();
                }
            }
            for( Symbol symb : simpleTable.getLocalVariables("main")) {
                if (symb.getName().equals(node.get(attribute))) {
                    return symb.getType();
                }
            }
        }
        return new Type("NotFound", false);
    }

    public Type getType(JmmNode node){
        Type type = new Type("", false);
        switch (node.getKind()) {
            case "Not", "Compare", "LogicalAnd", "BoolLiteral" -> type = new Type("boolean", false);
            case "BinaryOp", "Length", "Integer" -> type = new Type("int", false);
            case "Parenthesis", "SquareBrackets" -> type = getType(node.getJmmChild(0));
            case "NewArray" -> type = new Type(node.getJmmChild(0).get("typeName"), true);
            case "NewClass" -> type = new Type(node.get("className"), false);
            case "Identifier" -> {
                type = varCheck(node, "value");
                if(node.getJmmParent().getKind().equals("SquareBrackets")){
                    type = new Type(type.getName(), false);
                }
            }
            case "This" -> {
                while (!node.getKind().equals("ClassDeclaration"))
                    node = node.getJmmParent();
                type = new Type(node.get("name"), false);
            }
            case "FunctionCall" -> {
                Type calleeType = getType(node.getJmmChild(0));
                if(calleeType.getName().equals("NotFound")) return calleeType;
                if (simpleTable.getMethods().contains(node.get("methodName"))) {
                    type = simpleTable.getReturnType(node.get("methodName"));
                } else {
                    // check imports
                    for (String s : simpleTable.getImports()) {
                        String[] parts = s.split("\\."); // split the string on "." character
                        if (parts[parts.length - 1].equals(calleeType.getName())) {
                            if(node.getJmmParent().getKind().equals("StatementExpression"))
                                return new Type("void", false);
                            else if (node.getJmmParent().getKind().equals("Method")){  // check return type ??
                                return simpleTable.getReturnType(node.getJmmParent().get("name"));
                            }
                            else return varCheck(node.getJmmParent(), "var");
                        }
                    }
                    if (simpleTable.getSuper() != null && calleeType.getName().equals(simpleTable.getSuper())) {
                        if (node.getJmmParent().getKind().equals("StatementExpression")) {
                            type = new Type("void", false);
                        } else if (node.getJmmParent().getKind().equals("Method")){  // check return type ??
                            type = simpleTable.getReturnType(node.getJmmParent().get("name"));
                        }
                        else{
                            type = varCheck(node.getJmmParent(), "var");
                        }
                    }
                    else if(simpleTable.getSuper() != null && calleeType.getName().equals(simpleTable.getClassName())){
                        if (node.getJmmParent().getKind().equals("StatementExpression")) {
                            type = new Type("void", false);
                        } else if (node.getJmmParent().getKind().equals("Method")){  // check return type ??
                            type = simpleTable.getReturnType(node.getJmmParent().get("name"));
                        }
                        else{
                            type = varCheck(node.getJmmParent(), "var");
                        }
                    }
                    else {
                        createReport(node, "Method " + node.get(("methodName")) + " does not exist!");
                    }
                }
            }
        }
        return type;
    }
    public List<Report> getReports(){
        return this.reports;
    }

    @Override
    public Void visit(JmmNode jmmNode) {
        return super.visit(jmmNode);
    }
}

