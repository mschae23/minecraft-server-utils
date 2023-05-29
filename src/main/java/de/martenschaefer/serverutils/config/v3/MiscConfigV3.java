package de.martenschaefer.serverutils.config.v3;

import de.martenschaefer.serverutils.config.EnableGamemodeSwitcherConfig;
import de.martenschaefer.serverutils.config.ItemFrameConfig;
import de.martenschaefer.serverutils.config.MiscConfig;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record MiscConfigV3(EnableGamemodeSwitcherConfig enableGamemodeSwitcher) {
    public static final Codec<MiscConfigV3> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        EnableGamemodeSwitcherConfig.CODEC.fieldOf("enable_gamemode_switcher").forGetter(MiscConfigV3::enableGamemodeSwitcher)
    ).apply(instance, instance.stable(MiscConfigV3::new)));

    public static final MiscConfigV3 DEFAULT = new MiscConfigV3(EnableGamemodeSwitcherConfig.DEFAULT);

    public MiscConfig latest() {
        return new MiscConfig(this.enableGamemodeSwitcher, ItemFrameConfig.DEFAULT);
    }
}
