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
		String node = args[0];
		if (node.contains("**"))
		{
			err("Permission \"&c{0}&4\" contains invalid characters: &cdouble star&4!", permission);
			return;
		}

		boolean negative = node.startsWith("-");
		String permission = negative ? node.substring(1) : node;

		if (group.hasPermission(permission) && ! negative)
		{
			sendpMessage("Group &b{0} &ealready has access to this permission.", group.getName());
			String matchingPerm = group.getMatchingPermission(permission);
			if (matchingPerm != null)
				sendpMessage("Node: &b{0}", group.getMatchingPermission(permission));
			return;
		}

		if (group.hasPermissionNode(node))
		{
			sendpMessage("Group &b{0} &ealready has this node.", group.getName());
			return;
		}

		group.addPermission(node);
		group.updatePermissions(true);

		sendpMessage("Permission ''&b{0}&e'' added to group &b{1}&e''s permissions.", permission, group.getName());
	}
}