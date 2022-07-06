package de.martenschaefer.serverutils.config.impl;

import de.martenschaefer.serverutils.config.ServerUtilsConfigV2;
import de.martenschaefer.serverutils.config.v1.ServerUtilsConfigV1;
import com.mojang.serialization.Codec;

public interface ModConfig {
    Codec<ModConfig> CODEC = Type.CODEC.dispatch("version", ModConfig::type, Type::codec);

    int LATEST_VERSION = 2;

    ServerUtilsConfigV2 LATEST_DEFAULT = ServerUtilsConfigV2.DEFAULT;

    Type<?> type();

    default int version() {
        return type().version();
    }

    ServerUtilsConfigV2 latest();

    boolean shouldUpdate();

    static Type<?> getType(int version) {
        return new Type<ModConfig>(version, switch (version) {
            case 1 -> ServerUtilsConfigV1.TYPE_CODEC;
            default -> ServerUtilsConfigV2.TYPE_CODEC;
        });
    }

    record Type<T extends ModConfig>(int version, Codec<? extends T> codec) {
        public static final Codec<Type<?>> CODEC = Codec.intRange(1, ModConfig.LATEST_VERSION).xmap(ModConfig::getType, Type::version);
    }
}
