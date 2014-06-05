/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.swornpermissions.commands.user;

import net.dmulloy2.swornpermissions.SwornPermissions;
import net.dmulloy2.swornpermissions.types.Permission;

/**
 * @author dmulloy2
 */

public class CmdRemoveTemp extends UserCommand
{
	public CmdRemoveTemp(SwornPermissions plugin)
	{
		super(plugin);
		this.action = "remove";
		this.name = "temp";
		this.requiredArgs.add("permission");
		this.description = "Removed a temp permission";
		this.permission = Permission.USER_REMOVE_PERMISSION;
	}

	@Override
	public void perform()
	{
		String permission = args[0];
		if (! user.hasTempPermission(permission))
		{
			sendpMessage("User &b{0} &edoes not have temp permission ''&b{1}&e.''", user.getName(), permission);
			return;
		}

		user.removeTempPermission(permission);
		user.updatePermissions(true);

		sendpMessage("Temp permission ''&b{0}&e'' removed from user &b{1}&e''s permissions.", permission, user.getName());
	}
}