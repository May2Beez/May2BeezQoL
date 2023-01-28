package com.May2Beez.utils.structs;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

public class OldWaypoint {

    public static class Root {
        @Expose
        @Getter
        @Setter
        JsonObject routes;
        @Expose
        @Getter
        @Setter
        String selectedRoute;
    }
}
