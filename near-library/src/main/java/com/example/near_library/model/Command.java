package com.example.near_library.model;

public class Command {
    private String type;

    private String data;

    private String path;

    private String destinationId;

    public Command() {
    }

    public Command(String type, String data) {
        this.type = type;
        this.data = data;
    }

    public Command(String type, String data, String path) {
        this.type = type;
        this.data = data;
        this.path = path;
    }

    public Command(String type, String data, String destinationId, String path) {
        this.type = type;
        this.data = data;
        this.path = path;
        this.destinationId = destinationId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getDestinationId() {
        return destinationId;
    }

    public void setDestinationId(String destinationId) {
        this.destinationId = destinationId;
    }
}
