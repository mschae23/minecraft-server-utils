/*
 * Copyright (C) 2026  mschae23
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

public record GameRulesConfig(String keepInventoryPermission, String pvpPermission) {
    public static final Codec<GameRulesConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.fieldOf("keep_inventory_permission").forGetter(GameRulesConfig::keepInventoryPermission),
        Codec.STRING.fieldOf("pvp_permission").forGetter(GameRulesConfig::pvpPermission)
    ).apply(instance, instance.stable(GameRulesConfig::new)));

    public static final GameRulesConfig DEFAULT = new GameRulesConfig("serverutils.rule.keepinventory", "serverutils.rule.pvp");
}
