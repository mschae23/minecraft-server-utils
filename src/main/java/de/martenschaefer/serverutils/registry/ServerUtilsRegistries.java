package de.martenschaefer.serverutils.registry;

import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import de.martenschaefer.serverutils.ServerUtilsMod;
import de.martenschaefer.serverutils.region.shape.ProtectionShapeType;

public class ServerUtilsRegistries {
    public static final Registry<ProtectionShapeType<?>> PROTECTION_SHAPE = FabricRegistryBuilder.<ProtectionShapeType<?>>createSimple(RegistryKey.ofRegistry(ServerUtilsMod.id("protection_shape"))).buildAndRegister();

    private ServerUtilsRegistries() {
    }

    public static void init() {
    }
}
