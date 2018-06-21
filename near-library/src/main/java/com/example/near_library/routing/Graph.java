package com.example.near_library.routing;

import com.example.near_library.Endpoint;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Graph {
    private List<Edge> edges;
    private Set<Endpoint> vertices;

    Graph() {
        edges = new ArrayList<>();
        vertices = new HashSet<>();
    }

    public void addEdge(Endpoint source, Endpoint destination, Integer weight) {
        vertices.add(source);
        vertices.add(destination);
        edges.add(new Edge(source, destination, weight));
    }

    public void addUnorientedEdge(Endpoint source, Endpoint destination, Integer weight) {
        addEdge(source, destination, weight);
        addEdge(destination, source, weight);
    }

    public Integer getNrVertices() {
        return vertices.size();
    }

    public Integer getNrEdges() {
        return edges.size();
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public void setEdges(List<Edge> edges) {
        this.edges = edges;
    }

    public Set<Endpoint> getVertices() {
        return vertices;
    }
}
