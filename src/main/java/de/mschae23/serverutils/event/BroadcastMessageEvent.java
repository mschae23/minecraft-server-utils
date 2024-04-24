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

package de.mschae23.serverutils.event;

import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.util.TriState;

public class BroadcastMessageEvent {
    public static final Event<BroadcastMessage> EVENT = EventFactory.createArrayBacked(BroadcastMessage.class, callbacks -> (server, message) -> {
        for (BroadcastMessage callback : callbacks) {
            TriState result = callback.announce(server, message);

            if (result != TriState.DEFAULT) {
                return result;
            }
        }

        return TriState.DEFAULT;
    });

    public interface BroadcastMessage {
        TriState announce(MinecraftServer server, Text message);
    }
}
