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
        setDefaultVisit(this::defaultVisitor);
        addVisit("Program", this::defaultVisitor);
        addVisit("Array", this::dealWithArray);
        addVisit("BinaryOp", this::dealWithBinaryOp);
        addVisit("SquareBrackets", this::dealWithSquareBrackets);
        addVisit("NewArray", this::dealWithNewArray);
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

