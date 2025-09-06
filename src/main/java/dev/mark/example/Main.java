package dev.mark.example;

import net.fabricmc.api.ModInitializer;
import net.minecraft.SharedConstants;

public class Main implements ModInitializer {
    @Override
    public void onInitialize() {
        System.out.println("Test mod initialized");
        System.out.println("Game version: " + SharedConstants.getGameVersion().getName());
    }
}
