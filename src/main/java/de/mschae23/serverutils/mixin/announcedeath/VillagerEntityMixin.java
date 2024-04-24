/*
 * Copyright (C) 2024  mschae23
 *
 * This file is part of Server utils.
 *
 * Server utils is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package de.mschae23.serverutils.mixin.announcedeath;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import de.mschae23.serverutils.ServerUtilsMod;
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

        world.getServer().getPlayerManager().broadcast(entity.getDamageTracker().getDeathMessage(), false);
    }

    @Redirect(method = "onStruckByLightning", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;info(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V", remap = false))
    private void redirectLogVillagerStruckByLightning(Logger logger, String logText, Object arg1, Object arg2, ServerWorld world, LightningEntity lightning) {
        if (!ServerUtilsMod.getConfig().misc().broadcastEntityDeath().enabled()) {
            logger.info(logText, arg1, arg2);
            return;
        }

        world.getServer().getPlayerManager().broadcast(Text.translatable("death.attack.lightningBolt", this.getDisplayName()), false);
    }
}
