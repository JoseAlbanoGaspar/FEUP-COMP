package pt.up.fe.comp2023.semantics;

import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp2023.SimpleTable;

import java.util.List;

public class SemanticArrayVisitor extends PreorderJmmVisitor<Void, Void> implements Reporter {
    protected SimpleTable simpleTable;
    protected SemanticUtils utils;

    public SemanticArrayVisitor(SimpleTable simpleTable){
        this.simpleTable = simpleTable;
        this.utils = new SemanticUtils(simpleTable);
    }

    @Override
    protected void buildVisitor() {
        addVisit("Program", this::defaultVisitor);
        addVisit("ImportDeclaration", this::defaultVisitor);
        addVisit("ClassDeclaration", this::defaultVisitor);
        addVisit("VarDeclaration", this::defaultVisitor);
        addVisit("MainMethod", this::defaultVisitor);
        addVisit("Method", this::defaultVisitor);
        addVisit("Type", this::defaultVisitor);
        addVisit("BlockCode", this::defaultVisitor);
        addVisit("If",this::defaultVisitor);
        addVisit("While", this::defaultVisitor);
        addVisit("StatementExpression", this::defaultVisitor);
        addVisit("Assignment", this::defaultVisitor);
        addVisit("Array", this::dealWithArray);
        addVisit("Not", this::defaultVisitor);
        addVisit("Parenthesis", this::defaultVisitor);
        addVisit("BinaryOp", this::dealWithBinaryOp);
        addVisit("Compare", this::defaultVisitor);
        addVisit("LogicalAnd", this::defaultVisitor);
        addVisit("SquareBrackets", this::dealWithSquareBrackets);
        addVisit("Length", this::defaultVisitor);
        addVisit("FunctionCall", this::defaultVisitor);
        addVisit("NewArray", this::dealWithNewArray);
        addVisit("NewClass", this::defaultVisitor);
        addVisit("Integer", this::defaultVisitor);
        addVisit("BoolLiteral", this::defaultVisitor);
        addVisit("Identifier", this::defaultVisitor);
        addVisit("This", this::defaultVisitor);
        addVisit("MethodArgs", this::defaultVisitor);
    }

    private Void defaultVisitor(JmmNode jmmNode, Void _void){
        return null;
    }

    private Void dealWithNewArray(JmmNode node, Void _void) {
        if(!utils.getType(node.getJmmChild(1)).getName().equals("int"))
            utils.createReport(node,"Index expression must be an integer");
        return null;
    }

    private Void dealWithSquareBrackets(JmmNode node, Void _void) {
        if(!node.getJmmChild(0).getKind().equals("Identifier")){
            utils.createReport(node, "Trying to index something that is not an identifier");
        }
        else if(!utils.varCheck(node.getJmmChild(0), "value").isArray())
            utils.createReport(node,"Cannot index a variable that is not an array!");
        if(!utils.getType(node.getJmmChild(1)).getName().equals("int"))
            utils.createReport(node,"Index expression must be an integer");
        return null;
    }

    private Void dealWithBinaryOp(JmmNode node, Void _void) {
        for(JmmNode child : node.getChildren()){
            Type type = utils.getType(child);
            if(type.isArray()){
                utils.createReport(node, "Arrays cannot be used in arithmetic operations!");
            }
        }
        return null;
    }

    private Void dealWithArray(JmmNode node, Void _void) {
        if(!utils.varCheck(node, "var").isArray())
            utils.createReport(node,"Cannot index a variable that is not an array!");
        if(!utils.getType(node.getJmmChild(0)).getName().equals("int"))
            utils.createReport(node,"Index expression must be an integer");
        return null;
    }

    public List<Report> getReports(){
        return this.utils.getReports();
    }

    @Override
    public Void visit(JmmNode jmmNode) {
        return super.visit(jmmNode);
    }
}

