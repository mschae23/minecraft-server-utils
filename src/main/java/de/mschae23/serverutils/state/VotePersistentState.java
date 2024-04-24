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

package de.mschae23.serverutils.state;

import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import de.mschae23.serverutils.ServerUtilsMod;
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
    public NbtCompound writeNbt(NbtCompound root, RegistryWrapper.WrapperLookup wrapperLookup) {
        VoteStorage.CODEC.encodeStart(NbtOps.INSTANCE, this.storage)
            .ifSuccess(result -> root.put("vote_storage", result))
            .ifError(partial -> ServerUtilsMod.LOGGER.error("Error writing vote data as persistent state: " + partial.message()));
        return root;
    }

    private static VotePersistentState readNbt(NbtCompound root, RegistryWrapper.WrapperLookup wrapperLookup) {
        return VoteStorage.CODEC.decode(NbtOps.INSTANCE, root.get("vote_storage"))
            .map(Pair::getFirst)
            .result()
            .map(VotePersistentState::new).orElseGet(VotePersistentState::new);
    }

    public static VotePersistentState get(MinecraftServer server) {
        PersistentStateManager stateManager = server.getOverworld().getPersistentStateManager();
        return stateManager.getOrCreate(new Type<>(VotePersistentState::new, VotePersistentState::readNbt, DataFixTypes.LEVEL), ID);
    }
}
