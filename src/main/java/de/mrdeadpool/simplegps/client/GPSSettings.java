package de.mrdeadpool.simplegps.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;



public class GPSSettings {



    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static boolean journeyMapEnabled = false;

    private static float soundVolume = 1.0f;
    private static float displaySoundVolume = 1.0f;

    private static boolean draggingSound = false;

    private static Path getSavePath() {
        return Minecraft.getInstance()
                .gameDirectory.toPath()
                .resolve("simplegps_settings.json");
    }

    public static float getSoundVolume() {
        return soundVolume;
    }

    public static float getDisplaySoundVolume() {
        return displaySoundVolume;
    }

    public static boolean isJourneyMapEnabled() {
        return journeyMapEnabled;
    }

    public static void setJourneyMapEnabled(boolean enabled) {
        journeyMapEnabled = enabled;
        save();
    }
    public static void setSoundVolume(float volume) {
        soundVolume = Math.max(0.0f, Math.min(1.0f, volume));
        save();
    }

    public static void load() {
        Path path = getSavePath();
        if (!Files.exists(path)) return;

        try {
            String json = Files.readString(path);
            JsonObject obj = GSON.fromJson(json, JsonObject.class);

            if (obj != null) {

                if (obj.has("journeyMapEnabled")) {
                    journeyMapEnabled = obj.get("journeyMapEnabled").getAsBoolean();
                }

                if (obj.has("soundVolume")) {
                    soundVolume = obj.get("soundVolume").getAsFloat();
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void save() {
        JsonObject obj = new JsonObject();
        obj.addProperty("journeyMapEnabled", journeyMapEnabled);
        obj.addProperty("soundVolume", soundVolume);

        try {
            Files.writeString(getSavePath(), GSON.toJson(obj));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    public static void tick() {
        float speed = 0.15f;
        displaySoundVolume += (soundVolume - displaySoundVolume) * speed;
    }
}