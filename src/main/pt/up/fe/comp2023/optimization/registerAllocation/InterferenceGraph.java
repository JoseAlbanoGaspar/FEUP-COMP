package pt.up.fe.comp2023.optimization.registerAllocation;

import org.specs.comp.ollir.Descriptor;
import org.specs.comp.ollir.Instruction;
import org.specs.comp.ollir.Method;
import org.specs.comp.ollir.VarScope;

import java.util.*;

public class InterferenceGraph {
    protected HashMap<Instruction, LivenessSets> sets;
    private final Graph graph;
    private final Method method;

    public InterferenceGraph(HashMap<Instruction, LivenessSets> sets, Method method) {
        this.sets = sets;
        this.graph = new Graph(method);
        this.method = method;
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
    private static HashMap<IntGraphNode, List<IntGraphNode>> deepCopyHashMap(HashMap<IntGraphNode, List<IntGraphNode>> original) {
        HashMap<IntGraphNode, List<IntGraphNode>> copy = new HashMap<>();
        for (Map.Entry<IntGraphNode, List<IntGraphNode>> entry : original.entrySet()) {
            IntGraphNode key = new IntGraphNode(entry.getKey());
            List<IntGraphNode> value = new ArrayList<>(entry.getValue().size());
            for (IntGraphNode node : entry.getValue()) {
                value.add(new IntGraphNode(node));
            }
            copy.put(key, value);
        }
        return copy;
    }

    public boolean paint(int k) {
        Graph aux_graph = new Graph(deepCopyHashMap(graph.getAdjacencyList()), method);
        Stack<IntGraphNode> stack = new Stack<>();
        while (!aux_graph.getAdjacencyList().isEmpty()) {
            boolean nodeFound = false;
            for (Map.Entry<IntGraphNode, List<IntGraphNode>> entry : aux_graph.getAdjacencyList().entrySet()) {
                IntGraphNode node = entry.getKey();
                List<IntGraphNode> neighbors = entry.getValue();

                if (neighbors.size() < k) {
                    stack.push(node);
                    aux_graph.removeVertex(node);
                    nodeFound = true;
                    break;
                }
            }
            if (!nodeFound) {
                System.out.println("Could not allocate " + k + " registers!");
                return false;
            }
        }

        while (!stack.isEmpty()) {
            IntGraphNode node = stack.pop();
            graph.setRegister(node.getName());
        }

        graph.printRegisterAllocation();
        System.out.println("Allocated with " + k + " registers!");
        return true;
    }

    public void allocate() {
        final HashMap<String, Descriptor> varTable = this.method.getVarTable();
        for (IntGraphNode node : graph.getNodes()) {
            Descriptor descriptor = varTable.get(node.getName());
            if (descriptor.getScope().equals(VarScope.LOCAL)) {
                descriptor.setVirtualReg(node.getColor());
            }
        }
    }
}
