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
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.PersistentStateType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.mschae23.serverutils.ServerUtilsMod;

public class VotePersistentState extends PersistentState {
    public static final String ID = ServerUtilsMod.MODID + "_vote";

    public static final Codec<VotePersistentState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        VoteStorage.CODEC.fieldOf("vote_storage").forGetter(VotePersistentState::getStorage)
    ).apply(instance, instance.stable(VotePersistentState::new)));

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

    public static VotePersistentState get(MinecraftServer server) {
        PersistentStateManager stateManager = server.getOverworld().getPersistentStateManager();
        return stateManager.getOrCreate(new PersistentStateType<>(ID, VotePersistentState::new, CODEC, DataFixTypes.LEVEL));
    }
}
