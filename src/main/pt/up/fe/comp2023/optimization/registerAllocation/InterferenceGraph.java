package pt.up.fe.comp2023.optimization.registerAllocation;

import org.specs.comp.ollir.Instruction;

import java.util.*;

public class InterferenceGraph {
    protected HashMap<Instruction, LivenessSets> sets;
    private Graph graph;

    public InterferenceGraph(HashMap<Instruction, LivenessSets> sets) {
        this.sets = sets;
        this.graph = new Graph();
    }

    public void make() {
        for (Map.Entry<Instruction, LivenessSets> entry : sets.entrySet()) {
            LivenessSets livenessSet = entry.getValue();
            Set<String> out = livenessSet.getOut();
            out.addAll(livenessSet.getDef());
            graph.addVertexes(new ArrayList<>(livenessSet.getUse()));
            graph.addVertexes(new ArrayList<>(livenessSet.getDef()));
            graph.connectAllNodes(livenessSet.getIn());
            graph.connectAllNodes(out);
        }
        graph.printGraph();
    }

    public void paint(int k) {
        k = 5; // only for testing

    }
}
