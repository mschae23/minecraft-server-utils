package de.martenschaefer.serverutils.vote;

import net.minecraft.text.Text;
import net.minecraft.util.dynamic.Codecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class VoteOption {
        public static final Codec<VoteOption> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.fieldOf("name").forGetter(VoteOption::getName),
        Codecs.TEXT.fieldOf("displayname").forGetter(VoteOption::getDisplayName)
    ).apply(instance, instance.stable(VoteOption::new)));

    private final String name;
    private Text displayName;

    public VoteOption(String name, Text displayName) {
        this.name = name;
        this.displayName = displayName;
    }

    public VoteOption(String name) {
        this(name, Text.literal(name));
    }

    public String getName() {
        return this.name;
    }

    public Text getDisplayName() {
        return this.displayName;
    }

    public void setDisplayName(Text displayName) {
        this.displayName = displayName;
    }
}
