/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.swornpermissions.commands.user;

import net.dmulloy2.swornpermissions.SwornPermissions;
import net.dmulloy2.swornpermissions.types.Permission;

/**
 * @author dmulloy2
 */

public class CmdRemoveSubgroup extends UserCommand
{
	public CmdRemoveSubgroup(SwornPermissions plugin)
	{
		super(plugin);
		this.action = "remove";
		this.name = "subgroup";
		this.aliases.add("sub");
		this.requiredArgs.add("group");
		this.description = "Remove a subgroup from a user";
		this.permission = Permission.USER_REMOVE_SUBGROUP;
	}

	@Override
	public void perform()
	{
		String group = args[0];
		if (! user.isInSubGroup(group))
		{
			sendpMessage("User &b{0} &eis not in sub group &b{1}&e.", user.getName(), group);
			return;
		}

		user.removeSubGroup(group);
		user.updatePermissions(true);

		sendpMessage("Sub group &b{0} &eremoved from user &b{1}&e''s permissions.", group, user.getName());
	}
}