package pt.up.fe.comp2023.semantics;

import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp2023.SimpleTable;

import java.util.*;

public class SemanticAnalyserVisitor extends PreorderJmmVisitor<Void, Void> implements Reporter {
    protected SimpleTable simpleTable;
    protected SemanticUtils utils;

    public SemanticAnalyserVisitor(SimpleTable simpleTable){
        this.simpleTable = simpleTable;
        this.utils = new SemanticUtils(simpleTable);
    }

    @Override
    protected void buildVisitor() {
        setDefaultVisit(this::defaultVisitor);
        addVisit("FunctionCall", this::dealWithFunctionCall);
        addVisit("Identifier", this::dealWithIdentifier);
    }

    private Void defaultVisitor(JmmNode jmmNode, Void _void){
        return null;
    }

    private Void dealWithIdentifier(JmmNode node, Void _void) {
        // see if it's declared
       Type type = utils.varCheck(node,"value");
        if(type.getName().equals("NotFound"))
            utils.createReport(node, "Variable not declared: " + node.get("value"));

        return null;
    }

    private Void dealWithFunctionCall(JmmNode node, Void _void) {
        utils.getType(node);
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
