package com.example.near_library.routing;

import com.example.near_library.Endpoint;

import java.util.*;

public class DSDV {

    private Graph graph;

    private HashMap<Endpoint, Integer> distances;

    private HashMap<Endpoint, Endpoint> predecessors;

    private Endpoint source;

    private static Integer INFINTE = Integer.MAX_VALUE;

    public DSDV(Endpoint source) {
        graph = new Graph();

        distances = new HashMap<>();

        predecessors = new HashMap<>();

        this.source = source;
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

                if (!distances.get(source).equals(INFINTE) &&
                        distances.get(source) + weight < distances.get(destination)) {
                    distances.put(destination, distances.get(source) + weight);
                    predecessors.put(destination, source);
                }
            }
        }
    }

    private void initialiseDistancesAndPredecessors() {
        Set<Endpoint> vertices = graph.getVertices();

        distances.clear();
        predecessors.clear();

        for (Endpoint e: vertices) {
            distances.put(e, INFINTE);
            predecessors.put(e, null);
        }
        distances.put(source, 0);
        predecessors.put(source, null);
    }

    public List<Endpoint> getPathTo(Endpoint endpoint) {
        List<Endpoint> path = new ArrayList<>();
        Endpoint current = endpoint;
        while (current != null) {
            path.add(current);
            current = predecessors.get(current);
        }
        Collections.reverse(path);
        return path;
    }

    public void addEdge(Endpoint source, Endpoint destination, Integer weight) {
        graph.addEdge(source, destination, weight);
    }

    public void addUnorientedEdge(Endpoint source, Endpoint destination, Integer weight) {
        graph.addUnorientedEdge(source, destination, weight);
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
}
