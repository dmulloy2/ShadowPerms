/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.shadowperms.commands;

import net.dmulloy2.shadowperms.ShadowPerms;
import net.dmulloy2.shadowperms.types.Permission;
import net.dmulloy2.shadowperms.types.User;

/**
 * @author dmulloy2
 */

public class CmdRealName extends ShadowPermsCommand
{
	public CmdRealName(ShadowPerms plugin)
	{
		super(plugin);
		this.name = "realname";
		this.addRequiredArg("player");
		this.description = "Get a player''s real name";
		this.permission = Permission.CMD_REALNAME;
	}

	@Override
	public void perform()
	{
		User user = getUser(0);
		if (user == null)
			return;

		sendpMessage("&r{0} &eis &b{1}&e.", user.getDisplayName(), user.getName());
	}
}