package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.*;

public class SemanticAnalyserVisitor extends PreorderJmmVisitor<Void, Void> implements Reporter{
    protected SimpleTable simpleTable;
    protected List<Report> reports;

    public SemanticAnalyserVisitor(SimpleTable simpleTable){
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
        /*JmmNode aux = node;
        while(!aux.getKind().equals("Method") && !aux.getKind().equals("MainMethod") ){
            aux = aux.getJmmParent();
        }
        if(aux.getKind().equals("MainMethod")) {
            createReport(node, "this cannot be used in static methods");
        }*/


        return null;
    }

    private Void dealWithIdentifier(JmmNode node, Void _void) {

       Type type = varCheck(node, "value");
        if(type.getName().equals("NotFound"))
            createReport(node, "Variable not declared: " + node.get("value"));
        /*else if(type.getName().equals("int")) {
            dealWithInteger(node, _void);
            if (node.getJmmParent().getKind().equals("If") || node.getJmmParent().getKind().equals("While")) {
                createReport(node, "Expressions in conditions must return a boolean!");
            }
        }
        else if(type.getName().equals("boolean")) {
            dealWithBool(node, _void);
            if(node.getJmmParent().getKind().equals("SquareBrackets") ||
                    (node.getJmmParent().getKind().equals("Array") &&
                    node.getJmmParent().getJmmChild(0).equals(node))){
                createReport(node, "Array index expression must be integer!");
            }
        }
        else{ //import or class types
            if(node.getJmmParent().getKind().equals("BinaryOp")){
                createReport(node, node.get("value") + " cannot be used in arithmetic operations!");
            }
            else if (node.getJmmParent().getKind().equals("LogicalAnd") || node.getJmmParent().getKind().equals("Not")){
                createReport(node, node.get("value") + " cannot be used in boolean operations!");
            }
        }

        if(type.isArray())
            isArrayInBinaryOp(node);*/
        return null;
    }

    private Void dealWithBool(JmmNode node, Void _void) {

        /* JmmNode parent = node.getJmmParent();

        if(parent.getKind().equals("BinaryOp") || parent.getKind().equals("Compare")){
            createReport(node, "The value < " +node.get("value") + " > is not integer!");
        }*/
        return null;
    }

    private Void dealWithInteger(JmmNode node, Void _void) {

        /*JmmNode parent = node.getJmmParent();

        if(parent.getKind().equals("LogicalAnd"))
            createReport(node, "The value < " +node.get("value") + " > is not boolean!");
*/
        return null;
    }

    private Void dealWithNewClass(JmmNode node, Void _void) {

        return null; }

    private Void dealWithNewArray(JmmNode node, Void _void) {

        return null;
    }

    private Void dealWithFunctionCall(JmmNode node, Void _void) {
        Type type = getType(node);
        System.out.println("-------------------");
        System.out.println(type);
        System.out.println(node);
        System.out.println("-------------------");

        return null;
    }

    private Void dealWithLength(JmmNode node, Void _void) {

        /* JmmNode parent = node.getJmmParent();
        if(!parent.getKind().equals("BinaryOp") || !parent.getKind().equals("Compare")){
            createReport(node, "Length returns an integer: Bool expected!");
            return null;
        }*/
        return null;
    }

    private Void dealWithSquareBrackets(JmmNode node, Void _void) {

        /*if(node.getJmmChild(0).getKind().equals("Identifier"))
            checkIndexedVarIsArray(node.getChildren().get(0), "value");
        if(!node.getJmmChild(1).getKind().equals("BinaryOp") &&
           !node.getJmmChild(1).getKind().equals("Integer")  &&
           !node.getJmmChild(1).getKind().equals("Identifier"))  //note : in dealWithIdentifier are made the verifications about the type of identifier
            createReport(node, "Array index expression must be integer!");*/
        return null;
    }

    private Void dealWithLogicalAnd(JmmNode node, Void _void) {
        return null;
    }

    private Void dealWithCompare(JmmNode node, Void _void) {
        /*JmmNode parent = node.getJmmParent();
        if(parent.getKind().equals("BinaryOp")){
            createReport(node, "Boolean not allowed as arithmetic member!");
            return null;
        }*/
        return null;
    }

