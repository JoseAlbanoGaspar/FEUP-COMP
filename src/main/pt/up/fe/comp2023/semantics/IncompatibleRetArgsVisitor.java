package pt.up.fe.comp2023.semantics;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp2023.SimpleTable;

import java.util.List;

public class IncompatibleRetArgsVisitor extends PreorderJmmVisitor<Void, Void> implements Reporter {
    protected SimpleTable simpleTable;
    protected SemanticUtils utils;

    public IncompatibleRetArgsVisitor(SimpleTable simpleTable){
        this.simpleTable = simpleTable;
        this.utils = new SemanticUtils(simpleTable);
    }

    @Override
    protected void buildVisitor() {
        setDefaultVisit(this::defaultVisitor);
        addVisit("Method", this::dealWithMethod);
        addVisit("FunctionCall", this::dealWithFunctionCall);
    }

    private Void defaultVisitor(JmmNode jmmNode, Void _void){
        return null;
    }

    private Void dealWithFunctionCall(JmmNode node, Void _void) {
        //se o metodo existir -> compara
        if(simpleTable.getMethods().contains(node.get("methodName"))){

            List<Symbol> actualTypes = simpleTable.getParameters(node.get("methodName"));
            List<JmmNode> insertedTypes = node.getChildren();
            insertedTypes.remove(0); // don't need 1st element -> it's the callee, not argument
            if(actualTypes.size() != insertedTypes.size()) {
                utils.createReport(node, "Incompatible argument types!");
                return null;
            }
            for (int i = 0;  i < actualTypes.size(); i++){
                if(!utils.getType(insertedTypes.get(i)).equals(actualTypes.get(i).getType())){
                    utils.createReport(node, "Incompatible argument types!");
                    return null;
                }
            }
        }
        //se o método extendido / importado -> ignora -> já visto pelos outros visitors???
        return null;
    }

    private Void dealWithMethod(JmmNode node, Void _void) {
        Type retType = utils.getType(node.getJmmChild(node.getNumChildren() - 1));
        if(!simpleTable.getReturnType(node.get("name")).equals(retType)){
            utils.createReport(node, "Return type not compatible!");
        }
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

