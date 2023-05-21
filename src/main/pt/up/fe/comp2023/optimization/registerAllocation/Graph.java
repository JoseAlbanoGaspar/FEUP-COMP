package pt.up.fe.comp2023.optimization.registerAllocation;

import java.util.*;

class IntGraphNode {
    String name;
    int color;
    public IntGraphNode(String name) {
        this.color = -1;
        this.name = name;
    }
    public String getName() {
        return name;
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
    private HashMap<IntGraphNode, List<IntGraphNode>> adjacencyList;
    public Graph() {
        adjacencyList = new HashMap<>();
    }
    private IntGraphNode makeNode(String name) {
        return new IntGraphNode(name);
    }
    public void addVertex(IntGraphNode vertex) {
        adjacencyList.put(vertex, new ArrayList<>());
    }

    public void addVertexes(List<String> vertexes) {
        for (String vertex : vertexes) {
            if(!vertexExists(vertex)) {
                adjacencyList.put(makeNode(vertex), new ArrayList<>());
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
        int size = nodeList.size();

        for (int i = 0; i < size - 1; i++) {
            IntGraphNode source = makeNode(nodeList.get(i));
            if (!vertexExists(source.getName())) addVertex(source);
            for (int j = i + 1; j < size; j++) {
                IntGraphNode destination = makeNode(nodeList.get(j));
                if (!vertexExists(destination.getName())) addVertex(destination);
                if (!hasConnection(source.getName(), destination.getName())) {
                    addEdge(source.getName(), destination.getName());
                }
            }
        }
    }

    public void removeVertex(String vertex) {
        // Remove the vertex from the adjacency list
        adjacencyList.remove(makeNode(vertex));

        // Remove any edges containing the removed vertex
        for (List<IntGraphNode> neighbors : adjacencyList.values()) {
            neighbors.removeIf(n -> n.equals(makeNode(vertex)));
        }
    }

    public List<String> getNeighbors(String vertex) {
        IntGraphNode node = makeNode(vertex);
        List<String> neighbors = new ArrayList<>();
        for (IntGraphNode nd : adjacencyList.get(node)) {
            neighbors.add(nd.getName());
        }
        return neighbors;
    }

    public boolean vertexExists(String vertex) {
        for (Map.Entry<IntGraphNode, List<IntGraphNode>> entry : adjacencyList.entrySet()) {
            if (entry.getKey().getName().equals(vertex))
                return true;
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
}
