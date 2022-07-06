package de.martenschaefer.serverutils.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import de.martenschaefer.serverutils.ServerUtilsMod;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(VillagerEntity.class)
public abstract class VillagerEntityMixin extends Entity {
    public VillagerEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Redirect(method = "onDeath", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;info(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V"))
    private void redirectLogVillagerDeath(Logger logger, String logText, Object entityObject, Object deathMessage) {
        VillagerEntity entity = (VillagerEntity) entityObject;
        MinecraftServer server = entity.getServer();

        if (!ServerUtilsMod.getConfig().chat().enabled() || entity.getWorld().isClient() || server == null) {
            logger.info(logText, entityObject, deathMessage);
            return;
        }

        server.getPlayerManager().broadcast(entity.getDamageTracker().getDeathMessage(), false);
    }

    @Redirect(method = "onStruckByLightning", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;info(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V"))
    private void redirectLogVillagerStruckByLightning(Logger logger, String logText, Object arg1, Object arg2, ServerWorld world, LightningEntity lightning) {
        if (!ServerUtilsMod.getConfig().chat().enabled()) {
            logger.info(logText, arg1, arg2);
            return;
        }

        @SuppressWarnings("ConstantConditions")
        VillagerEntity entity = (VillagerEntity) (Object) this;

        world.getServer().getPlayerManager().broadcast(
            Text.translatable("death.attack.lightningBolt", entity.getDisplayName()), false);
    }
}
