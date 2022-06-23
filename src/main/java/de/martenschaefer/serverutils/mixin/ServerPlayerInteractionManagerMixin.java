package de.martenschaefer.serverutils.mixin;

import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerInteractionManager.class)
public class ServerPlayerInteractionManagerMixin {
    @Unique
    private static LuckPerms serverutils_luckPerms = null;

    @Unique
    private int serverutils_ticksSinceUpdate = 0;

    @Unique
    @Nullable
    private String serverutils_lastColorName = null;

    @Shadow
    private int tickCounter;

    @Shadow
    @Final
    protected ServerPlayerEntity player;

    @Inject(method = "update", at = @At("RETURN"))
    private void onUpdate(CallbackInfo ci) {
        if (this.serverutils_ticksSinceUpdate++ >= 100) {
            User user = getLuckPerms().getPlayerAdapter(ServerPlayerEntity.class).getUser(this.player);
            String colorName = user.getCachedData().getMetaData().getMetaValue("username-color");

            if (colorName != null && !colorName.equals(serverutils_lastColorName)) {
                this.serverutils_lastColorName = colorName;
                this.player.server.getPlayerManager().sendToAll(new PlayerListS2CPacket(PlayerListS2CPacket.Action.UPDATE_DISPLAY_NAME, this.player));
            }

            this.serverutils_ticksSinceUpdate = 0;
        }
    }

    @Unique
    private static LuckPerms getLuckPerms() {
        if (serverutils_luckPerms == null) {
            serverutils_luckPerms = LuckPermsProvider.get();
        }

        return serverutils_luckPerms;
    }
}
