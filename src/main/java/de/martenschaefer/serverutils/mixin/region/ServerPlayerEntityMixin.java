package de.martenschaefer.serverutils.mixin.region;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import de.martenschaefer.serverutils.region.RegionRuleEnforcer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin {
    @Redirect(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;isPvpEnabled()Z"))
    private boolean redirectIsPvpEnabled(ServerPlayerEntity player, DamageSource source) {
        ActionResult result = RegionRuleEnforcer.onPlayerPvp(player, player.getPos());

        if (result == ActionResult.FAIL) {
            return false;
        } else if (source.getAttacker() instanceof ServerPlayerEntity attacker) {
            result = RegionRuleEnforcer.onPlayerPvpSendDenied(attacker, attacker.getPos());

            if (result == ActionResult.FAIL) {
                return false;
            }
        }

        return this.isPvpEnabled();
    }

    @Shadow
    protected abstract boolean isPvpEnabled();
}
