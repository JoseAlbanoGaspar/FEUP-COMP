package pt.up.fe.comp2023;

import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.analysis.JmmAnalysis;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.jmm.report.Report;

import java.util.List;

public class Analysis implements JmmAnalysis {
    @Override
    public JmmSemanticsResult semanticAnalysis(JmmParserResult jmmParserResult) {
        // Check if there are parsing errors
        List<Report> reports = jmmParserResult.getReports();

        for (Report report : jmmParserResult.getReports()) {
            System.out.println(report.getMessage());
        }
        TestUtils.noErrors(jmmParserResult.getReports());

        // Generate Symbolic Table
        SymbolTableVisitor visitor = new SymbolTableVisitor();
        visitor.visit(jmmParserResult.getRootNode(), null);
        SimpleTable table = visitor.generateSymbolicTable();
        System.out.println(table);

        // Semantic Analysis
        /*
                1 - create a new visitor with the symbolic table
                2 - the visitor should do the analysis
                3 - it should update the reports
         */
        SemanticAnalyserVisitor semanticVisitor = new SemanticAnalyserVisitor(table);
        semanticVisitor.visit(jmmParserResult.getRootNode(),null);


        for (Report report : semanticVisitor.getReports()) {
            System.out.println(report.getMessage());
        }
        TestUtils.noErrors(semanticVisitor.getReports());

        return new JmmSemanticsResult(jmmParserResult, table, reports);
    }
}
