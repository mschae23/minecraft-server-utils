package de.martenschaefer.serverutils.config.impl;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;
import net.minecraft.util.dynamic.RegistryOps;
import net.minecraft.util.registry.DynamicRegistryManager;
import de.martenschaefer.serverutils.ServerUtilsMod;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;

public final class ConfigUtils {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static final Path CONFIG_PATH = Paths.get(ServerUtilsMod.MODID + ".json");

    private ConfigUtils() {
    }

    @SuppressWarnings("deprecation")
    public static ModConfig decodeConfig(InputStream input, DynamicRegistryManager manager) throws IOException {
        try (InputStreamReader reader = new InputStreamReader(new BufferedInputStream(input))) {
            JsonElement element = new JsonParser().parse(reader); // Using this for 1.17 compatibility, would be JsonReader.parseReader in 1.18+

            Either<ModConfig, DataResult.PartialResult<ModConfig>> result =
                ModConfig.CODEC.parse(RegistryOps.of(JsonOps.INSTANCE, manager), element).get();

            return result.map(Function.identity(), partialResult -> {
                throw new RuntimeException("Error decoding config: " + partialResult.message());
            });
        }
    }

    public static void encodeConfig(Writer writer, ModConfig config, DynamicRegistryManager manager) throws IOException {
        DataResult<JsonElement> result = ModConfig.CODEC
            .encodeStart(RegistryOps.of(JsonOps.INSTANCE, manager), config);

        JsonElement element = result.get().map(Function.identity(), partialResult -> {
            throw new RuntimeException("Error encoding config: " + partialResult.message());
        });

        String json = GSON.toJson(element);
        writer.append(json);
    }
}
