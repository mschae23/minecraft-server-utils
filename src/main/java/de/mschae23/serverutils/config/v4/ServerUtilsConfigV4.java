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

package de.mschae23.serverutils.config.v4;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.mschae23.config.api.ModConfig;
import de.mschae23.serverutils.config.BroadcastEntityDeathConfig;
import de.mschae23.serverutils.config.ChatConfig;
import de.mschae23.serverutils.config.ContainerLockConfig;
import de.mschae23.serverutils.config.DeathCoordsConfig;
import de.mschae23.serverutils.config.MiscConfig;
import de.mschae23.serverutils.config.ServerUtilsConfigV6;
import de.mschae23.serverutils.config.VoteConfig;
import de.mschae23.serverutils.config.command.CommandConfig;

@Deprecated
@SuppressWarnings("DeprecatedIsStillUsed")
public record ServerUtilsConfigV4(CommandConfig command,
                                  ChatConfig chat,
                                  DeathCoordsConfig deathCoords,
                                  BroadcastEntityDeathConfig broadcastEntityDeath,
                                  ContainerLockConfig lock,
                                  VoteConfig vote,
                                  MiscConfigV4 misc) implements ModConfig<ServerUtilsConfigV6> {
    public static final MapCodec<ServerUtilsConfigV4> TYPE_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        CommandConfig.CODEC.fieldOf("command").forGetter(ServerUtilsConfigV4::command),
        ChatConfig.CODEC.fieldOf("chat").forGetter(ServerUtilsConfigV4::chat),
        DeathCoordsConfig.CODEC.fieldOf("death_coords").forGetter(ServerUtilsConfigV4::deathCoords),
        BroadcastEntityDeathConfig.CODEC.fieldOf("broadcast_entity_death").forGetter(ServerUtilsConfigV4::broadcastEntityDeath),
        ContainerLockConfig.CODEC.fieldOf("container_lock").forGetter(ServerUtilsConfigV4::lock),
        VoteConfig.CODEC.fieldOf("vote").forGetter(ServerUtilsConfigV4::vote),
        MiscConfigV4.CODEC.fieldOf("misc").forGetter(ServerUtilsConfigV4::misc)
    ).apply(instance, instance.stable(ServerUtilsConfigV4::new)));

    public static final Type<ServerUtilsConfigV6, ServerUtilsConfigV4> TYPE = new Type<>(4, TYPE_CODEC);

    public static final ServerUtilsConfigV4 DEFAULT =
        new ServerUtilsConfigV4(CommandConfig.DEFAULT, ChatConfig.DEFAULT, DeathCoordsConfig.DEFAULT, BroadcastEntityDeathConfig.DEFAULT, ContainerLockConfig.DEFAULT, VoteConfig.DEFAULT, MiscConfigV4.DEFAULT);

    @Override
    public Type<ServerUtilsConfigV6, ?> type() {
        return TYPE;
    }

    @Override
    public ServerUtilsConfigV6 latest() {
        return new ServerUtilsConfigV6(this.command, this.chat, this.lock, this.vote,
            new MiscConfig(this.deathCoords, this.broadcastEntityDeath, this.misc.enableGamemodeSwitcher(), this.misc.itemFrame()));
    }

    @Override
    public boolean shouldUpdate() {
        return true;
    }
}
