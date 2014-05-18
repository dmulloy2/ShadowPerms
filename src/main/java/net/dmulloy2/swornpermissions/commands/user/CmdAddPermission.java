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
		String permission = args[0];
		if (user.hasPermission(permission))
		{
			sendpMessage("User &b{0} &ealready has this permission.", user.getName());
			sendpMessage("Node: &b{0}", user.getMatchingPermission(permission));
			return;
		}

		user.addPermission(permission);
		user.updatePermissions(true);

		sendpMessage("Permission ''{0}'' added to user {1}''s permissions.", permission, user.getName());
	}
}