package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.analysis.JmmAnalysis;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp2023.semantics.*;

import java.util.ArrayList;
import java.util.List;

public class Analysis implements JmmAnalysis {
    @Override
    public JmmSemanticsResult semanticAnalysis(JmmParserResult jmmParserResult) {
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
        List<Reporter> visitors = new ArrayList<>();
        visitors.add(new SemanticAnalyserVisitor(table));
        visitors.add(new OperandsTypeVisitor(table));
        visitors.add(new SemanticArrayVisitor(table));
        visitors.add(new AssignmentAndThisVisitor(table));
        visitors.add(new IncompatibleRetArgsVisitor(table));
        for(Reporter semVisitor : visitors){
            semVisitor.visit(jmmParserResult.getRootNode());
            for (Report report : semVisitor.getReports()) {
                System.out.println(report.getMessage());
            }
            if(!semVisitor.getReports().isEmpty()) {
                // check if reports are from type ERROR
                for (Report report : semVisitor.getReports())
                    if(report.getType().equals(ReportType.ERROR))
                        return new JmmSemanticsResult(jmmParserResult, table, semVisitor.getReports());
            }
        }

        //

        return new JmmSemanticsResult(jmmParserResult, table,new ArrayList<>());
    }
}
