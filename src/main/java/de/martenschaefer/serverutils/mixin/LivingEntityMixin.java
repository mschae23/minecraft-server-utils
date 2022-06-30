package de.martenschaefer.serverutils.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.network.message.MessageType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import de.martenschaefer.serverutils.ServerUtilsMod;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Redirect(method = "onDeath", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;info(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V"))
    private void redirectLogNamedEntityDeath(Logger logger, String logText, Object entityObject, Object deathMessage, DamageSource source) {
        LivingEntity entity = (LivingEntity) entityObject;
        MinecraftServer server = entity.getServer();

        if (!ServerUtilsMod.getConfig().broadcastEntityDeath().enabled() || server == null) {
            logger.info(logText, entityObject, deathMessage);
            return;
        }

        server.getPlayerManager().broadcast(source.getDeathMessage(entity), false);
    }
}
