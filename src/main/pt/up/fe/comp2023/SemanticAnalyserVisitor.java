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
    private Map<String, List<String>> assigns = new HashMap<>();

    public SemanticAnalyserVisitor(SimpleTable simpleTable){
        this.simpleTable = simpleTable;
        this.utils = new SemanticUtils(simpleTable);

        for (String method : simpleTable.getMethods()) {
            assigns.put(method, new ArrayList<>());
        }
        assigns.put("fields", new ArrayList<>());
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

        return null;
    }

    private Void dealWithIdentifier(JmmNode node, Void _void) {
        // see if it's declared
       Type type = utils.varCheck(node,"value");
        if(type.getName().equals("NotFound"))
            utils.createReport(node, "Variable not declared: " + node.get("value"));
        // see if it is initialized
        ////////////////////////////////////////
        // see if assigned (left) var is a field!
        for (Symbol field : simpleTable.getFields()){
            if (field.getName().equals(node.get("value"))){
                return null;
            }
        }
        // else
        JmmNode aux = node;
        while (!aux.getKind().equals("Method") && !aux.getKind().equals("MainMethod")) {
            aux = aux.getJmmParent();
        }
        String methodName = "main";
        if (aux.getKind().equals("Method")) {
            methodName = aux.get("name");
        }

        boolean isParameter = false;
        for ( Symbol parameter : simpleTable.getParameters(methodName)){
            if(parameter.getName().equals(node.get("value"))) {
                isParameter = true;
                break;
            }
        }
        boolean isClassName = false;
        if (aux.getJmmParent().get("name").equals(node.get("value")))
            isClassName = true;

        boolean isSuper = false;
        if (aux.getJmmParent().hasAttribute("superName") && node.get("value").equals(aux.get("superName")))
            isSuper = true;

        boolean isImport = false;
        for (String s : simpleTable.getImports()) {
            String[] parts = s.split("\\."); // split the string on "." character
            if(parts[parts.length-1].equals(node.get("value"))) {
                isImport = true;
                break;
            }

        }

        if(!isParameter && !isClassName && !isSuper && !isImport && !assigns.get(methodName).contains(node.get("value"))){
            utils.createReport(node, "Used uninitialized variable " + node.get("value") + " in expression!");
        }


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

        return null;
    }

    private Void dealWithParenthesis(JmmNode node, Void _void) {

        return null;
    }

    private Void dealWithNegation(JmmNode node, Void _void) {

        return null;
    }

    private Void dealWithArray(JmmNode node, Void _void) {
        // see if assigned (left) var is a field!

        // else
        JmmNode aux = node;
        while (!aux.getKind().equals("Method") && !aux.getKind().equals("MainMethod")) {
            aux = aux.getJmmParent();
        }
        if (aux.getKind().equals("Method")) {
            assigns.get(aux.get("name")).add(node.get("var"));
        }
        else {
            assigns.get("main").add(node.get("var"));
        }
        return null;
    }

    private Void dealWithAssignment(JmmNode node, Void _void) {
        // see if assigned (left) var is a field!

        // else
        JmmNode aux = node;
        while (!aux.getKind().equals("Method") && !aux.getKind().equals("MainMethod")) {
            aux = aux.getJmmParent();
        }
        if (aux.getKind().equals("Method")) {
            assigns.get(aux.get("name")).add(node.get("var"));
        }
        else {
            assigns.get("main").add(node.get("var"));
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
