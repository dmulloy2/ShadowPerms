/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.swornpermissions.commands.user;

import net.dmulloy2.swornpermissions.SwornPermissions;
import net.dmulloy2.swornpermissions.types.Permission;

/**
 * @author dmulloy2
 */

public class CmdAddPermission extends UserCommand
{
	public CmdAddPermission(SwornPermissions plugin)
	{
		super(plugin);
		this.action = "add";
		this.name = "permission";
		this.aliases.add("perm");
		this.requiredArgs.add("permission");
		this.description = "Give a user a permission";
		this.permission = Permission.USER_ADD_PERMISSION;
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

		if (user.hasPermission(permission) && ! negative)
		{
			sendpMessage("User &b{0} &ealready has access to this permission.", user.getName());
			String matchingPerm = user.getMatchingPermission(permission);
			if (matchingPerm != null)
				sendpMessage("Node: &b{0}", user.getMatchingPermission(permission));
			return;
		}

		if (user.hasPermissionNode(node))
		{
			sendpMessage("User &b{0} &ealready has this node.", user.getName());
			return;
		}

		user.addPermission(node);
		user.updatePermissions(true);

		sendpMessage("Permission ''&b{0}&e'' added to user &b{1}&e''s permissions.", node, user.getName());
	}
}