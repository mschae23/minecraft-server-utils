package de.martenschaefer.serverutils.config.v5;

import com.mojang.serialization.Codec;

@Deprecated
public record RegionConfigV5(boolean enabled) {
    public static final Codec<RegionConfigV5> CODEC = Codec.BOOL.fieldOf("enabled").xmap(RegionConfigV5::new, RegionConfigV5::enabled).codec();

    public static final RegionConfigV5 DEFAULT = new RegionConfigV5(true);
}
