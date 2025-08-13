package org.daylight.rememberurlhashes;

import net.fabricmc.api.ClientModInitializer;
import org.daylight.rememberurlhashes.commands.ModCommandsManager;
import org.daylight.rememberurlhashes.storage.StoragePersistence;

/**
 * Quick overview:
 * The main functionality is located in {@link org.daylight.rememberurlhashes.mixin.client.NetworkUtilsMixin}
 * Commands are in {@link ModCommandsManager}
 * Data is stored in {@link org.daylight.rememberurlhashes.storage.Storage}
 */
public class RememberUrlHashesClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ModCommandsManager.initializeCommands();
		StoragePersistence.loadData();
	}
}
