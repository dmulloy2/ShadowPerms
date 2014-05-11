/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.swornpermissions.commands.group;

import net.dmulloy2.swornpermissions.SwornPermissions;
import net.dmulloy2.swornpermissions.types.Permission;

/**
 * @author dmulloy2
 */

public class CmdRemovePermission extends GroupCommand
{
	public CmdRemovePermission(SwornPermissions plugin)
	{
		super(plugin);
		this.action = "remove";
		this.name = "permission";
		this.aliases.add("perm");
		this.requiredArgs.add("permission");
		this.description = "Remove a permission from a group";
		this.permission = Permission.GROUP_REMOVE_PERMISSION;
	}

	@Override
	public void perform()
	{
		String permission = args[0];
		if (! group.hasPermission(permission))
		{
			sendpMessage("Group {0} does not have permission ''{1}.''", group.getName(), permission);
			return;
		}

		group.removePermission(permission);
		group.update();

		sendpMessage("Permission ''{0}'' removed from group {1}''s permissions.", permission, group.getName());
	}
}