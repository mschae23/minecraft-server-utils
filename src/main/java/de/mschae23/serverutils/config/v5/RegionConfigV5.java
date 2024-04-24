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

package de.mschae23.serverutils.config.v5;

import com.mojang.serialization.Codec;

@Deprecated
public record RegionConfigV5(boolean enabled) {
    public static final Codec<RegionConfigV5> CODEC = Codec.BOOL.fieldOf("enabled").xmap(RegionConfigV5::new, RegionConfigV5::enabled).codec();

    public static final RegionConfigV5 DEFAULT = new RegionConfigV5(true);
}
