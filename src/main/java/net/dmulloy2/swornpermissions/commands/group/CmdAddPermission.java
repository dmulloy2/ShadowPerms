/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.swornpermissions.commands.group;

import net.dmulloy2.swornpermissions.SwornPermissions;
import net.dmulloy2.swornpermissions.types.Permission;

/**
 * @author dmulloy2
 */

public class CmdAddPermission extends GroupCommand
{
	public CmdAddPermission(SwornPermissions plugin)
	{
		super(plugin);
		this.action = "add";
		this.name = "permission";
		this.aliases.add("perm");
		this.requiredArgs.add("permission");
		this.description = "Give a group a permission";
		this.permission = Permission.GROUP_ADD_PERMISSION;
	}

	@Override
	public void perform()
	{
		String permission = args[0];
		if (group.hasPermission(permission))
		{
			sendpMessage("Group &b{0} &ealready has this permission.", group.getName());
			sendpMessage("Node: &b{0}", group.getMatchingPermission(permission));
			return;
		}

		group.addPermission(permission);
		group.update();

		sendpMessage("Permission ''&b{0}&e'' added to group &b{1}&e''s permissions.", permission, group.getName());
	}
}