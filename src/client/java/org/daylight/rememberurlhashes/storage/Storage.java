package org.daylight.rememberurlhashes.storage;

import com.google.common.hash.HashCode;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * A class with fields to store everything in
 */
public class Storage {
    /**
     * The map for storing pairs of "URL -> Hashcode"
     */
    private static Map<URI, HashCode> uriToHash = new HashMap<>();

    private static URI lastUriUpdated = null;

    private static boolean dataSaveEnabled = false;

    public static Map<URI, HashCode> getUriToHash() {
        return uriToHash;
    }

    public static URI getLastUriUpdated() {
        return lastUriUpdated;
    }

    public static void setUriToHash(Map<URI, HashCode> uriToHash) {
        Storage.uriToHash = uriToHash;
    }

    public static void setLastUriUpdated(URI lastUriUpdated) {
        Storage.lastUriUpdated = lastUriUpdated;
    }

    public static boolean isDataSaveEnabled() {
        return dataSaveEnabled;
    }

    public static void setDataSaveEnabled(boolean dataSaveEnabled) {
        Storage.dataSaveEnabled = dataSaveEnabled;
    }

    public static void storeHash(URI uri, HashCode hash) {
        uriToHash.put(uri, hash);
    }

    public static HashCode getHash(URI uri) {
        return uriToHash.get(uri);
    }

    public static void resetAllHashes() {
        uriToHash.clear();
    }

    public static void resetLastHash() {
        if(lastUriUpdated != null) uriToHash.remove(lastUriUpdated);
    }
}
