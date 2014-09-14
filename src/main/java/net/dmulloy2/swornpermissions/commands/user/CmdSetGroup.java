/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.swornpermissions.commands.user;

import net.dmulloy2.swornpermissions.SwornPermissions;
import net.dmulloy2.swornpermissions.types.Group;
import net.dmulloy2.swornpermissions.types.Permission;
import net.dmulloy2.swornpermissions.types.ServerGroup;

/**
 * @author dmulloy2
 */

public class CmdSetGroup extends UserCommand
{
	public CmdSetGroup(SwornPermissions plugin)
	{
		super(plugin);
		this.action = "set";
		this.name = "group";
		this.requiredArgs.add("group");
		this.description = "Set a user's group";
		this.permission = Permission.USER_SET_GROUP;
	}

	@Override
	public void perform()
	{
		Group group = getGroup(0, world, true);
		if (group == null)
			return;

		if (group instanceof ServerGroup)
		{
			err("A user''s primary group cannot be a Server Group!");
			return;
		}

		user.setGroup(group);
		user.updatePermissions(true);

		sendpMessage("User &b{0} &emoved to group &b{1} &ein world &b{2}&e.", user.getName(), group.getName(), world.getName());
	}
}