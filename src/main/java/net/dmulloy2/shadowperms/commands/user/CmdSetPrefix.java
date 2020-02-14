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

public class CmdSetPrefix extends ShadowPermsCommand
{
	public CmdSetPrefix(ShadowPerms plugin)
	{
		super(plugin);
		this.name = "prefix";
		this.addRequiredArg("user");
		this.addRequiredArg("prefix");
		this.description = "Set a user's prefix";
		this.permission = Permission.USER_SET_PREFIX;
	}

	@Override
	public void perform()
	{
		User user = getUser(0);
		String prefix = getFinalArg(1).replace("\"", "");

		if (prefix.equalsIgnoreCase("null") || prefix.equalsIgnoreCase("remove"))
		{
			user.resetPrefix();
			sendpMessage("&b{0} &eprefix has been reset.", user.describeTo(sender, true));
			return;
		}

		user.setPrefix(prefix);

		sendpMessage("&b{0} &eprefix is now \"&r{1}&e\"", user.describeTo(sender, true), prefix);
	}
}