package net.dmulloy2.swornpermissions.commands;

import net.dmulloy2.swornpermissions.SwornPermissions;
import net.dmulloy2.swornpermissions.permissions.User;
import net.dmulloy2.swornpermissions.types.Permission;
import net.dmulloy2.swornpermissions.util.FormatUtil;

import org.bukkit.ChatColor;

/**
 * @author dmulloy2
 */

public class CmdPrefix extends SwornPermissionsCommand
{
	public CmdPrefix(SwornPermissions plugin)
	{
		super(plugin);
		this.name = "prefix";
		this.aliases.add("pre");
		this.requiredArgs.add("prefix");
		this.description = "Change your prefix";
		this.permission = Permission.CMD_PREFIX;
		this.mustBePlayer = true;
	}

	@Override
	public void perform()
	{
		User user = getUser(true);
		if (user == null)
			return;

		String prefix = FormatUtil.join(" ", args);
		prefix = prefix.replaceAll("\"", "");

		String argsCheck = ChatColor.stripColor(FormatUtil.replaceColors(prefix));
		argsCheck = argsCheck.replaceAll("\\[", "").replaceAll("\\]", "");

		// Perform and enforce args check
		int maxLength = plugin.getConfig().getInt("prefix.maxLength");
		if (argsCheck.length() > maxLength)
		{
			err("Your prefix is too long! (Max &c{0} &4characters)", maxLength);
			return;
		}

		// Trim the string
		prefix = prefix.trim();

		if (prefix.isEmpty())
		{
			sendpMessage("&eYour prefix has been reset!");
			user.resetPrefix();
			return;
		}

		// Apply configuration settings
		if (plugin.getConfig().getBoolean("prefix.forceReset"))
			prefix = prefix + "&r";
		if (plugin.getConfig().getBoolean("prefix.forceSpace"))
			prefix = prefix + " ";

		user.setPrefix(prefix);

		sendpMessage("&eYour prefix is now \"{0}&e\"", FormatUtil.replaceColors(prefix));
	}
}