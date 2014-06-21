/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.swornpermissions.commands.group;

import java.util.logging.Level;

import lombok.Getter;
import net.dmulloy2.swornpermissions.SwornPermissions;
import net.dmulloy2.swornpermissions.commands.SwornPermissionsCommand;
import net.dmulloy2.swornpermissions.permissions.Group;
import net.dmulloy2.util.FormatUtil;
import net.dmulloy2.util.Util;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author dmulloy2
 */

public abstract class GroupCommand extends SwornPermissionsCommand
{
	protected Group group;
	protected World world;
	protected @Getter String action;

	public GroupCommand(SwornPermissions plugin)
	{
		super(plugin);
		this.requiredArgs.add("group");
	}

	public void execute(CommandSender sender, Group group, World world, String[] args)
	{
		this.sender = sender;
		this.group = group;
		this.world = world;
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

		if (requiredArgs.size() - 1 > args.length)
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
			plugin.getLogHandler().log(Level.WARNING, Util.getUsefulStack(e, "executing command " + name));
		}

		// ---- Clear Variables
		this.sender = null;
		this.args = null;
		this.player = null;
		this.group = null;
		this.world = null;
	}

	@Override
	public String getUsageTemplate(boolean displayHelp)
	{
		StringBuilder ret = new StringBuilder();
		ret.append(String.format("&b/%s &bgroup &3<group> &b%s %s", plugin.getCommandHandler().getCommandPrefix(), action, name));

		ret.append("&3 ");
		for (String s : requiredArgs.subList(1, requiredArgs.size()))
			ret.append(String.format("<%s> ", s));

		for (String s : optionalArgs)
			ret.append(String.format("[%s] ", s));

		if (displayHelp)
			ret.append("&e" + description);

		return FormatUtil.format(ret.toString());
	}
}