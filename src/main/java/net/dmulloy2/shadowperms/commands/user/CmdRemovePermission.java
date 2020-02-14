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

public class CmdRemovePermission extends ShadowPermsCommand
{
	public CmdRemovePermission(ShadowPerms plugin)
	{
		super(plugin);
		this.name = "delperm";
		this.aliases.add("rmperm");
		this.addRequiredArg("user");
		this.addRequiredArg("permission");
		this.description = "Remove a permission from a user";
		this.permission = Permission.USER_REMOVE_PERMISSION;
	}

	@Override
	public void perform()
	{
		User user = getUser(0);
		String permission = args[1];

		if (! user.hasPermissionNode(permission))
		{
			sendpMessage("User &b{0} &edoes not directly have permission node ''&b{1}&e.''", user.getName(), permission);
			return;
		}

		user.removePermission(permission);
		user.updatePermissions(true);

		sendpMessage("Node ''&b{0}&e'' removed from user &b{1}&e''s permissions.", permission, user.getName());
	}
}