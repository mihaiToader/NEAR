package com.example.near_library.routing;

import com.example.near_library.model.Endpoint;

import java.util.Objects;

public class Edge {
    private Endpoint source;

    private Endpoint destination;

    private Integer weight = 1;

    public Edge() {
    }

    public Edge(Endpoint source, Endpoint destination, Integer weight) {
        this.source = source;
        this.destination = destination;
        this.weight = weight;
    }

    public Edge(Endpoint source, Endpoint destination) {
        this.source = source;
        this.destination = destination;
    }

    public Endpoint getSource() {
        return source;
    }

    public void setSource(Endpoint source) {
        this.source = source;
    }

    public Endpoint getDestination() {
        return destination;
    }

    public void setDestination(Endpoint destination) {
        this.destination = destination;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Edge edge = (Edge) o;
        return ((edge.getDestination().getId().equals(this.getDestination().getId())  || edge.getDestination().getId().equals(this.getSource().getId()))
                && ((edge.getSource().getId().equals(this.getDestination().getId())  || edge.getSource().getId().equals(this.getSource().getId()))));
    }

    public String encode() {
        return String.format("%s,%s", source.encode(), destination.encode());
    }

    public static Edge decodeEdge(String encodingString) {
        String[] components = encodingString.split(",");
        return new Edge(Endpoint.decode(components[0]), Endpoint.decode(components[1]));
    }
}
