package com.yuncan.pathfinder.clas;

public class Requests {
    String request_type;

    public Requests(String request_type) {
        this.request_type = request_type;
    }

    public Requests() {
    }

    public String getRequest_type() {
        return request_type;
    }

    public void setRequest_type(String request_type) {
        this.request_type = request_type;
    }
}
