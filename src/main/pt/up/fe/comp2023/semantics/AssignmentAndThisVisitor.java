package pt.up.fe.comp2023.semantics;

import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp2023.SimpleTable;

import java.util.ArrayList;
import java.util.List;

public class AssignmentAndThisVisitor extends PreorderJmmVisitor<Void, Void> implements Reporter {
    protected SimpleTable simpleTable;
    protected SemanticUtils utils;

    public AssignmentAndThisVisitor(SimpleTable simpleTable){
        this.simpleTable = simpleTable;
        this.utils = new SemanticUtils(simpleTable);
    }

    @Override
    protected void buildVisitor() {
        addVisit("Program", this::defaultVisitor);
        addVisit("ImportDeclaration", this::defaultVisitor);
        addVisit("ClassDeclaration", this::defaultVisitor);
        addVisit("VarDeclaration", this::defaultVisitor);
        addVisit("MainMethod",this::defaultVisitor);
        addVisit("Method", this::defaultVisitor);
        addVisit("Type", this::defaultVisitor);
        addVisit("BlockCode", this::defaultVisitor);
        addVisit("If",this::defaultVisitor);
        addVisit("While", this::defaultVisitor);
        addVisit("StatementExpression", this::defaultVisitor);
        addVisit("Assignment", this::dealWithAssignment);
        addVisit("Array", this::dealWithArray);
        addVisit("Not",this::defaultVisitor);
        addVisit("Parenthesis", this::defaultVisitor);
        addVisit("BinaryOp", this::defaultVisitor);
        addVisit("Compare", this::defaultVisitor);
        addVisit("LogicalAnd", this::defaultVisitor);
        addVisit("SquareBrackets", this::defaultVisitor);
        addVisit("Length", this::defaultVisitor);
        addVisit("FunctionCall", this::defaultVisitor);
        addVisit("NewArray", this::defaultVisitor);
        addVisit("NewClass", this::defaultVisitor);
        addVisit("Integer", this::defaultVisitor);
        addVisit("BoolLiteral", this::defaultVisitor);
        addVisit("Identifier", this::defaultVisitor);
        addVisit("This", this::dealWithThis);
        addVisit("MethodArgs", this::defaultVisitor);
    }
    private Void defaultVisitor(JmmNode jmmNode, Void _void){
        return null;
    }

    private Void dealWithThis(JmmNode node, Void _void) {
        while (!node.getKind().equals("Method") && !node.getKind().equals("MainMethod"))
            node = node.getJmmParent();
        if(node.getKind().equals("MainMethod"))
            utils.createReport(node, "Can only use this in static methods");
        return null;
    }

    private Void dealWithArray(JmmNode node, Void _void) {
        Type left = utils.varCheck(node, "var");
        Type right = utils.getType(node.getJmmChild(1));
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


    public List<Report> getReports(){
        return utils.getReports();
    }

    @Override
    public Void visit(JmmNode jmmNode) {
        return super.visit(jmmNode);
    }
}

