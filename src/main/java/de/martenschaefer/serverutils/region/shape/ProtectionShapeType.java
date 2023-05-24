package de.martenschaefer.serverutils.region.shape;

import net.minecraft.registry.Registry;
import de.martenschaefer.serverutils.ServerUtilsMod;
import de.martenschaefer.serverutils.registry.ServerUtilsRegistries;
import com.mojang.serialization.Codec;

public interface ProtectionShapeType<S extends ProtectionShape> {
    ProtectionShapeType<UniversalShape> UNIVERSAL = register("universal", UniversalShape.CODEC);
    ProtectionShapeType<DimensionShape> DIMENSION = register("dimension", DimensionShape.CODEC);
    ProtectionShapeType<BoxShape> BOX = register("box", BoxShape.CODEC);
    ProtectionShapeType<UnionShape> UNION = register("union", UnionShape.CODEC);

    Codec<S> codec();

    static <S extends ProtectionShape> ProtectionShapeType<S> register(String id, Codec<S> codec) {
        return Registry.register(ServerUtilsRegistries.PROTECTION_SHAPE, ServerUtilsMod.id(id), () -> codec);
    }

    static void init() {
    }
}
