package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ollir.JmmOptimization;
import pt.up.fe.comp.jmm.ollir.OllirResult;

public class OllirParser implements JmmOptimization {
    @Override
    public OllirResult toOllir(JmmSemanticsResult jmmSemanticsResult) {
        //semanticResult -> jmmParserResult(AST), SimbolTable, Reports
        //getRootNode(), getSymbolTable(), getReports()

        OllirVisitor visitor = new OllirVisitor(jmmSemanticsResult.getSymbolTable());
        visitor.visit(jmmSemanticsResult.getRootNode(), null);

        return new OllirResult(jmmSemanticsResult, visitor.getOllirCode(), jmmSemanticsResult.getReports());
    }
}
