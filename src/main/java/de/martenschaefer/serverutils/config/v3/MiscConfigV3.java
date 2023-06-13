package de.martenschaefer.serverutils.config.v3;

import de.martenschaefer.serverutils.config.BroadcastEntityDeathConfig;
import de.martenschaefer.serverutils.config.DeathCoordsConfig;
import de.martenschaefer.serverutils.config.EnableGamemodeSwitcherConfig;
import de.martenschaefer.serverutils.config.ItemFrameConfig;
import de.martenschaefer.serverutils.config.MiscConfig;
import de.martenschaefer.serverutils.config.v4.MiscConfigV4;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

@Deprecated
public record MiscConfigV3(EnableGamemodeSwitcherConfig enableGamemodeSwitcher) {
    public static final Codec<MiscConfigV3> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        EnableGamemodeSwitcherConfig.CODEC.fieldOf("enable_gamemode_switcher").forGetter(MiscConfigV3::enableGamemodeSwitcher)
    ).apply(instance, instance.stable(MiscConfigV3::new)));

    public static final MiscConfigV3 DEFAULT = new MiscConfigV3(EnableGamemodeSwitcherConfig.DEFAULT);

    public MiscConfigV4 latestV4() {
        return new MiscConfigV4(this.enableGamemodeSwitcher, ItemFrameConfig.DEFAULT);
    }
}
