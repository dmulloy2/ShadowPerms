/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.swornpermissions.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import net.dmulloy2.chat.BaseComponent;
import net.dmulloy2.chat.ChatUtil;
import net.dmulloy2.chat.ClickEvent;
import net.dmulloy2.chat.ComponentBuilder;
import net.dmulloy2.chat.HoverEvent;
import net.dmulloy2.chat.HoverEvent.Action;
import net.dmulloy2.chat.TextComponent;
import net.dmulloy2.swornpermissions.SwornPermissions;
import net.dmulloy2.swornpermissions.types.Group;
import net.dmulloy2.swornpermissions.types.Permission;
import net.dmulloy2.swornpermissions.types.User;
import net.dmulloy2.types.CommandVisibility;
import net.dmulloy2.types.StringJoiner;
import net.dmulloy2.util.FormatUtil;
import net.dmulloy2.util.ListUtil;
import net.dmulloy2.util.NumberUtil;
import net.dmulloy2.util.Util;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

/**
 * @author dmulloy2
 */

public abstract class SwornPermissionsCommand implements CommandExecutor
{
	protected SwornPermissions plugin;

	protected CommandSender sender;
	protected Player player;
	protected String args[];

	protected String name;
	protected String description;

	protected Permission permission;
	protected CommandVisibility visibility = CommandVisibility.PERMISSION;

	protected List<String> requiredArgs;
	protected List<String> optionalArgs;
	protected List<String> aliases;

	protected boolean hasSubCommands;
	protected boolean mustBePlayer;
	protected boolean usesPrefix;

	public SwornPermissionsCommand(SwornPermissions plugin)
	{
		this.plugin = plugin;
		this.requiredArgs = new ArrayList<String>(2);
		this.optionalArgs = new ArrayList<String>(2);
		this.aliases = new ArrayList<String>(2);
	}

	// ---- Execution

