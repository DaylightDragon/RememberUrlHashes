package org.daylight.rememberurlhashes.mixin.client;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import net.minecraft.util.NetworkUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.daylight.rememberurlhashes.storage.Storage;
import org.daylight.rememberurlhashes.storage.StoragePersistence;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.IOException;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@Mixin(NetworkUtils.class)
public class NetworkUtilsMixin {
    /**
     * A logger for this mixin with a distinct name
     */
    @Unique
    private static final Logger LOGGER = LogManager.getLogger("NetworkUtilsMixin");

    /**
     * Remembering what hashcode was produced from the pack downloaded from this URL.
     *
     * @param url Download URL that we store as a key
     * @param hashFunction The same hash function used to create the SHA1 hash from the file and put that as a value in a map
     * @param cir CallbackInfoReturnable, used to get the original path returned by that function
     */
    @Inject(method = "download", at = @At("RETURN"))
    private static void onDownloadReturn(Path path, URL url, Map<String, String> headers, HashFunction hashFunction, @Nullable HashCode hashCode, int maxBytes, Proxy proxy, NetworkUtils.DownloadListener listener, CallbackInfoReturnable<Path> cir) {
        try {
            Path returnedPath = cir.getReturnValue();
            if (returnedPath != null && Files.exists(returnedPath)) {
                HashCode realHash = hash(returnedPath, hashFunction);
                URI uri = url.toURI();

                Storage.storeHash(uri, realHash);
                Storage.setLastUriUpdated(uri);

                if (Storage.isDataSaveEnabled()) {
                    StoragePersistence.saveData();
                }

                LOGGER.info("Saved a hash for this URL");

//                HashCode.fromString(realHash.toString());
            }
        } catch (Exception e) {
            LOGGER.error("Failed to store the produced hash for a downloaded resource pack:", e);
        }
    }

    /**
     * Replacing unknown hashcodes with known ones based on past pairs of "URL -> Hashcode"
     * To be exact, this modifies function's arguments before it runs.
     * <p>
     * If you break this annotation (ordinal= smth else) you'll skip loading the resource pack all together (with an error in the console).
     * Use this if you need such behaviour and if you don't have a better way to do this.
     *
     * @param hashCode Possibly null (unknown) original hashcode
     * @param url The url
     * @return possibly modified hashcode
     */
    @ModifyVariable(method = "download", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private static HashCode replaceNullHashCode(HashCode hashCode, Path path, URL url) {
        if (hashCode == null) {
            HashCode savedHash = null;
            try {
                savedHash = Storage.getHash(url.toURI());
            } catch (URISyntaxException e) {
                LOGGER.error("Failed to replace a resource pack's hash:", e);
            }
            if (savedHash != null) {
                LOGGER.info("Successfully replaced a null hashCode for a resource pack with a saved hash");
                return savedHash;
            }
        }
        return hashCode;
    }

    /**
     * Access to the original class's method for hashing the resourcepack file
     *
     * @return HashCode
     */
    @Shadow
    private static HashCode hash(Path path, HashFunction hashFunction) throws IOException {
        throw new UnsupportedOperationException("Shadow method");
    }
}