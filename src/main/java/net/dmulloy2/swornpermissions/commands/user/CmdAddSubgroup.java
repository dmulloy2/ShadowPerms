/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.swornpermissions.commands.user;

import net.dmulloy2.swornpermissions.SwornPermissions;
import net.dmulloy2.swornpermissions.permissions.Group;
import net.dmulloy2.swornpermissions.types.Permission;

/**
 * @author dmulloy2
 */

public class CmdAddSubgroup extends UserCommand
{
	public CmdAddSubgroup(SwornPermissions plugin)
	{
		super(plugin);
		this.action = "add";
		this.name = "subgroup";
		this.aliases.add("sub");
		this.requiredArgs.add("group");
		this.description = "Give a subgroup to a user";
		this.permission = Permission.USER_ADD_SUBGROUP;
	}

	@Override
	public void perform()
	{
		Group group = getGroup(0, world, true);
		if (group == null)
			return;

		if (user.isInGroup(group.getName()))
		{
			sendpMessage("Group &b{0} &eis already available to user {1}.", group.getName(), user.getName());
			return;
		}

		user.addSubGroup(group);
		user.updatePermissions(true);

		sendpMessage("Group &b{0} &eadded to user &b{1}&e''s permissions.", group.getName());
	}
}