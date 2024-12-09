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

package de.mschae23.serverutils.holder;

import org.jetbrains.annotations.NotNull;

public interface LockPermissionHolder {
    @NotNull
    String getLockPermission();

    /**
     * Sets the lock permission. This should be used with caution, as this interface is implemented on
     * {@link net.minecraft.inventory.ContainerLock}, which is a record and therefore expected to be immutable.
     *
     * @param permission the lock permission
     */
    void setLockPermission(String permission);
}
