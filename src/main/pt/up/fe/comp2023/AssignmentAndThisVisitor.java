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
    protected SemanticUtils utils;

    public AssignmentAndThisVisitor(SimpleTable simpleTable){
        this.simpleTable = simpleTable;
        this.utils = new SemanticUtils(simpleTable);
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
        while (!node.getKind().equals("Method") && !node.getKind().equals("MainMethod"))
            node = node.getJmmParent();
        if(node.getKind().equals("MainMethod"))
            utils.createReport(node, "Can only use this in static methods");
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
        Type left = utils.varCheck(node, "var");
        Type right = utils.getType(node.getJmmChild(0));
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
            utils.createReport(node, "Cannot assign to " + node.get("var") + " the type " + right.getName() + "!");
        }
        if(node.getJmmChild(1).getKind().equals("This") && !(left.getName().equals(simpleTable.getClassName()) || (simpleTable.getSuper() != null && simpleTable.getSuper().equals(left.getName())))){
            utils.createReport(node , "Cannot assign this!");
        }
        return null;
    }

    private Void dealWithAssignment(JmmNode node, Void _void) {
        Type left = utils.varCheck(node, "var");
        Type right = utils.getType(node.getJmmChild(0));

        System.out.println("-------exp type---------");
        System.out.println(right);
        System.out.println("------------------------");
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
            utils.createReport(node, "Cannot assign to " + node.get("var") + " the type " + right.getName() + "!");
        }
        if(node.getJmmChild(0).getKind().equals("This") && !(left.getName().equals(simpleTable.getClassName()) || (simpleTable.getSuper() != null && simpleTable.getSuper().equals(left.getName())))){
            utils.createReport(node , "Cannot assign this!");
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


    public List<Report> getReports(){
        return utils.getReports();
    }

    @Override
    public Void visit(JmmNode jmmNode) {
        return super.visit(jmmNode);
    }
}

