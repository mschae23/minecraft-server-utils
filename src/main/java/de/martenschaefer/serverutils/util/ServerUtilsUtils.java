package de.martenschaefer.serverutils.util;

import net.fabricmc.fabric.api.util.TriState;
import net.luckperms.api.util.Tristate;

public final class ServerUtilsUtils {
    private ServerUtilsUtils() {
    }

    public static TriState toFabricTriState(Tristate state) {
        return switch (state) {
            case TRUE -> TriState.TRUE;
            case FALSE -> TriState.FALSE;
            case UNDEFINED -> TriState.DEFAULT;
        };
    }

    public static TriState orTriState(TriState state, TriState other) {
        return state == TriState.DEFAULT ? other : state;
    }
}
