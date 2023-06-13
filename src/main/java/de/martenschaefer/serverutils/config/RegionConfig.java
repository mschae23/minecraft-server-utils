package de.martenschaefer.serverutils.config;

import com.mojang.serialization.Codec;

public record RegionConfig(boolean enabled) {
    public static final Codec<RegionConfig> CODEC = Codec.BOOL.fieldOf("enabled").xmap(RegionConfig::new, RegionConfig::enabled).codec();

    public static final RegionConfig DEFAULT = new RegionConfig(true);
}
