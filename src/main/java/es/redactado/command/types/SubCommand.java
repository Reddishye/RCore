package es.redactado.command.types;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * Represents an abstract base class for subcommands.
 * <p>
 * Subcommands are modular components of a parent command that can be executed independently,
 * with support for argument management and intelligent suggestion handling.
 */
@Slf4j
public abstract class SubCommand {

    /**
     * Builds the Brigadier node for this subcommand.
     *
     * @return a {@link LiteralArgumentBuilder} representing this subcommand
     */
    public LiteralArgumentBuilder<CommandSender> build() {
        return LiteralArgumentBuilder.<CommandSender>literal(getName())
                .requires(sender -> sender != null && sender.hasPermission(getPermission())) // Permission check
                .executes(this::execute) // Main execution logic
                .then(RequiredArgumentBuilder.<CommandSender, String>argument("argument", StringArgumentType.word()) // Argument with suggestions
                        .suggests(getSuggestions())); // Suggestion provider for dynamic tab completion
    }

    /**
     * Dynamic suggestion provider for tab-completion.
     *
     * @return a {@link SuggestionProvider} that generates the suggestions.
     */
    private SuggestionProvider<CommandSender> getSuggestions() {
        return (context, builder) -> {
            List<String> suggestions = suggest(context, builder.getInput().split(" ")); // Method to fetch custom suggestions
            for (String suggestion : suggestions) {
                builder.suggest(suggestion);
            }
            return builder.buildFuture();
        };
    }

    /**
     * Gets the name of this subcommand.
     *
     * @return the name of the subcommand
     */
    protected abstract String getName();

    /**
     * Gets the permission required to execute this subcommand.
     *
     * @return the permission string
     */
    protected abstract String getPermission();

    /**
     * Executes the behavior of this subcommand.
     *
     * @param context the command context
     * @return an integer indicating the result of the execution
     */
    protected abstract int execute(CommandContext<CommandSender> context);

    /**
     * Abstract method to generate custom suggestions.
     * This method must be implemented by subclasses.
     *
     * @param context the current command context
     * @return a list of strings with the available suggestions
     */
    protected abstract List<String> suggest(CommandContext<CommandSender> context, String[] args);
}
