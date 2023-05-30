package de.martenschaefer.serverutils.mixin.region;

import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import de.martenschaefer.serverutils.region.RegionRuleEnforcer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Entity.class)
public class EntityMixin {
    @Shadow
    protected int netherPortalTime;

    @Redirect(method = "tickPortal", at = @At(value = "INVOKE", target = "net/minecraft/server/MinecraftServer.isNetherAllowed ()Z"))
    private boolean onIsNetherAllowed(MinecraftServer server) {
        Entity self = (Entity) (Object) this;

        if (this.netherPortalTime >= self.getMaxNetherPortalTime() && self instanceof ServerPlayerEntity player) {
            ActionResult result = RegionRuleEnforcer.onNetherPortalUse(player, player.getPos());

            if (result == ActionResult.FAIL) {
                return false;
            }
        }

        return server.isNetherAllowed();
    }
}
