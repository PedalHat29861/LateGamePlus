package com.pedalhat.lategameplus;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

public class LateGamePlusClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        System.out.println("[LGP] LateGamePlusClient onInitializeClient()");

        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES)
            .registerReloadListener(new SimpleSynchronousResourceReloadListener() {
                @Override public Identifier getFabricId() {
                    return Identifier.of(LateGamePlus.MOD_ID, "debug_models");
                }
                @Override public void reload(ResourceManager manager) {
                    System.out.println("[LGP] Reload listener disparado");
                    // Verifica que el archivo items/<id>.json exista
                    var id = Identifier.of(LateGamePlus.MOD_ID, "items/void_crystal.json");
                    var res = manager.getResource(id);
                    System.out.println("[LGP] items/void_crystal.json => " + (res.isPresent() ? "ENCONTRADO" : "NO ENCONTRADO"));
                }
            });

    }
}
