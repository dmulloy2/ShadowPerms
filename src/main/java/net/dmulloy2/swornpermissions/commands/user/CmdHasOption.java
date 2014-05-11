/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.swornpermissions.commands.user;

import net.dmulloy2.swornpermissions.SwornPermissions;
import net.dmulloy2.swornpermissions.types.Permission;

/**
 * @author dmulloy2
 */

public class CmdHasOption extends UserCommand
{
	public CmdHasOption(SwornPermissions plugin)
	{
		super(plugin);
		this.action = "has";
		this.name = "option";
		this.requiredArgs.add("option");
		this.description = "Check if a user has an option";
		this.permission = Permission.USER_HAS_OPTION;
	}

	@Override
	public void perform()
	{
		String key = args[0];
		if (user.hasOption(key))
		{
			sendpMessage("User {0} has option ''{1}''", user.getName(), key);
			sendpMessage("Value: {0}", user.getOption(key));
		}
		else
		{
			sendpMessage("User {0} does not have option ''{1}''", user.getName(), key);
		}
	}
}