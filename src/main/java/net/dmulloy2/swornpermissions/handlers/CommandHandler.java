/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.swornpermissions.handlers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import net.dmulloy2.chat.ComponentBuilder;
import net.dmulloy2.swornpermissions.SwornPermissions;
import net.dmulloy2.swornpermissions.commands.SwornPermissionsCommand;
import net.dmulloy2.util.FormatUtil;

import org.apache.commons.lang.Validate;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;

/**
 * Handles commands. This supports both prefixed and non-prefixed commands.
 *
 * @author dmulloy2
 */

public class CommandHandler implements CommandExecutor
{
	private String commandPrefix;
	private List<SwornPermissionsCommand> registeredPrefixedCommands;
	private List<SwornPermissionsCommand> registeredCommands;

	private final SwornPermissions plugin;

	public CommandHandler(SwornPermissions plugin)
	{
		this.plugin = plugin;
		this.registeredCommands = new ArrayList<>();
	}

	/**
	 * Registers a non-prefixed {@link SwornPermissionsCommand}.
	 *
	 * @param command Non-prefixed {@link SwornPermissionsCommand} to register.
	 */
	public void registerCommand(SwornPermissionsCommand command)
	{
		Validate.notNull(command, "command cannot be null!");
		PluginCommand pluginCommand = plugin.getCommand(command.getName());
		if (pluginCommand != null)
		{
			pluginCommand.setExecutor(command);
			registeredCommands.add(command);
		}
		else
		{
			plugin.getLogHandler().log(Level.WARNING, "Entry for command {0} is missing in plugin.yml", command.getName());
		}
	}

	/**
	 * Registers a prefixed {@link SwornPermissionsCommand}. The commandPrefix
	 * must be set for this method to work.
	 *
	 * @param command Prefixed {@link SwornPermissionsCommand} to register.
	 */
	public void registerPrefixedCommand(SwornPermissionsCommand command)
	{
		Validate.notNull(command, "command cannot be null!");
		if (commandPrefix != null)
			registeredPrefixedCommands.add(command);
	}

	/**
	 * @return A {@link List} of all registered non-prefixed commands.
	 */
	public List<SwornPermissionsCommand> getRegisteredCommands()
	{
		return registeredCommands;
	}

	/**
	 * @return A {@link List} of all registered prefixed commands.
	 */
	public List<SwornPermissionsCommand> getRegisteredPrefixedCommands()
	{
		return registeredPrefixedCommands;
	}

	/**
	 * @return The command prefix.
	 */
	public String getCommandPrefix()
	{
		return commandPrefix;
	}

	/**
	 * Sets the command prefix. This method must be called before any prefixed
	 * commands are registered.
	 *
	 * @param commandPrefix SwornPermissionsCommand prefix
	 */
	public void setCommandPrefix(String commandPrefix)
	{
		Validate.notEmpty(commandPrefix, "prefix cannot be null or empty!");
		this.commandPrefix = commandPrefix;
		this.registeredPrefixedCommands = new ArrayList<SwornPermissionsCommand>();

		plugin.getCommand(commandPrefix).setExecutor(this);
	}

	/**
	 * @return whether or not the command prefix is used.
	 */
	public boolean usesCommandPrefix()
	{
		return commandPrefix != null;
	}

	public final SwornPermissionsCommand getCommand(String name)
	{
		Validate.notNull(name, "name cannot be null!");
		for (SwornPermissionsCommand command : registeredPrefixedCommands)
		{
			if (name.equalsIgnoreCase(command.getName()) || command.getAliases().contains(name.toLowerCase()))
				return command;
		}

		for (SwornPermissionsCommand command : registeredCommands)
		{
			if (name.equalsIgnoreCase(command.getName()) || command.getAliases().contains(name.toLowerCase()))
				return command;
		}

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean onCommand(CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args)
	{
		if (args.length > 0)
		{
			String name = args[0];
			args = Arrays.copyOfRange(args, 1, args.length);

			SwornPermissionsCommand command = getCommand(name);
			if (command != null)
			{
				command.execute(sender, args);
				return true;
			}

			new ComponentBuilder(FormatUtil.format("&cError: &4Unknown command \"&c{0}&4\". Try ", name))
				.addAll(getHelpCommand().getFancyUsageTemplate()).send(sender);
		}
		else
		{
			getHelpCommand().execute(sender, args);
		}

		return true;
	}

	private final SwornPermissionsCommand getHelpCommand()
	{
		return getCommand("help");
	}
}