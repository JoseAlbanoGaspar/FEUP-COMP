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
        System.out.println("Rets:");
        System.out.println(table.getReturnType("getField1"));
        System.out.println(table.getReturnType("getField2"));
        System.out.println(table.getReturnType("getField3"));
        System.out.println(table.getReturnType("all"));
        /*System.out.println(table.getClassName());
        System.out.println(table.getSuper());
        System.out.println(table.getImports());
        System.out.println(table.getFields());
        System.out.println(table.getParameters("main"));*/
        
        return new JmmSemanticsResult(jmmParserResult, table, reports);
    }
}
