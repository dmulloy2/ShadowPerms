/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.swornpermissions.commands.group;

import net.dmulloy2.swornpermissions.SwornPermissions;
import net.dmulloy2.swornpermissions.types.Permission;

/**
 * @author dmulloy2
 */

public class CmdHasOption extends GroupCommand
{
	public CmdHasOption(SwornPermissions plugin)
	{
		super(plugin);
		this.action = "has";
		this.name = "option";
		this.requiredArgs.add("option");
		this.description = "Check if a group has an option";
		this.permission = Permission.GROUP_HAS_OPTION;
	}

	@Override
	public void perform()
	{
		String key = args[0];
		if (group.hasOption(key))
		{
			sendpMessage("Group {0} has option ''{1}''", group.getName(), key);
			sendpMessage("Value: {0}", group.getOption(key));
		}
		else
		{
			sendpMessage("Group {0} does not have option ''{1}''", group.getName(), key);
		}
	}
}