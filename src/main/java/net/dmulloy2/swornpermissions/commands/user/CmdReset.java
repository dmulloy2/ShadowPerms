/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.swornpermissions.commands.user;

import net.dmulloy2.swornpermissions.SwornPermissions;
import net.dmulloy2.swornpermissions.types.Permission;

/**
 * @author dmulloy2
 */

public class CmdReset extends UserCommand
{
	public CmdReset(SwornPermissions plugin)
	{
		super(plugin);
		this.name = "reset";
		this.description = "Reset a user's data";
		this.permission = Permission.USER_RESET;
	}

	@Override
	public void perform()
	{
		user.reset();

		sendpMessage("You have reset &b{0} &eto default settings.", user.getName());
	}
}