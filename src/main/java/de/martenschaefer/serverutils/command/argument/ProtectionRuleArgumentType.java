package de.martenschaefer.serverutils.command.argument;

import net.minecraft.command.argument.EnumArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import de.martenschaefer.serverutils.region.rule.ProtectionRule;
import com.mojang.brigadier.context.CommandContext;

@Deprecated
public class ProtectionRuleArgumentType extends EnumArgumentType<ProtectionRule> {
    private ProtectionRuleArgumentType() {
        super(ProtectionRule.CODEC, ProtectionRule::values);
    }

    public static ProtectionRuleArgumentType protectionRule() {
        return new ProtectionRuleArgumentType();
    }

    public static ProtectionRule getProtectionRule(CommandContext<ServerCommandSource> context, String id) {
        return context.getArgument(id, ProtectionRule.class);
    }
}
