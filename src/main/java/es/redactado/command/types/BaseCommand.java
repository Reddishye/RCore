package es.redactado.command.types;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import es.redactado.command.exceptions.CommandSetupException;
import es.redactado.command.exceptions.CommandSuggestionBuildingException;
import es.redactado.command.exceptions.SubCommandFetchException;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an abstract base class for commands.
 * <p>
 * This class provides a framework for creating commands with subcommands,
 * permission checks, and argument management.
 */
public abstract class BaseCommand {

    private final List<SubCommand> subCommands = new ArrayList<>();

    /**
     * Registers the command and its subcommands with the given dispatcher.
     *
     * @param dispatcher the Brigadier command dispatcher
     */
    public void register(CommandDispatcher<CommandSender> dispatcher) {
        try {
            LiteralArgumentBuilder<CommandSender> command = LiteralArgumentBuilder.<CommandSender>literal(getName())
                    .requires(sender -> sender != null && sender.hasPermission(getPermission())) // Permission check
                    .executes(this::executeDefault); // Default execution

            // Register all subcommands
            for (SubCommand subCommand : subCommands) {
                command.then(subCommand.build());
            }

            dispatcher.register(command);
        } catch (Exception e) {
            throw new CommandSetupException(e.toString());
        }
    }

    /**
     * Adds a subcommand to this command.
     *
     * @param subCommand the subcommand to add
     */
    public void addSubCommand(SubCommand subCommand) {
        subCommands.add(subCommand);
    }

    /**
     * Gets the name of this command.
     *
     * @return the name of the command
     */
    protected abstract String getName();

    /**
     * Gets the permission required to execute this command.
     *
     * @return the permission string
     */
    protected abstract String getPermission();

    /**
     * Executes the default behavior of this command when no arguments are provided.
     *
     * @param context the command context
     * @return an integer indicating the result of the execution
     */
    protected abstract int executeDefault(CommandContext<CommandSender> context);

    /**
     * Gets the subcommand with the given name.
     *
     * @param name the name of the subcommand
     * @return the subcommand with the given name, or null if not found
     */
    protected SubCommand getSubCommand(String name) {
        try {
            for (SubCommand subCommand : subCommands) {
                if (subCommand.getName().equalsIgnoreCase(name)) {
                    return subCommand;
                }
            }

            return null;
        } catch (Exception e) {
            throw new SubCommandFetchException(e.toString());
        }
    }

    /**
     * Gets the suggestions for the given argument.
     *
     * @param context the command context
     * @param args    the arguments
     * @return a list of suggestions
     */
    protected List<String> getSuggestions(CommandContext<CommandSender> context, String[] args) {
        try {
            SubCommand subCommand = getSubCommand(args[0]);
            if (subCommand == null) {
                return new ArrayList<>();
            }

            return subCommand.suggest(context, args);
        } catch (Exception e) {
            throw new CommandSuggestionBuildingException(e.toString());
        }
    }
}