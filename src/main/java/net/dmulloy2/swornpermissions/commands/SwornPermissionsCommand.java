/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.swornpermissions.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import net.dmulloy2.swornpermissions.SwornPermissions;
import net.dmulloy2.swornpermissions.permissions.Group;
import net.dmulloy2.swornpermissions.permissions.User;
import net.dmulloy2.swornpermissions.types.Permission;
import net.dmulloy2.swornpermissions.util.FormatUtil;
import net.dmulloy2.swornpermissions.util.Util;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author dmulloy2
 */

public abstract class SwornPermissionsCommand implements CommandExecutor
{
	protected final SwornPermissions plugin;

	protected CommandSender sender;
	protected Player player;
	protected String args[];

	protected String name;
	protected String description;

	protected Permission permission;

	protected boolean mustBePlayer;

	protected List<String> requiredArgs;
	protected List<String> optionalArgs;
	protected List<String> aliases;

	protected boolean usesPrefix;

	protected boolean hasSubCommands;

	public SwornPermissionsCommand(SwornPermissions plugin)
	{
		this.plugin = plugin;
		this.requiredArgs = new ArrayList<String>(2);
		this.optionalArgs = new ArrayList<String>(2);
		this.aliases = new ArrayList<String>(2);
	}

	@Override
	public final boolean onCommand(CommandSender sender, Command command, String label, String[] args)
	{
		execute(sender, args);
		return true;
	}

	public void execute(CommandSender sender, String[] args)
	{
		this.sender = sender;
		this.args = args;

		// Prevent commands being run on command blocks, if applicable
		if (sender instanceof BlockCommandSender && ! plugin.getConfig().getBoolean("allowCommandBlocks"))
		{
			Block block = ((BlockCommandSender) sender).getBlock();
			plugin.getLogHandler().log(Level.WARNING, "SwornPermissions commands cannot be used from command blocks!");
			plugin.getLogHandler().log(Level.WARNING, "Location: {0}, {1}, {2} ({3})", block.getX(), block.getY(), block.getZ(),
					block.getWorld().getName());
			return;
		}

		if (sender instanceof Player)
			player = (Player) sender;

		if (mustBePlayer && ! isPlayer())
		{
			err("You must be a player to execute this command!");
			return;
		}

		if (requiredArgs.size() > args.length)
		{
			invalidArgs();
			return;
		}

		if (! hasPermission())
		{
			err("You do not have permission to perform this command!");
			plugin.getLogHandler().log(Level.WARNING, sender.getName() + " was denied access to a command!");
			return;
		}

		try
		{
			perform();
		}
		catch (Throwable e)
		{
			err("Error executing command: &c{0}&4: &c{1}", e.getClass().getName(), e.getLocalizedMessage());
			plugin.getLogHandler().debug(Util.getUsefulStack(e, "executing command " + name));
		}

		// ---- Clear Variables
		this.sender = null;
		this.args = null;
		this.player = null;
	}

	public abstract void perform();

	protected final boolean isPlayer()
	{
		return player != null;
	}

	protected final boolean hasPermission()
	{
		return hasPermission(permission);
	}

	protected final boolean hasPermission(Permission permission)
	{
		return plugin.getPermissionHandler().hasPermission(sender, permission);
	}

	public final Permission getPermission()
	{
		return permission;
	}

	public final String getDescription()
	{
		return FormatUtil.format(description);
	}

	public final List<String> getAliases()
	{
		return aliases;
	}

	public final String getName()
	{
		return name;
	}

	public final boolean hasSubCommands()
	{
		return hasSubCommands;
	}

	// Sub Command Help - This is required if hasSubCommands is true
	public List<String> getSubCommandHelp(CommandSender sender)
	{
		return null;
	}

	public String getUsageTemplate(boolean displayHelp)
	{
		StringBuilder ret = new StringBuilder();

		if (usesPrefix)
			ret.append("&b/" + plugin.getCommandHandler().getCommandPrefix() + " ");

		ret.append(name);

		ret.append("&3 ");
		for (String s : requiredArgs)
			ret.append(String.format("<%s> ", s));

		for (String s : optionalArgs)
			ret.append(String.format("[%s] ", s));

		if (displayHelp)
			ret.append("&e" + description);

		return FormatUtil.format(ret.toString());
	}

	protected final void sendpMessage(String message, Object... objects)
	{
		sendMessage(plugin.getPrefix() + message, objects);
	}

	protected final void sendMessage(String message, Object... objects)
	{
		sender.sendMessage(FormatUtil.format("&e" + message, objects));
	}

	protected final void sendMessage(Player player, String message, Object... objects)
	{
		player.sendMessage(FormatUtil.format(message, objects));
	}

	protected final void sendpMessage(Player player, String message, Object... objects)
	{
		sendMessage(player, plugin.getPrefix() + message, objects);
	}

	protected final void err(String string, Object... objects)
	{
		sendMessage("&cError: &4" + string, objects);
	}

	protected void invalidArgs()
	{
		err("Invalid arguments! Try: " + getUsageTemplate(false));
	}

	protected final User getUser(boolean msg)
	{
		if (! isPlayer())
		{
			if (msg)
				err("You must be a player to do this!");
			return null;
		}

		return plugin.getPermissionHandler().getUser(player);
	}

	protected final User getUser(int arg, World world, boolean msg)
	{
		if (args.length >= arg)
			return getUser(args[arg], world, msg);

		if (msg)
			err("User not specified!");
		return null;
	}

	protected final User getUser(String name, World world, boolean msg)
	{
		User user = plugin.getPermissionHandler().getUser(world.getName(), name);
		if (user == null && msg)
			err("User \"&c{0}&4\" not found!", name);

		return user;
	}

	protected final Group getGroup(int arg, World world, boolean msg)
	{
		if (args.length > arg)
			return getGroup(args[arg], world, msg);

		if (msg)
			err("Group not specified!");
		return null;
	}

	protected final Group getGroup(String name, World world, boolean msg)
	{
		Group group = plugin.getPermissionHandler().getGroup(world.getName(), name);
		if (group == null && msg)
			err("Group \"&c{0}&4\" not found!", name);

		return group;
	}

	public final World getWorld()
	{
		try
		{
			World world = plugin.getServer().getWorld(args[args.length]);
			if (world != null)
				return world;
		} catch (Throwable ex) { }
		return getDefaultWorld();
	}

	protected final World getDefaultWorld()
	{
		return plugin.getPermissionHandler().getDefaultWorld();
	}
}