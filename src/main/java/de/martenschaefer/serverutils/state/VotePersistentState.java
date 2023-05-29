package de.martenschaefer.serverutils.state;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import de.martenschaefer.serverutils.ServerUtilsMod;
import com.mojang.datafixers.util.Pair;

public class VotePersistentState extends PersistentState {
    public static final String ID = ServerUtilsMod.MODID + "_vote";

    private final VoteStorage storage;

    private VotePersistentState(VoteStorage storage) {
        this.storage = storage;
    }

    private VotePersistentState() {
        this(new VoteStorage());
    }

    public VoteStorage getStorage() {
        return this.storage;
    }

    @Override
    public boolean isDirty() {
        return true;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound root) {
        VoteStorage.CODEC.encodeStart(NbtOps.INSTANCE, this.storage)
            .result().ifPresentOrElse(result -> root.put("vote_storage", result),
                () -> ServerUtilsMod.LOGGER.error("Error writing vote data as persistent state"));
        return root;
    }

    private static VotePersistentState readNbt(NbtCompound root) {
        return VoteStorage.CODEC.decode(NbtOps.INSTANCE, root.get("vote_storage"))
            .map(Pair::getFirst)
            .result()
            .map(VotePersistentState::new).orElseGet(VotePersistentState::new);
    }

    public static VotePersistentState get(MinecraftServer server) {
        PersistentStateManager stateManager = server.getOverworld().getPersistentStateManager();
        return stateManager.getOrCreate(VotePersistentState::readNbt, VotePersistentState::new, ID);
    }
}
