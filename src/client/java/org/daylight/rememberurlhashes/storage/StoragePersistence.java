package org.daylight.rememberurlhashes.storage;

import com.google.common.hash.HashCode;
import com.google.gson.*;
import com.google.gson.annotations.SerializedName;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.URI;
import java.util.Map;

public class StoragePersistence {
    private static final File saveFile = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("remember_url_hashes.json")
            .toFile();


    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(URI.class, (JsonSerializer<URI>) (src, typeOfSrc, context) -> new JsonPrimitive(src.toString()))
            .registerTypeAdapter(URI.class, (JsonDeserializer<URI>) (json, typeOfT, context) -> URI.create(json.getAsString()))
            .registerTypeAdapter(HashCode.class, (JsonSerializer<HashCode>) (src, typeOfSrc, context) -> new JsonPrimitive(src.toString()))
            .registerTypeAdapter(HashCode.class, (JsonDeserializer<HashCode>) (json, typeOfT, context) -> HashCode.fromString(json.getAsString()))
            .create();

    public static void saveData() {
        try (FileWriter writer = new FileWriter(saveFile)) {
            GSON.toJson(new StorageData(
                    Storage.getUriToHash(),
                    Storage.getLastUriUpdated(),
                    Storage.isDataSaveEnabled()
            ), writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void loadData() {
        if (!saveFile.exists()) return;
        try (FileReader reader = new FileReader(saveFile)) {
            StorageData data = GSON.fromJson(reader, StorageData.class);
            if (data != null) {
                Storage.setDataSaveEnabled(data.dataSaveEnabled);
                if (Storage.isDataSaveEnabled()) {
                    Storage.setUriToHash(data.uriToHashMap);
                    Storage.setLastUriUpdated(data.lastUriUpdated);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Save file structure
     */
    private static class StorageData {
        @SerializedName("uriToHashMap")
        Map<URI, HashCode> uriToHashMap;

        @SerializedName("lastUriUpdated")
        URI lastUriUpdated;

        @SerializedName("dataSaveEnabled")
        boolean dataSaveEnabled;

        StorageData(Map<URI, HashCode> uriToHashMap, URI lastUriUpdated, boolean dataSaveEnabled) {
            this.uriToHashMap = uriToHashMap;
            this.lastUriUpdated = lastUriUpdated;
            this.dataSaveEnabled = dataSaveEnabled;
        }
    }
}
