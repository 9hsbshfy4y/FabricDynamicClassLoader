package dev.mark.loader.entrypoints;

import net.fabricmc.api.ClientModInitializer;
import dev.mark.loader.service.ConnectionService;

public class Client implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ConnectionService.loadClasses();
    }
}