package de.martenschaefer.serverutils.mixin.announcedeath;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import de.martenschaefer.serverutils.ServerUtilsMod;
import de.martenschaefer.serverutils.event.AnnounceEntityDeathEvent;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Redirect(method = "onDeath", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;info(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V", remap = false))
    private void redirectLogNamedEntityDeath(Logger logger, String logText, Object entityObject, Object deathMessage, DamageSource source) {
        LivingEntity entity = (LivingEntity) entityObject;
        World world = entity.getWorld();

        if (!ServerUtilsMod.getConfig().misc().broadcastEntityDeath().enabled() || !(world instanceof ServerWorld serverWorld)) {
            logger.info(logText, entityObject, deathMessage);
            return;
        }

        AnnounceEntityDeathEvent.EVENT.invoker().announce(serverWorld, (LivingEntity) (Object) this, source.getDeathMessage(entity));
    }
}
