/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.swornpermissions.commands.user;

import net.dmulloy2.swornpermissions.SwornPermissions;
import net.dmulloy2.swornpermissions.types.Permission;

/**
 * @author dmulloy2
 */

public class CmdHasGroup extends UserCommand
{
	public CmdHasGroup(SwornPermissions plugin)
	{
		super(plugin);
		this.action = "has";
		this.name = "group";
		this.requiredArgs.add("group");
		this.description = "Checks if a user is in a group";
		this.permission = Permission.USER_HAS_GROUP;
	}

	@Override
	public void perform()
	{
		String group = args[0];
		if (user.isInGroup(group))
		{
			sendpMessage("User {0} is in group {1}.", user.getName(), group);
		}
		else if (user.isInSubGroup(group))
		{
			sendpMessage("User {0} is in sub group {1}.", user.getName(), group);
		}
		else
		{
			sendpMessage("User {0} is not in group {1}.", user.getName(), group);
		}
	}
}