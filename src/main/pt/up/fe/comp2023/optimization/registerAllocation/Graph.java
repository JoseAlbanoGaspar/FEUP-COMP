package pt.up.fe.comp2023.optimization.registerAllocation;

import org.specs.comp.ollir.Method;

import java.util.*;

class IntGraphNode {
    String name;
    int color;

    public IntGraphNode(String name) {
        this.color = -1;
        this.name = name;
    }
    public IntGraphNode(IntGraphNode node) {
        this.color = node.getColor();
        this.name = node.getName();
    }
    public String getName() {
        return name;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        IntGraphNode other = (IntGraphNode) obj;
        return name.equals(other.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}

public class Graph {
    final int FIRST_REG;
    private final HashMap<IntGraphNode, List<IntGraphNode>> adjacencyList;

    public Graph(Method method) {
        adjacencyList = new HashMap<>();
        FIRST_REG = getFirstReg(method);
    }
    public Graph(HashMap<IntGraphNode, List<IntGraphNode>> adjacencyList, Method method) {
        this.adjacencyList = adjacencyList;
        FIRST_REG = getFirstReg(method);
    }

    private int getFirstReg(Method method) {
        return method.getParams().size() + 1;
    }

    private IntGraphNode makeNode(String name) {
        return new IntGraphNode(name);
    }

    public HashMap<IntGraphNode, List<IntGraphNode>> getAdjacencyList() {
        return adjacencyList;
    }

    public void addVertex(IntGraphNode vertex) {
        adjacencyList.put(vertex, new ArrayList<>());
    }

    public void addVertexes(List<String> vertexes) {
        for (String vertex : vertexes) {
            IntGraphNode node = makeNode(vertex);
            if (!vertexExists(vertex)) {
                addVertex(node);
            }
        }
    }

    public void addEdge(String source, String destination) {
        IntGraphNode src = makeNode(source);
        IntGraphNode dest = makeNode(destination);

        // Add the edge from source to destination
        adjacencyList.get(src).add(dest);
        // Add the edge from destination to source (since it's an undirected graph)
        adjacencyList.get(dest).add(src);
    }

    public void connectAllNodes(Set<String> nodes) {
        List<String> nodeList = new ArrayList<>(nodes);

        for (int i = 0; i < nodeList.size(); i++) {
            String sourceName = nodeList.get(i);
            IntGraphNode source = makeNode(sourceName);
            if (!vertexExists(sourceName)) {
                addVertex(source);
            }

            for (int j = i + 1; j < nodeList.size(); j++) {
                String destinationName = nodeList.get(j);
                IntGraphNode destination = makeNode(destinationName);
                if (!vertexExists(destinationName)) {
                    addVertex(destination);
                }

                if (!hasConnection(sourceName, destinationName)) {
                    addEdge(sourceName, destinationName);
                }
            }
        }
    }

    public void removeVertex(IntGraphNode node) {
        adjacencyList.remove(node);

        // Remove any edges containing the removed vertex
        for (List<IntGraphNode> neighbors : adjacencyList.values()) {
            neighbors.removeIf(n -> n.equals(node));
        }
    }

    public List<IntGraphNode> getNeighbors(String vertex) {
        IntGraphNode node = makeNode(vertex);
        return adjacencyList.get(node);
    }

    public boolean vertexExists(String vertex) {
        for (IntGraphNode node : adjacencyList.keySet()) {
            if (node.getName().equals(vertex)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasConnection(String source, String destination) {
        IntGraphNode src = makeNode(source);
        IntGraphNode dest = makeNode(destination);
        List<IntGraphNode> neighbors = adjacencyList.get(src);
        return neighbors != null && neighbors.contains(dest);
    }

    public void printGraph() {
        System.out.println("----Interference Graph----");
        for (Map.Entry<IntGraphNode, List<IntGraphNode>> entry : adjacencyList.entrySet()) {
            IntGraphNode vertex = entry.getKey();
            List<IntGraphNode> neighbors = entry.getValue();
            System.out.print(vertex.getName() + " -> ");
            for (IntGraphNode neighbor : neighbors) {
                System.out.print(neighbor.getName() + " ");
            }
            System.out.println();
        }
        System.out.println("--------------------------");
    }

    public void printRegisterAllocation() {
        System.out.println("----Register Allocation----");
        for (Map.Entry<IntGraphNode, List<IntGraphNode>> entry : adjacencyList.entrySet()) {
            IntGraphNode vertex = entry.getKey();
            System.out.print(vertex.getName() + " -> " + vertex.getColor());
            System.out.println();
        }
        System.out.println("Used registers: " + numOfRegisters());
        System.out.println("--------------------------");
    }
    public IntGraphNode getNode(String name) {
        for (Map.Entry<IntGraphNode, List<IntGraphNode>> entry : adjacencyList.entrySet()) {
            IntGraphNode node = entry.getKey();
            if (node.getName().equals(name)) return node;
        }
        return null;

    }
    public List<IntGraphNode> getNodes() {
        List<IntGraphNode> nodes = new ArrayList<>();
        for (Map.Entry<IntGraphNode, List<IntGraphNode>> entry : adjacencyList.entrySet()) {
            nodes.add(entry.getKey());
        }
        return nodes;
    }
    public void setRegister(String name) {
        Set<Integer> usedReg = new HashSet<>();
        for (IntGraphNode nd : getNeighbors(name)) {
            IntGraphNode node = getNode(nd.getName());
            int register = node.getColor();
            if (register != -1) {
               usedReg.add(register);
            }
        }
        int allocatedReg = -1;
        IntGraphNode node = getNode(name);
        if (usedReg.isEmpty()) {
            node.setColor(FIRST_REG);
        }
        else {
            int i = FIRST_REG;
            while (allocatedReg == -1) {
                if(!usedReg.contains(i)) {
                    allocatedReg = i;
                    node.setColor(allocatedReg);
                }
                i++;
            }
        }
    }

    public int numOfRegisters() {
        int max = 0;
        for (Map.Entry<IntGraphNode, List<IntGraphNode>> entry : adjacencyList.entrySet()) {
            IntGraphNode node = entry.getKey();
            if (node.getColor() > max) max = node.getColor();
        }
        return max - FIRST_REG + 1;
    }
}
