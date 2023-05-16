package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ollir.JmmOptimization;
import pt.up.fe.comp.jmm.ollir.OllirResult;

public class OllirParser implements JmmOptimization {
    @Override
    public OllirResult toOllir(JmmSemanticsResult jmmSemanticsResult) {

        // Optimization stage
        Optimizer optimizer = new Optimizer();
        jmmSemanticsResult = optimizer.optimize(jmmSemanticsResult);

        // Ollir parse
        OllirVisitor visitor = new OllirVisitor(jmmSemanticsResult.getSymbolTable());
        visitor.visit(jmmSemanticsResult.getRootNode(), null);


        OllirResult ollirResult = new OllirResult(jmmSemanticsResult, visitor.getOllirCode(), jmmSemanticsResult.getReports());


        return ollirResult;
    }
}
