package com.homework.streamcombiner.producer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.homework.streamcombiner.busobj.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JsonProducer {
    private static final Gson GSON = new GsonBuilder().create();

    /**
     * Parse given data to json and produce it
     *
     * @param data - data to produce
     */
    public void produce(@NonNull Data data) {
        final JsonElement jsonElement = GSON.toJsonTree(data);
        JsonObject root = new JsonObject();
        root.add(Data.class.getSimpleName().toLowerCase(), jsonElement);
        final String msg = root.toString();
        log.info(msg);
    }
}
