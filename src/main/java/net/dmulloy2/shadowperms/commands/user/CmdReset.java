/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.shadowperms.commands.user;

import net.dmulloy2.shadowperms.ShadowPerms;
import net.dmulloy2.shadowperms.commands.ShadowPermsCommand;
import net.dmulloy2.shadowperms.types.Permission;
import net.dmulloy2.shadowperms.types.User;

/**
 * @author dmulloy2
 */

public class CmdReset extends ShadowPermsCommand
{
	public CmdReset(ShadowPerms plugin)
	{
		super(plugin);
		this.name = "reset";
		this.addRequiredArg("user");
		this.description = "Reset a user's data";
		this.permission = Permission.USER_RESET;
	}

	@Override
	public void perform()
	{
		User user = getUser(0);
		user.reset();

		sendpMessage("You have reset &b{0} &eto default settings.", user.getName());
	}
}