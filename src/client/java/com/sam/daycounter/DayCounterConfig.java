package com.sam.daycounter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class DayCounterConfig {

    public boolean enabled = true;

    // LABEL MODE: AUTO, CUSTOM, OFF
    public String labelMode = "AUTO";

    // Used only when labelMode == CUSTOM
    public String customLabel = "";

    public boolean showBackground = true;
    public float scale = 1.0f;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static File file;

    public static DayCounterConfig load() {
        try {
            file = new File("config/daycounter.json");
            if (!file.exists()) {
                DayCounterConfig cfg = new DayCounterConfig();
                save(cfg);
                return cfg;
            }
            return GSON.fromJson(new FileReader(file), DayCounterConfig.class);
        } catch (Exception e) {
            return new DayCounterConfig();
        }
    }

    public static void save(DayCounterConfig cfg) {
        try (FileWriter writer = new FileWriter(file)) {
            GSON.toJson(cfg, writer);
        } catch (Exception ignored) {}
    }
}
