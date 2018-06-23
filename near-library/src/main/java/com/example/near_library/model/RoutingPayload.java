package com.example.near_library.model;

public enum RoutingPayload {

    /**
     * For the initial connection, we need to set the source, for the new device
     * If a device doesn't has the source set, he is new in the network
     */
    INITIAL_CONNECTION,

    /**
     * Receive the whole graph when the device is new in the network
     */
    RECEIVE_GRAPH,

    /**
     * For update the graph, when a connection is lost or added
     */
    ADD_EDGE,
    REMOVE_EDGE,
}
