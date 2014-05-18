/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.swornpermissions.commands.user;

import net.dmulloy2.swornpermissions.SwornPermissions;
import net.dmulloy2.swornpermissions.types.Permission;

/**
 * @author dmulloy2
 */

public class CmdRemovePermission extends UserCommand
{
	public CmdRemovePermission(SwornPermissions plugin)
	{
		super(plugin);
		this.action = "remove";
		this.name = "permission";
		this.aliases.add("perm");
		this.requiredArgs.add("permission");
		this.description = "Remove a permission from a user";
		this.permission = Permission.USER_REMOVE_PERMISSION;
	}

	@Override
	public void perform()
	{
		String permission = args[0];
		if (! user.hasPermission(permission))
		{
			sendpMessage("User &b{0} &edoes not have permission ''&b{1}&e.''", user.getName(), permission);
			return;
		}

		user.removePermission(permission);
		user.updatePermissions(true);

		sendpMessage("Permission ''&b{0}&e'' removed from user &b{1}&e''s permissions.", permission, user.getName());
	}
}