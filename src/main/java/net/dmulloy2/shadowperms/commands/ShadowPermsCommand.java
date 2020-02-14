/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.shadowperms.commands;

import java.util.logging.Level;

import net.dmulloy2.commands.Command;
import net.dmulloy2.shadowperms.ShadowPerms;
import net.dmulloy2.shadowperms.types.Group;
import net.dmulloy2.shadowperms.types.Permission;
import net.dmulloy2.shadowperms.types.User;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;

/**
 * @author dmulloy2
 */

public abstract class ShadowPermsCommand extends Command
{
	protected ShadowPerms plugin;

	public ShadowPermsCommand(ShadowPerms plugin)
	{
		super(plugin);
		this.usesPrefix = true;
	}

	@Override
	public void prePerform()
	{
		if (sender instanceof BlockCommandSender && ! plugin.getConfig().getBoolean("allowCommandBlocks"))
		{
			Block block = ((BlockCommandSender) sender).getBlock();
			plugin.getLogHandler().log(Level.WARNING, "ShadowPerms commands cannot be used from command blocks!");
			plugin.getLogHandler().log(Level.WARNING, "Location: {0}, {1}, {2} ({3})", block.getX(), block.getY(), block.getZ(),
					block.getWorld().getName());
			stopExecution();
		}
	}

	// ---- Permission Management

	protected final boolean hasPermission(CommandSender sender, Permission permission)
	{
		return plugin.getPermissionHandler().hasPermission(sender, permission);
	}

	protected final boolean hasPermission(Permission permission)
	{
		return hasPermission(sender, permission);
	}

	protected final boolean hasPermission()
	{
		return hasPermission(permission);
	}

	protected final User getUser(int index)
	{
		User user = plugin.getPermissionHandler().getUser(getWorld().getName(), args[index]);
		checkNotNull(user,"User \"&c{0}&4\" not found!", args[index]);
		return user;
	}

	protected final Group getGroup(int index)
	{
		Group group = plugin.getPermissionHandler().getGroup(getWorld().getName(), args[index]);
		checkNotNull(group,"Group \"&c{0}&4\" not found!", args[index]);
		return group;
	}

	public final World getWorld()
	{
		try
		{
			World world = plugin.getServer().getWorld(args[args.length - 1]);
			if (world != null)
				return world;
		} catch (Throwable ignored) { }

		if (player != null)
			return player.getWorld();

		return getDefaultWorld();
	}

	protected final World getDefaultWorld()
	{
		return plugin.getPermissionHandler().getDefaultWorld();
	}

	protected final boolean hasArgument(String string)
	{
		for (String arg : args)
		{
			if (arg.equalsIgnoreCase(string))
				return true;
		}

		return false;
	}
}