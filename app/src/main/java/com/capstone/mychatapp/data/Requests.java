package com.capstone.mychatapp.data;

public class Requests {
    String requestType;

    public Requests() {

    }

    public Requests(String requestType) {
        this.requestType = requestType;
    }

    public String getReq_type() {
        return requestType;
    }

    public void setReq_type(String requestType) {
        this.requestType = requestType;
    }
}