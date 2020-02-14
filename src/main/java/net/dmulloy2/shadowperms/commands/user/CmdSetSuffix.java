/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.shadowperms.commands.user;

import net.dmulloy2.shadowperms.ShadowPerms;
import net.dmulloy2.shadowperms.commands.ShadowPermsCommand;
import net.dmulloy2.shadowperms.types.Permission;
import net.dmulloy2.shadowperms.types.User;
import net.dmulloy2.util.FormatUtil;

/**
 * @author dmulloy2
 */

public class CmdSetSuffix extends ShadowPermsCommand
{
	public CmdSetSuffix(ShadowPerms plugin)
	{
		super(plugin);
		this.name = "suffix";
		this.addRequiredArg("user");
		this.addRequiredArg("suffix");
		this.description = "Set a user's suffix";
		this.permission = Permission.USER_SET_SUFFIX;
	}

	@Override
	public void perform()
	{
		User user = getUser(0);
		String suffix = getFinalArg(1).replace("\"", "");

		if (suffix.equalsIgnoreCase("null") || suffix.equalsIgnoreCase("remove"))
		{
			user.resetPrefix();
			sendpMessage("&b{0} &esuffix has been reset.", user.describeTo(sender, true));
			return;
		}

		user.setSuffix(suffix);

		sendpMessage("&b{0} &esuffix is now \"&r{1}&e\"", user.describeTo(sender, true), suffix);
	}
}