package com.example.near_library.routing;

import com.example.near_library.model.Endpoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Graph {
    private List<Edge> edges;
    private HashMap<Endpoint, Integer> vertices;

    Graph() {
        edges = new ArrayList<>();
        vertices = new HashMap<>();
    }

    public void addEdge(Endpoint source, Endpoint destination, Integer weight) {
        Edge edge = new Edge(source, destination, weight);
        if (!edges.contains(edge)) {
            addVertex(source);
            addVertex(destination);
            edges.add(edge);
        }
    }

    public void addVertex(Endpoint vertex) {
        if (vertices.get(vertex) == null) {
            vertices.put(vertex, 1);
        } else {
            vertices.put(vertex, vertices.get(vertex) + 1);
        }
    }

    public void removeEdge(Endpoint source, Endpoint destination) {
        edges.remove(new Edge(source, destination));
        removeVertex(source);
        removeVertex(destination);
    }

    public void removeVertex(Endpoint vertex) {
        if (vertices.get(vertex) != null) {
            if (vertices.get(vertex) == 1) {
                vertices.remove(vertex);
            } else {
                vertices.put(vertex, vertices.get(vertex) + 1);
            }
        }
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

    public HashMap<Endpoint, Integer> getVertices() {
        return vertices;
    }

    public String encodeEdges() {
        StringBuilder encoding = new StringBuilder();
        for (Edge edge : getEdges()) {
            encoding.append(edge.encode());
            encoding.append("@");
        }
        if (encoding.toString().isEmpty()) {
            return encoding.toString();
        }
        return encoding.toString().substring(0, encoding.length() - 1);

    }

    public List<Edge> decodeEdges(String encodingString) {
        List<Edge> res = new ArrayList<>();
        if (!encodingString.equals("")) {
            for (String encodingEdge : encodingString.split("@")) {
                res.add(Edge.decodeEdge(encodingEdge));
            }
        }
        return res;
    }

    public void clear() {
        vertices.clear();
        edges.clear();
    }


    public Endpoint findVertex(String vertexId) {
        for (Endpoint endpoint : vertices.keySet()) {
            if (endpoint.getId().equals(vertexId))
                return endpoint;
        }
        return null;
    }
}
