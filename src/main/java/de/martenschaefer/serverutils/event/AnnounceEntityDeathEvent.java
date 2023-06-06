package de.martenschaefer.serverutils.event;

import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.util.TriState;

public class AnnounceEntityDeathEvent {
    public static final Event<AnnounceEntityDeath> EVENT = EventFactory.createArrayBacked(AnnounceEntityDeath.class, callbacks -> (world, entity, message) -> {
        for (AnnounceEntityDeath callback : callbacks) {
            TriState result = callback.announce(world, entity, message);

            if (result != TriState.DEFAULT) {
                return result;
            }
        }

        return TriState.DEFAULT;
    });

    public interface AnnounceEntityDeath {
        TriState announce(ServerWorld world, LivingEntity entity, Text message);
    }
}
