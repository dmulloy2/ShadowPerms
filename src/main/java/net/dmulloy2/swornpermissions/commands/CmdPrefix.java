package net.dmulloy2.swornpermissions.commands;

import net.dmulloy2.swornpermissions.SwornPermissions;
import net.dmulloy2.swornpermissions.permissions.User;
import net.dmulloy2.swornpermissions.types.Permission;
import net.dmulloy2.swornpermissions.util.FormatUtil;

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

		String argsCheck = prefix.replaceAll("(?i)&([a-f0-9])", "").replaceAll("&", "").replaceAll("\\[", "").replaceAll("\\]", "");

		// Perform and enforce args check
		int maxLength = plugin.getConfig().getInt("prefix.maxLength");
		if (argsCheck.length() > maxLength)
		{
			err("Your prefix is too long! (Max &c{0} &4characters)", maxLength);
			return;
		}

		if (plugin.getConfig().getBoolean("prefix.forceSpace"))
			prefix = prefix + " ";

		user.setPrefix(prefix);

		sendpMessage("&eYour prefix is now \"{0}&e\"", plugin.getChatHandler().replaceColors(prefix));
	}
}