    private Void dealWithBinaryOp(JmmNode node, Void _void) {
        for(JmmNode child : node.getChildren()){
            Type type = getType(child);
            if(!type.getName().equals("int")){
                createReport(node, "One of the 2 operands do not evaluate as integer!");
            }

        }
        /* JmmNode parent = node.getJmmParent();
        if(parent.getKind().equals("Compare")) {
            createReport(node, "Boolean not allowed as arithmetic member!");
            return null;
        }
        if(parent.getKind().equals("LogicalAnd")) {
            createReport(node, "Integer used in && operator!");
            return null;
        }*/

        return null;
    }

    private Void dealWithParenthesis(JmmNode node, Void _void) {

        /*JmmNode child = node.getJmmChild(0);
        JmmNode parent = node.getJmmParent();
        if(child.getKind().equals("BinaryOp")){
            if(parent.getKind().equals("LogicalAnd")){
                createReport(node, "Integer used in && operator!");
            }
        }
        if(child.getKind().equals("Compare")){
            if(parent.getKind().equals("BinaryOp")){
                createReport(node, "Boolean used in " + parent.get("op") + " operator!");
            }
        }
        if(child.getKind().equals("LogicalAnd")){
            if(parent.getKind().equals("BinaryOp")){
                createReport(node, " <bool> " + parent.get("op") + " int not possible");
            }
            if(parent.getKind().equals("Compare")){
                createReport(node, "< operator must receive 2 integers, one bool given");
            }
        }
        if(child.getKind().equals("Length")){
            if(parent.getKind().equals("Compare") || parent.getKind().equals("Not") || parent.getKind().equals("LogicalAnd"))
                createReport(node, "Length returns an integer and it's passed instead of a boolean");
        }*/
        return null;
    }

    private Void dealWithNegation(JmmNode node, Void _void) {

        /*if(!node.getJmmChild(0).getKind().equals("Compare") && !node.getJmmChild(0).getKind().equals("LogicalAnd")){
            if(!node.getJmmChild(0).getKind().equals("Parenthesis")){
                createReport(node, "Cannot negate integers");
            }
            else{
                if(!node.getJmmChild(0).getJmmChild(0).getKind().equals("Compare") && !node.getJmmChild(0).getJmmChild(0).getKind().equals("LogicalAnd")) {
                    createReport(node, "Cannot negate integers");
                }
            }
        }*/
        return null;
    }

    private Void dealWithArray(JmmNode node, Void _void) {

        /*Type type = varCheck(node, "var");
        if(type.getName().equals("NotFound"))
            createReport(node, "Variable not declared: " + node.get("var"));

        //checks if array expression is integer
        if(!node.getJmmChild(0).getKind().equals("BinaryOp") &&
                !node.getJmmChild(0).getKind().equals("Integer")  &&
                !node.getJmmChild(0).getKind().equals("Identifier"))  //note : in dealWithIdentifier are made the verifications about the type of identifier
            createReport(node, "Array index expression must be integer!");

        //checks type compatibility in assignments
        assignmentCheck(node.getJmmChild(1), type);*/


        return null;
    }

    private Void dealWithAssignment(JmmNode node, Void _void) {
        /*Type type = varCheck(node, "var");
        if(type.getName().equals("NotFound"))
            createReport(node, "Variable not declared: " + node.get("var"));

        //this used as object verification
        if(node.getJmmChild(0).getKind().equals("This")){
            JmmNode aux = node;
            while(!aux.getKind().equals("ClassDeclaration")){
                aux = aux.getJmmParent();
            }
            if(!type.getName().equals(aux.get("name"))){
                if(!aux.hasAttribute("superName")){
                    createReport(node, "this cannot be assigned to " + node.get("var"));
                }
                else if(!type.getName().equals(aux.get("superName"))){
                    createReport(node, "this cannot be assigned to " + node.get("var"));
                }
            }
        }
        assignmentCheck(node.getJmmChild(0), type);*/
        return null;
    }

    private Void dealWithStatementExpression(JmmNode node, Void _void) {
        return null;
    }

    private Void dealWithWhile(JmmNode node, Void _void) {
        //checkBoolExpInConditions(node);
        return null;
    }

    private Void dealWithIf(JmmNode node, Void _void) {
        //checkBoolExpInConditions(node);
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
            for(Symbol symb : simpleTable.getParameters(aux.get("name"))){
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
            case "Identifier" -> type = varCheck(node, "value");
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
