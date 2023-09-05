package de.martenschaefer.serverutils.event;

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
