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

package de.mschae23.serverutils.config.v1;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.mschae23.config.api.ModConfig;
import de.mschae23.serverutils.config.BroadcastEntityDeathConfig;
import de.mschae23.serverutils.config.ChatConfig;
import de.mschae23.serverutils.config.ContainerLockConfig;
import de.mschae23.serverutils.config.DeathCoordsConfig;
import de.mschae23.serverutils.config.ServerUtilsConfigV6;
import de.mschae23.serverutils.config.command.CommandConfig;
import de.mschae23.serverutils.config.v2.ServerUtilsConfigV2;
import de.mschae23.serverutils.config.v3.MiscConfigV3;

@SuppressWarnings("DeprecatedIsStillUsed")
@Deprecated
public record ServerUtilsConfigV1(CommandConfig command,
                                  ChatConfig chat,
                                  DeathCoordsConfig deathCoords,
                                  BroadcastEntityDeathConfig broadcastEntityDeath,
                                  ContainerLockConfig lock) implements ModConfig<ServerUtilsConfigV6> {
    public static final MapCodec<ServerUtilsConfigV1> TYPE_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        CommandConfig.CODEC.fieldOf("command").forGetter(ServerUtilsConfigV1::command),
        ChatConfig.CODEC.fieldOf("chat").forGetter(ServerUtilsConfigV1::chat),
        DeathCoordsConfig.CODEC.fieldOf("death_coords").forGetter(ServerUtilsConfigV1::deathCoords),
        BroadcastEntityDeathConfig.CODEC.fieldOf("broadcast_entity_death").forGetter(ServerUtilsConfigV1::broadcastEntityDeath),
        ContainerLockConfig.CODEC.fieldOf("container_lock").forGetter(ServerUtilsConfigV1::lock)
    ).apply(instance, instance.stable(ServerUtilsConfigV1::new)));

    public static final ModConfig.Type<ServerUtilsConfigV6, ServerUtilsConfigV1> TYPE = new ModConfig.Type<>(1, TYPE_CODEC);

    public static final ServerUtilsConfigV1 DEFAULT =
        new ServerUtilsConfigV1(CommandConfig.DEFAULT, ChatConfig.DEFAULT, DeathCoordsConfig.DEFAULT, BroadcastEntityDeathConfig.DEFAULT, ContainerLockConfig.DEFAULT);

    @Override
    public ModConfig.Type<ServerUtilsConfigV6, ?> type() {
        return TYPE;
    }

    @Override
    public ServerUtilsConfigV6 latest() {
        return new ServerUtilsConfigV2(this.command, this.chat, this.deathCoords, this.broadcastEntityDeath, this.lock, MiscConfigV3.DEFAULT).latest();
    }

    @Override
    public boolean shouldUpdate() {
        return true;
    }
}
