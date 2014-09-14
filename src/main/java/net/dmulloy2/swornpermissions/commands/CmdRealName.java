/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.swornpermissions.commands;

import net.dmulloy2.swornpermissions.SwornPermissions;
import net.dmulloy2.swornpermissions.types.Permission;
import net.dmulloy2.swornpermissions.types.User;

/**
 * @author dmulloy2
 */

public class CmdRealName extends SwornPermissionsCommand
{
	public CmdRealName(SwornPermissions plugin)
	{
		super(plugin);
		this.name = "realname";
		this.requiredArgs.add("player");
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