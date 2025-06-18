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

package de.mschae23.serverutils.mixin;

import java.util.Optional;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Formatting;
import net.minecraft.world.waypoint.Waypoint;
import net.minecraft.world.waypoint.WaypointStyle;
import de.mschae23.serverutils.ModUtils;
import de.mschae23.serverutils.ServerUtilsMod;
import net.luckperms.api.model.user.User;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Waypoint.Config.class)
public abstract class WaypointConfigMixin {
    @Shadow
    public RegistryKey<WaypointStyle> style;

    @Inject(method = "withTeamColorOf", at = @At("RETURN"), cancellable = true)
    private static void onWithTeamColorOf(LivingEntity entity, CallbackInfoReturnable<Waypoint.Config> cir) {
        if (!ServerUtilsMod.getConfig().chat().enabled()) {
            return;
        }

        if (entity instanceof ServerPlayerEntity player) {
            User user = ModUtils.getLuckPerms().getPlayerAdapter(ServerPlayerEntity.class).getUser(player);
            String colorName = user.getCachedData().getMetaData().getMetaValue("username-color");
            Formatting usernameFormatting = ModUtils.getUsernameFormatting(colorName);
            Optional<Integer> color = Optional.ofNullable(usernameFormatting.getColorValue());

            if (usernameFormatting != Formatting.RESET && color.isPresent()) {
                Waypoint.Config original = cir.getReturnValue();
                cir.setReturnValue(new Waypoint.Config(original.style, color));
            }
        }
    }
}
