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

public record ContainerLockConfig(boolean enabled, String dataKey, String permissionPrefix) {
    public static final Codec<ContainerLockConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.BOOL.fieldOf("enabled").forGetter(ContainerLockConfig::enabled),
        Codec.STRING.fieldOf("data_key").forGetter(ContainerLockConfig::dataKey),
        Codec.STRING.fieldOf("key_permission_prefix").forGetter(ContainerLockConfig::permissionPrefix)
    ).apply(instance, instance.stable(ContainerLockConfig::new)));

    public static final ContainerLockConfig DEFAULT = new ContainerLockConfig(true, "lock_permission", "key");
}
