package org.daylight.rememberurlhashes.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.daylight.rememberurlhashes.storage.Storage;
import org.daylight.rememberurlhashes.storage.StoragePersistence;

public class ModCommandsManager {
    private static final MinecraftClient client = MinecraftClient.getInstance();

    public static void initializeCommands() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(
                    ClientCommandManager.literal("urlHashesResetAll")
                            .executes(ModCommandsManager::resetAllCommand)
            );
            dispatcher.register(
                    ClientCommandManager.literal("urlHashesResetLast")
                            .executes(ModCommandsManager::resetLastCommand)
            );
            dispatcher.register(
                    ClientCommandManager.literal("urlHashesToggleSaving")
                            .executes(ModCommandsManager::toggleSavingCommand)
            );
            dispatcher.register(
                    ClientCommandManager.literal("urlHashesState")
                            .executes(ModCommandsManager::displayDataCommand)
            );
        });
    }

    private static void sendClientMessage(MutableText text) {
        if (client.player != null) {
            client.player.sendMessage(text, false);
        }
    }

    private static int resetAllCommand(CommandContext<?> context) {
        Storage.resetAllHashes();
        sendClientMessage(Text.literal("Successfully reset all resource pack url hashes!").setStyle(Style.EMPTY.withColor(Formatting.GREEN)));
        return Command.SINGLE_SUCCESS;
    }

    private static int resetLastCommand(CommandContext<?> context) {
        Storage.resetLastHash();
        sendClientMessage(Text.literal("Successfully reset the last resource pack url hash!").setStyle(Style.EMPTY.withColor(Formatting.GREEN)));
        return Command.SINGLE_SUCCESS;
    }

    private static int toggleSavingCommand(CommandContext<?> context) {
        Storage.setDataSaveEnabled(!Storage.isDataSaveEnabled());
        StoragePersistence.saveData();
        MutableText line = Storage.isDataSaveEnabled() ?
                Text.literal("Enabled").setStyle(Style.EMPTY.withBold(true).withColor(Formatting.GREEN))
                : Text.literal("Disabled").setStyle(Style.EMPTY.withBold(true).withColor(Formatting.RED));
        line.append(Text.literal(" saving hash data").setStyle(Style.EMPTY.withBold(false).withColor(Formatting.GRAY)));

        sendClientMessage(line);

        return Command.SINGLE_SUCCESS;
    }

    private static int displayDataCommand(CommandContext<?> context) {
        if (Storage.getUriToHash().isEmpty()) {
            sendClientMessage(Text.literal("No saved URL hashes").setStyle(Style.EMPTY.withColor(Formatting.GOLD)));
        } else {
            sendClientMessage(Text.literal("Saved URL hashes:").setStyle(Style.EMPTY.withColor(Formatting.GOLD)));
            Storage.getUriToHash().forEach((uri, hash) -> {
                MutableText line = Text.literal(uri.toString())
                        .append(Text.literal(" - ").setStyle(Style.EMPTY.withColor(Formatting.GRAY)))
                        .append(Text.literal(hash.toString()).setStyle(Style.EMPTY.withColor(Formatting.GREEN)));
                sendClientMessage(line);
            });
        }

        MutableText savingStatus = Text.literal("\nData saving enabled: ").setStyle(Style.EMPTY.withColor(Formatting.GOLD))
                .append(Text.literal(String.valueOf(Storage.isDataSaveEnabled()))
                        .setStyle(Style.EMPTY.withColor(Storage.isDataSaveEnabled() ? Formatting.GREEN : Formatting.RED)));
        sendClientMessage(savingStatus);

        return Command.SINGLE_SUCCESS;
    }
}
