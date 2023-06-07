package de.martenschaefer.serverutils.mixin.announcedeath;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import de.martenschaefer.serverutils.ServerUtilsMod;
import de.martenschaefer.serverutils.event.AnnounceEntityDeathEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TameableEntity.class)
public abstract class TameableEntityMixin extends AnimalEntity {
    protected TameableEntityMixin(EntityType<? extends AnimalEntity> entityType, World world) {
        super(entityType, world);
    }

    @Redirect(method = "onDeath", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/GameRules;getBoolean(Lnet/minecraft/world/GameRules$Key;)Z"))
    private boolean redirectShouldShowDeathMessages(GameRules instance, GameRules.Key<GameRules.BooleanRule> rule) {
        return ServerUtilsMod.getConfig().broadcastEntityDeath().enabled() || instance.getBoolean(rule);
    }

    @Redirect(method = "onDeath", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;sendMessage(Lnet/minecraft/text/Text;)V"))
    private void redirectSendMessage(LivingEntity owner, Text message) {
        if (!ServerUtilsMod.getConfig().broadcastEntityDeath().enabled()) {
            owner.sendMessage(message);
            return;
        }

        if (this.getWorld() instanceof ServerWorld world) {
            AnnounceEntityDeathEvent.EVENT.invoker().announce(world, this, message);
        }
    }
}
