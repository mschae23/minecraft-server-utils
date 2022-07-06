package de.martenschaefer.serverutils.holder;

import org.jetbrains.annotations.NotNull;

public interface LockPermissionHolder {
    @NotNull
    String getLockPermission();

    void setLockPermission(String permission);
}
