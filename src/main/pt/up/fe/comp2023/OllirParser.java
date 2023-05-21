package pt.up.fe.comp2023;

import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.Instruction;
import org.specs.comp.ollir.Method;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ollir.JmmOptimization;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp2023.optimization.registerAllocation.InterferenceGraph;
import pt.up.fe.comp2023.optimization.registerAllocation.LivenessAnalysis;
import pt.up.fe.comp2023.optimization.registerAllocation.LivenessSets;

import java.util.HashMap;

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
        
        /*if (jmmSemanticsResult.getConfig().getOrDefault("registerAllocation", "-1").equals("-1")) {*/
        ClassUnit ollirClass = ollirResult.getOllirClass();
        ollirClass.buildCFGs();
        
        int k = Integer.parseInt(jmmSemanticsResult.getConfig().get("registerAllocation"));
        
        //...

        for (Method method : ollirClass.getMethods()) {
            // in-out algorithm
            LivenessAnalysis livenessAnalysis = new LivenessAnalysis(method);
            livenessAnalysis.execute();
            // Interference graph
            InterferenceGraph interferenceGraph = new InterferenceGraph(livenessAnalysis.getSets());
            interferenceGraph.make();
            interferenceGraph.paint(k);
            
            // Graph coloring
        
        }



        return ollirResult;
    }
}
