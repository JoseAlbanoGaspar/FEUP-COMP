package pt.up.fe.comp2023.semantics;

import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp2023.SimpleTable;

import java.util.List;

public class OperandsTypeVisitor extends PreorderJmmVisitor<Void, Void> implements Reporter {
    protected SimpleTable simpleTable;
    protected SemanticUtils utils;

    public OperandsTypeVisitor(SimpleTable simpleTable){
        this.simpleTable = simpleTable;
        this.utils = new SemanticUtils(simpleTable);
    }

    @Override
    protected void buildVisitor() {
        setDefaultVisit(this::defaultVisitor);
        addVisit("If",this::dealWithIf);
        addVisit("While", this::dealWithWhile);
        addVisit("BinaryOp", this::dealWithBinaryOp);
        addVisit("Compare", this::dealWithCompare);
        addVisit("LogicalAnd", this::dealWithLogicalAnd);
    }

    private Void defaultVisitor(JmmNode jmmNode, Void _void){
        return null;
    }

    private Void dealWithLogicalAnd(JmmNode node, Void _void) {
        for(JmmNode child : node.getChildren()){
            if(!utils.getType(child).getName().equals("boolean")){
                utils.createReport(node, "One of the 2 operands do not evaluate as boolean");
            }
        }
        return null;
    }

    private Void dealWithCompare(JmmNode node, Void _void) {
        for(JmmNode child : node.getChildren()){
            if(!utils.getType(child).getName().equals("int")){
                utils.createReport(node, "One of the 2 operands do not evaluate as integer");
            }
        }
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

    private Void dealWithWhile(JmmNode node, Void _void) {
        if(!utils.getType(node.getJmmChild(0)).getName().equals("boolean"))
            utils.createReport(node, "If condition must evaluate to a boolean value!");
        return null;
    }

    private Void dealWithIf(JmmNode node, Void _void) {
        if(!utils.getType(node.getJmmChild(0)).getName().equals("boolean"))
            utils.createReport(node, "If condition must evaluate to a boolean value!");
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

