package com.example.demo.protocol;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class JSONProtocol implements IProtocol {

    private final Gson gson = new Gson();

    @Override
    public Map<String, String> fromString(String request) {
        return gson.fromJson(request, HashMap.class);
    }

    @Override
    public String toString(Map<String, String> response) {
        return gson.toJson(response);
    }
}
