package de.martenschaefer.serverutils.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record MiscConfigV2(EnableGamemodeSwitcherConfigV2 enableGamemodeSwitcher) {
    public static final Codec<MiscConfigV2> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        EnableGamemodeSwitcherConfigV2.CODEC.fieldOf("enable_gamemode_switcher").forGetter(MiscConfigV2::enableGamemodeSwitcher)
    ).apply(instance, instance.stable(MiscConfigV2::new)));

    public static final MiscConfigV2 DEFAULT = new MiscConfigV2(EnableGamemodeSwitcherConfigV2.DEFAULT);
}
