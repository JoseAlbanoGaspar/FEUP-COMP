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
    protected SemanticUtils utils;

    public SemanticAnalyserVisitor(SimpleTable simpleTable){
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

       Type type = utils.varCheck(node,"value");
        if(type.getName().equals("NotFound"))
            utils.createReport(node, "Variable not declared: " + node.get("value"));
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
        utils.getType(node);
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
        for(JmmNode child : node.getChildren()){
            Type type = utils.getType(child);
            if(!type.getName().equals("int")){
                utils.createReport(node, "One of the 2 operands do not evaluate as integer!");
            }

        }


        return null;
    }

    private Void dealWithParenthesis(JmmNode node, Void _void) {

        return null;
    }

    private Void dealWithNegation(JmmNode node, Void _void) {

        return null;
    }

    private Void dealWithArray(JmmNode node, Void _void) {

        return null;
    }

    private Void dealWithAssignment(JmmNode node, Void _void) {
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
