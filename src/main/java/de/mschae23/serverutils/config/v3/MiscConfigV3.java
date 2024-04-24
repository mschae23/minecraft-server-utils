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

package de.mschae23.serverutils.config.v3;

import de.mschae23.serverutils.config.EnableGamemodeSwitcherConfig;
import de.mschae23.serverutils.config.ItemFrameConfig;
import de.mschae23.serverutils.config.v4.MiscConfigV4;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

@Deprecated
public record MiscConfigV3(EnableGamemodeSwitcherConfig enableGamemodeSwitcher) {
    public static final Codec<MiscConfigV3> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        EnableGamemodeSwitcherConfig.CODEC.fieldOf("enable_gamemode_switcher").forGetter(MiscConfigV3::enableGamemodeSwitcher)
    ).apply(instance, instance.stable(MiscConfigV3::new)));

    public static final MiscConfigV3 DEFAULT = new MiscConfigV3(EnableGamemodeSwitcherConfig.DEFAULT);

    public MiscConfigV4 latestV4() {
        return new MiscConfigV4(this.enableGamemodeSwitcher, ItemFrameConfig.DEFAULT);
    }
}
