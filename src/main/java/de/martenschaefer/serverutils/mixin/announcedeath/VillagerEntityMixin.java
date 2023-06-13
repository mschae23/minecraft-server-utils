package de.martenschaefer.serverutils.mixin.announcedeath;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import de.martenschaefer.serverutils.ServerUtilsMod;
import de.martenschaefer.serverutils.event.AnnounceEntityDeathEvent;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(VillagerEntity.class)
public abstract class VillagerEntityMixin extends MerchantEntity {
    public VillagerEntityMixin(EntityType<? extends MerchantEntity> entityType, World world) {
        super(entityType, world);
    }

    @Redirect(method = "onDeath", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;info(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V", remap = false))
    private void redirectLogVillagerDeath(Logger logger, String logText, Object entityObject, Object deathMessage) {
        VillagerEntity entity = (VillagerEntity) entityObject;
        World world = entity.getWorld();

        if (!ServerUtilsMod.getConfig().misc().broadcastEntityDeath().enabled() || entity.getWorld().isClient() || !(world instanceof ServerWorld serverWorld)) {
            logger.info(logText, entityObject, deathMessage);
            return;
        }

        AnnounceEntityDeathEvent.EVENT.invoker().announce(serverWorld, this, entity.getDamageTracker().getDeathMessage());
    }

    @Redirect(method = "onStruckByLightning", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;info(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V", remap = false))
    private void redirectLogVillagerStruckByLightning(Logger logger, String logText, Object arg1, Object arg2, ServerWorld world, LightningEntity lightning) {
        if (!ServerUtilsMod.getConfig().misc().broadcastEntityDeath().enabled()) {
            logger.info(logText, arg1, arg2);
            return;
        }

        AnnounceEntityDeathEvent.EVENT.invoker().announce(world, this, Text.translatable("death.attack.lightningBolt", this.getDisplayName()));
    }
}
