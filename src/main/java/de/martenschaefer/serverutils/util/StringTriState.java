package de.martenschaefer.serverutils.util;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.util.Formatting;
import net.minecraft.util.StringIdentifiable;
import net.fabricmc.fabric.api.util.TriState;
import org.jetbrains.annotations.Nullable;

public enum StringTriState implements StringIdentifiable {
    FALSE("false", TriState.FALSE, Formatting.RED),
    DEFAULT("default", TriState.DEFAULT, Formatting.GRAY),
    TRUE("true", TriState.TRUE, Formatting.GREEN);

    public static final com.mojang.serialization.Codec<StringTriState> CODEC = StringIdentifiable.createCodec(StringTriState::values);
    private static final Map<String, TriState> BY_NAME = Arrays.stream(values()).collect(Collectors.toMap(StringTriState::asString, StringTriState::getState));

    private final String name;
    private final TriState state;
    private final Formatting formatting;

    StringTriState(String name, TriState state, Formatting formatting) {
        this.name = name;
        this.state = state;
        this.formatting = formatting;
    }

    public TriState getState() {
        return this.state;
    }

    public Formatting getFormatting() {
        return this.formatting;
    }

    @Override
    public String asString() {
        return this.name;
    }

    @Nullable
    public static TriState byName(String name) {
        return BY_NAME.get(name);
    }

    public static StringTriState from(TriState state) {
        return switch (state) {
            case FALSE -> FALSE;
            case DEFAULT -> DEFAULT;
            case TRUE -> TRUE;
        };
    }
}