	@Override
	public final boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args)
	{
		execute(sender, args);
		return true;
	}

	public final void execute(CommandSender sender, String[] args)
	{
		this.sender = sender;
		this.args = args;
		if (sender instanceof Player)
			player = (Player) sender;

		if (mustBePlayer && ! isPlayer())
		{
			err("You must be a player to perform this command!");
			return;
		}

		if (requiredArgs.size() > args.length)
		{
			invalidArgs();
			return;
		}

		if (! isVisibleTo(sender))
		{
			if (visibility == CommandVisibility.PERMISSION)
			{
				StringJoiner hoverText = new StringJoiner("\n");
				hoverText.append(FormatUtil.format("&4Permission:\n"));
				hoverText.append(getPermissionString());

				ComponentBuilder builder = new ComponentBuilder(FormatUtil.format("&cError: &4You do not have "));
				builder.append(FormatUtil.format("&cpermission")).event(new HoverEvent(Action.SHOW_TEXT, hoverText.toString()));
				builder.append(FormatUtil.format(" &4to perform this command!"));
				sendMessage(builder.create());
				return;
			}
			else
			{
				err("You cannot use this command!");
				return;
			}
		}

		try
		{
			perform();
		}
		catch (Throwable ex)
		{
			String stack = Util.getUsefulStack(ex, "executing command " + name);
			plugin.getLogHandler().log(Level.WARNING, stack);

			String error = FormatUtil.format("&cError: &4Encountered an exception executing this command: ");

			ComponentBuilder builder = new ComponentBuilder(error);
			builder.append(FormatUtil.format("&c{0}", ex.toString())).event(new HoverEvent(Action.SHOW_TEXT, stack));
			sendMessage(builder.create());
		}
	}

	public abstract void perform();

	protected final boolean isPlayer()
	{
		return sender instanceof Player;
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

	protected final String getPermissionString(Permission permission)
	{
		return plugin.getPermissionHandler().getPermissionString(permission);
	}

	public final String getPermissionString()
	{
		return getPermissionString(permission);
	}

	public final boolean isVisibleTo(CommandSender sender)
	{
		switch (visibility)
		{
			case ALL:
				return true;
			case NONE:
				return false;
			case OPS:
				return player.isOp();
			default:
				return hasPermission(sender, permission);
		}
	}

	// ---- Messaging

	protected final void err(String msg, Object... args)
	{
		sendMessage("&cError: &4" + FormatUtil.format(msg, args));
	}

	protected final void sendpMessage(String message, Object... objects)
	{
		sendMessage(plugin.getPrefix() + message, objects);
	}

	protected final void sendMessage(String message, Object... objects)
	{
		sender.sendMessage(ChatColor.YELLOW + FormatUtil.format(message, objects));
	}

	protected final void err(CommandSender sender, String msg, Object... args)
	{
		sendMessage(sender, "&cError: &4" + FormatUtil.format(msg, args));
	}

	protected final void sendpMessage(CommandSender sender, String message, Object... objects)
	{
		sendMessage(sender, plugin.getPrefix() + message, objects);
	}

	protected final void sendMessage(CommandSender sender, String message, Object... objects)
	{
		sender.sendMessage(ChatColor.YELLOW + FormatUtil.format(message, objects));
	}

	// ---- Fancy Messaging

	protected final void sendMessage(BaseComponent... components)
	{
		sendMessage(sender, components);
	}

	protected final void sendMessage(CommandSender sender, BaseComponent... components)
	{
		ChatUtil.sendMessage(player, components);
	}

	// ---- Help

	public final String getName()
	{
		return name;
	}

	public final List<String> getAliases()
	{
		return aliases;
	}

	public String getUsageTemplate(boolean displayHelp)
	{
		StringBuilder ret = new StringBuilder();
		ret.append("&b/");

		if (plugin.getCommandHandler().usesCommandPrefix() && usesPrefix)
			ret.append(plugin.getCommandHandler().getCommandPrefix() + " ");

		ret.append(name);

		for (String s : optionalArgs)
			ret.append(String.format(" &3[%s]", s));

		for (String s : requiredArgs)
			ret.append(String.format(" &3<%s>", s));

		if (displayHelp)
			ret.append(" &e" + description);

		return FormatUtil.format(ret.toString());
	}

	public BaseComponent[] getFancyUsageTemplate()
	{
		return getFancyUsageTemplate(false);
	}

	public BaseComponent[] getFancyUsageTemplate(boolean list)
	{
		String prefix = list ? "- " : "";
		String usageTemplate = getUsageTemplate(false);

		ComponentBuilder builder = new ComponentBuilder(ChatColor.AQUA + prefix + usageTemplate);

		StringBuilder hoverTextBuilder = new StringBuilder();
		hoverTextBuilder.append(usageTemplate + ":\n");

		StringJoiner description = new StringJoiner("\n");
		for (String s : getDescription())
			description.append(ChatColor.YELLOW + s);
		hoverTextBuilder.append(FormatUtil.format(description.toString()));

		if (permission != null)
		{
			hoverTextBuilder.append("\n\n");
			hoverTextBuilder.append(ChatColor.DARK_RED + "Permission:");
			hoverTextBuilder.append("\n" + getPermissionString());
		}

		String hoverText = hoverTextBuilder.toString();

		HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(hoverText));
		builder.event(hoverEvent);

		ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, ChatColor.stripColor(usageTemplate));
		builder.event(clickEvent);

		return builder.create();
	}

	public List<String> getDescription()
	{
		return ListUtil.toList(description);
	}

	// ---- Sub Commands

	public final boolean hasSubCommands()
	{
		return hasSubCommands;
	}

	public List<? extends SwornPermissionsCommand> getSubCommands()
	{
		return null;
	}

	public List<String> getSubCommandHelp(boolean displayHelp)
	{
		List<String> ret = new ArrayList<>();

		for (SwornPermissionsCommand cmd : getSubCommands())
		{
			ret.add(cmd.getUsageTemplate(displayHelp));
		}

		return ret;
	}

	public List<BaseComponent[]> getFancySubCommandHelp()
	{
		return getFancySubCommandHelp(false);
	}

	public List<BaseComponent[]> getFancySubCommandHelp(boolean list)
	{
		List<BaseComponent[]> ret = new ArrayList<>();

		for (SwornPermissionsCommand cmd : getSubCommands())
		{
			ret.add(cmd.getFancyUsageTemplate(list));
		}

		return ret;
	}

	// ---- Argument Manipulation

	protected final boolean argMatchesAlias(String arg, String... aliases)
	{
		for (String s : aliases)
		{
			if (arg.equalsIgnoreCase(s))
				return true;
		}

		return false;
	}

	protected final int argAsInt(int arg, boolean msg)
	{
		int ret = -1;
		if (args.length >= arg)
			ret = NumberUtil.toInt(args[arg]);

		if (msg && ret == -1)
			err("&c{0} &4is not a number.", args[arg]);

		return ret;
	}

	protected final double argAsDouble(int arg, boolean msg)
	{
		double ret = -1.0D;
		if (args.length >= arg)
			ret = NumberUtil.toDouble(args[arg]);

		if (msg && ret == -1.0D)
			err("&c{0} &4is not a number.", args[arg]);

		return ret;
	}

	protected final boolean argAsBoolean(int arg)
	{
		return Util.toBoolean(args[arg]);
	}

	protected final String getFinalArg(int start)
	{
		StringBuilder ret = new StringBuilder();
		for (int i = start; i < args.length; i++)
		{
			if (i != start)
				ret.append(" ");

			ret.append(args[i]);
		}

		return ret.toString();
	}

	// ---- Utility

	protected final void invalidArgs()
	{
		err("Invalid arguments! Try: {0}", getUsageTemplate(false));
	}

	protected final String getName(CommandSender sender)
	{
		if (sender instanceof BlockCommandSender)
		{
			BlockCommandSender commandBlock = (BlockCommandSender) sender;
			Location location = commandBlock.getBlock().getLocation();
			return FormatUtil.format("CommandBlock ({0}, {1}, {2})", location.getBlockX(), location.getBlockY(), location.getBlockZ());
		}
		else if (sender instanceof ConsoleCommandSender)
		{
			return "Console";
		}
		else
		{
			return sender.getName();
		}
	}

	protected final User getUser()
	{
		return getUser(true);
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

	protected final User getUser(int index)
	{
		return getUser(index, getWorld());
	}

	protected final User getUser(int index, World world)
	{
		return getUser(index, world, true);
	}

	protected final User getUser(int index, World world, boolean msg)
	{
		if (args.length > index)
			return getUser(args[index], world, msg);

		if (msg)
			err("User not specified!");
		return null;
	}

	protected final User getUser(String identifier, World world, boolean msg)
	{
		User user = plugin.getPermissionHandler().getUser(world.getName(), identifier);
		if (user == null && msg)
			err("User \"&c{0}&4\" not found!", identifier);

		return user;
	}

	protected final User getUser(String identifier, World world)
	{
		return getUser(identifier, world, true);
	}

	protected final Group getGroup(int arg, World world, boolean msg)
	{
		if (args.length > arg)
			return getGroup(args[arg], world, msg);

		if (msg)
			err("Group not specified!");
		return null;
	}

	protected final Group getGroup(int arg, World world)
	{
		return getGroup(arg, world, true);
	}

	protected final Group getGroup(String name, World world, boolean msg)
	{
		Group group = plugin.getPermissionHandler().getGroup(world.getName(), name);
		if (group == null && msg)
			err("Group \"&c{0}&4\" not found!", name);

		return group;
	}

	protected final Group getGroup(String name, World world)
	{
		return getGroup(name, world, true);
	}

	public final World getWorld()
	{
		try
		{
			World world = plugin.getServer().getWorld(args[args.length - 1]);
			if (world != null)
				return world;
		}
		catch (Throwable ex)
		{
		}
		return getDefaultWorld();
	}

	protected final World getDefaultWorld()
	{
		return plugin.getPermissionHandler().getDefaultWorld();
	}
}