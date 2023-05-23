package pt.up.fe.comp2023;

import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.Method;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ollir.JmmOptimization;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp2023.optimization.registerAllocation.InterferenceGraph;
import pt.up.fe.comp2023.optimization.registerAllocation.LivenessAnalysis;

public class OllirParser implements JmmOptimization {
    @Override
    public OllirResult toOllir(JmmSemanticsResult jmmSemanticsResult) {

        // Optimization stage
        //Optimizer optimizer = new Optimizer();
        //jmmSemanticsResult = optimizer.optimize(jmmSemanticsResult);
        
        // Ollir parse
        OllirVisitor visitor = new OllirVisitor(jmmSemanticsResult.getSymbolTable());
        visitor.visit(jmmSemanticsResult.getRootNode(), null);
        
        
        OllirResult ollirResult = new OllirResult(jmmSemanticsResult, visitor.getOllirCode(), jmmSemanticsResult.getReports());

        // register allocation
        return optimize(ollirResult);
    }

    @Override
    public OllirResult optimize(OllirResult ollirResult){
        if (! ollirResult.getConfig().getOrDefault("registerAllocation", "-1").equals("-1")) {
            ClassUnit ollirClass = ollirResult.getOllirClass();
            ollirClass.buildCFGs();

            for (Method method : ollirClass.getMethods()) {
                // in-out algorithm
                LivenessAnalysis livenessAnalysis = new LivenessAnalysis(method, ollirResult.getSymbolTable());
                livenessAnalysis.execute();
                // Interference graph
                InterferenceGraph interferenceGraph = new InterferenceGraph(livenessAnalysis.getSets(), method);
                interferenceGraph.make();
                // Graph coloring
                int k = Integer.parseInt(ollirResult.getConfig().get("registerAllocation"));
                if (k == 0) { // using the fewest registers possible
                    while (!interferenceGraph.paint(k)) {
                        k++;
                    }
                    interferenceGraph.allocate();
                } // using at most k registers
                else if (interferenceGraph.paint(k)) interferenceGraph.allocate();
            }
        }
        return ollirResult;
    }
}
