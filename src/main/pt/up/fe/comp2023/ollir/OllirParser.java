package pt.up.fe.comp2023.ollir;

import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.Method;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ollir.JmmOptimization;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2023.Optimizer;
import pt.up.fe.comp2023.optimization.registerAllocation.InterferenceGraph;
import pt.up.fe.comp2023.optimization.registerAllocation.LivenessAnalysis;

public class OllirParser implements JmmOptimization {
    @Override
    public OllirResult toOllir(JmmSemanticsResult jmmSemanticsResult) {
        // AST optimization
        optimize(jmmSemanticsResult);
        
        // Ollir parse
        OllirVisitor visitor = new OllirVisitor(jmmSemanticsResult.getSymbolTable());
        visitor.visit(jmmSemanticsResult.getRootNode(), null);

        System.out.println(visitor.getOllirCode());
        OllirResult ollirResult = new OllirResult(jmmSemanticsResult, visitor.getOllirCode(), jmmSemanticsResult.getReports());

        // register allocation
        return optimize(ollirResult);
    }

    @Override
    public JmmSemanticsResult optimize(JmmSemanticsResult jmmSemanticsResult){
        Optimizer optimizer = new Optimizer();
        return optimizer.optimize(jmmSemanticsResult);
    }


    @Override
    public OllirResult optimize(OllirResult ollirResult){
        ClassUnit ollirClass = ollirResult.getOllirClass();
        ollirClass.buildCFGs();
        ollirClass.buildVarTables();
        if (! ollirResult.getConfig().getOrDefault("registerAllocation", "-1").equals("-1")) {
            for (Method method : ollirClass.getMethods()) {
                // in-out algorithm
                LivenessAnalysis livenessAnalysis = new LivenessAnalysis(method);
                livenessAnalysis.execute();
                // Interference graph
                InterferenceGraph interferenceGraph = new InterferenceGraph(livenessAnalysis.getSets(), method);
                interferenceGraph.make();
                // Graph coloring
                int reg = Integer.parseInt(ollirResult.getConfig().get("registerAllocation"));
                int k = 0;
                while (!interferenceGraph.paint(k)) {
                    k++;
                }
                interferenceGraph.allocate();
                if ( k > reg && reg != 0) {
                    ollirResult.getReports().add(new Report(ReportType.ERROR, Stage.OPTIMIZATION, -1, -1, "Could not allocate method " + method.getMethodName() + " with " + reg + " registers!\n" + k + " registers required!"));
                }
            }
        }
        return ollirResult;
    }
}
