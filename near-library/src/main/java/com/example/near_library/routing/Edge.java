package com.example.near_library.routing;

import com.example.near_library.Endpoint;

public class Edge {
    private Endpoint source;

    private Endpoint destination;

    private Integer weight;

    public Edge() {
        weight = 1;
    }

    public Edge(Endpoint source, Endpoint destination, Integer weight) {
        this.source = source;
        this.destination = destination;
        this.weight = weight;
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
}
