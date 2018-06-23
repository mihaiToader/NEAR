package com.example.near_library.routing;

import com.example.near_library.model.Endpoint;

import java.util.*;

public class DSDV {

    private Graph graph;

    private HashMap<Endpoint, Integer> distances;

    private HashMap<Endpoint, Endpoint> predecessors;

    private Endpoint source;

    private boolean orientedGraph = false;

    private static Integer INFINTE = Integer.MAX_VALUE;

    public DSDV(Boolean orientedGraph, Endpoint source) {
        graph = new Graph();

        this.orientedGraph = orientedGraph;

        distances = new HashMap<>();

        predecessors = new HashMap<>();

        this.source = source;
    }

    public DSDV(boolean orientedGraph) {
        this(orientedGraph, null);
    }

    public void bellmanFord() {
        int V = graph.getNrVertices(), E = graph.getNrEdges();


        // Step 1: Initialize distances from src to all other
        // vertices as INFINITE
        initialiseDistancesAndPredecessors();


        // Step 2: Relax all edges |V| - 1 times. A simple
        // shortest path from src to any other vertex can
        // have at-most |V| - 1 edges
        List<Edge> edges = graph.getEdges();
        for (int i = 1; i < V; ++i) {
            for (int j = 0; j < E; ++j) {
                Edge edge = edges.get(j);
                Endpoint source = edge.getSource();
                Endpoint destination = edge.getDestination();
                Integer weight = edge.getWeight();

                calculateDistance(source, destination, weight);
                if (!orientedGraph) {
                    calculateDistance(destination, source, weight);
                }
            }
        }
    }

    private void calculateDistance(Endpoint source, Endpoint destination, Integer weight) {
        if (!distances.get(source).equals(INFINTE) &&
                distances.get(source) + weight < distances.get(destination)) {
            distances.put(destination, distances.get(source) + weight);
            predecessors.put(destination, source);
        }
    }

    private void initialiseDistancesAndPredecessors() {
        Set<Endpoint> vertices = graph.getVertices().keySet();

        distances.clear();
        predecessors.clear();

        for (Endpoint e : vertices) {
            distances.put(e, INFINTE);
            predecessors.put(e, null);
        }
        distances.put(source, 0);
        predecessors.put(source, null);
    }

    public List<Endpoint> getPathTo(String endpointId) {
        return getPathTo(graph.findVertex(endpointId));
    }

    public List<Endpoint> getPathTo(Endpoint endpoint) {
        if (endpoint != null) {
            List<Endpoint> path = new ArrayList<>();
            Endpoint current = endpoint;
            while (current != null) {
                path.add(current);
                current = predecessors.get(current);
            }
            Collections.reverse(path);
            path.remove(0);
            return path;
        }
        return null;
    }

    public void addEdge(Endpoint source, Endpoint destination, Integer weight) {
        graph.addEdge(source, destination, weight);
        bellmanFord();
    }

    public void addEdge(Edge edge) {
        this.addEdge(edge.getSource(), edge.getDestination());
    }

    public void addEdgeFromSource(Endpoint destination, Integer weight) {
        this.addEdge(source, destination, weight);
    }

    public void addEdge(Endpoint source, Endpoint destination) {
        this.addEdge(source, destination, 1);
    }

    public void addEdgeFromSource(Endpoint destination) {
        this.addEdge(source, destination, 1);
    }

    public Graph getGraph() {
        return graph;
    }

    public HashMap<Endpoint, Integer> getDistances() {
        return distances;
    }

    public HashMap<Endpoint, Endpoint> getPredecessors() {
        return predecessors;
    }

    public Endpoint getSource() {
        return source;
    }

    public void setSource(Endpoint endpoint) {
        this.source = endpoint;
    }

    public String encodePathTo(Endpoint destination) {
        return encodePath(getPathTo(destination));
    }

    public String encodePath(List<Endpoint> path) {
        StringBuilder encoding = new StringBuilder();
        for (Endpoint endpoint : path) {
            encoding.append(endpoint.encode()).append("@");
        }
        if (encoding.toString().isEmpty()) {
            return encoding.toString();
        }
        return encoding.toString().substring(0, encoding.length() - 1);
    }

    public List<Endpoint> decodePath(String encodingPath) {
        List<Endpoint> path = new ArrayList<>();
        for (String encodingEndpoint : encodingPath.split("@")) {
            path.add(Endpoint.decode(encodingEndpoint));
        }
        return path;
    }

    public void removeEdge(Endpoint source, Endpoint destination) {
        graph.removeEdge(source, destination);

        if (graph.getVertices().get(this.source) == null) {
            graph.clear();
            this.source = null;
        }
        bellmanFord();
    }

}
