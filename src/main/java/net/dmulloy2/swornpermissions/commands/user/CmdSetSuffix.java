/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.swornpermissions.commands.user;

import net.dmulloy2.swornpermissions.SwornPermissions;
import net.dmulloy2.swornpermissions.types.Permission;
import net.dmulloy2.util.FormatUtil;

/**
 * @author dmulloy2
 */

public class CmdSetSuffix extends UserCommand
{
	public CmdSetSuffix(SwornPermissions plugin)
	{
		super(plugin);
		this.action = "set";
		this.name = "suffix";
		this.requiredArgs.add("suffix");
		this.description = "Set a user's suffix";
		this.permission = Permission.USER_SET_SUFFIX;
	}

	@Override
	public void perform()
	{
		String suffix = FormatUtil.join(" ", args);
		suffix = suffix.replaceAll("\"", "");

		if (suffix.equalsIgnoreCase("null") || suffix.equalsIgnoreCase("remove"))
		{
			user.resetPrefix();
			sendpMessage("&b{0} &esuffix has been reset.", user.describeTo(sender, true));
			return;
		}

		user.setSuffix(suffix);

		sendpMessage("&eb{0} &esuffix is now \"&r{0}&e\"", user.describeTo(sender, true), suffix);
	}
}