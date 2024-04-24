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

package de.mschae23.serverutils.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record MiscConfig(DeathCoordsConfig deathCoords, BroadcastEntityDeathConfig broadcastEntityDeath, EnableGamemodeSwitcherConfig enableGamemodeSwitcher, ItemFrameConfig itemFrame) {
    public static final Codec<MiscConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        DeathCoordsConfig.CODEC.fieldOf("death_coords").forGetter(MiscConfig::deathCoords),
        BroadcastEntityDeathConfig.CODEC.fieldOf("broadcast_entity_death").forGetter(MiscConfig::broadcastEntityDeath),
        EnableGamemodeSwitcherConfig.CODEC.fieldOf("enable_gamemode_switcher").forGetter(MiscConfig::enableGamemodeSwitcher),
        ItemFrameConfig.CODEC.fieldOf("item_frame").forGetter(MiscConfig::itemFrame)
    ).apply(instance, instance.stable(MiscConfig::new)));

    public static final MiscConfig DEFAULT = new MiscConfig(DeathCoordsConfig.DEFAULT, BroadcastEntityDeathConfig.DEFAULT, EnableGamemodeSwitcherConfig.DEFAULT, ItemFrameConfig.DEFAULT);
}
