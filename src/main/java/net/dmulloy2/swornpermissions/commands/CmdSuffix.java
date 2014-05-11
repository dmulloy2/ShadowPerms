package net.dmulloy2.swornpermissions.commands;

import net.dmulloy2.swornpermissions.SwornPermissions;
import net.dmulloy2.swornpermissions.permissions.User;
import net.dmulloy2.swornpermissions.types.Permission;
import net.dmulloy2.swornpermissions.util.FormatUtil;

/**
 * @author dmulloy2
 */

public class CmdSuffix extends SwornPermissionsCommand
{
	public CmdSuffix(SwornPermissions plugin)
	{
		super(plugin);
		this.name = "suffix";
		this.aliases.add("suf");
		this.requiredArgs.add("suffix");
		this.description = "Change your suffix";
		this.permission = Permission.CMD_SUFFIX;
		this.mustBePlayer = true;
	}

	@Override
	public void perform()
	{
		User user = getUser(true);
		if (user == null)
			return;

		String suffix = FormatUtil.join(" ", args);
		suffix = suffix.replaceAll("\"", "");

		String argsCheck = suffix.replaceAll("(?i)&([a-f0-9])", "").replaceAll("&", "").replaceAll("\\[", "").replaceAll("\\]", "");

		// Perform and enforce args check
		int maxLength = plugin.getConfig().getInt("suffix.maxLength");
		if (argsCheck.length() > maxLength)
		{
			err("Your suffix is too long! (Max &c{0} &4characters)", maxLength);
			return;
		}

		if (plugin.getConfig().getBoolean("suffix.forceSpace"))
			suffix = suffix + " ";

		user.setSuffix(suffix);

		sendpMessage("&eYour suffix is now \"{0}&e\"", plugin.getChatHandler().replaceColors(suffix));
	}
}