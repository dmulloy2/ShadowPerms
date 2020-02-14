/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.shadowperms.commands.user;

import net.dmulloy2.shadowperms.ShadowPerms;
import net.dmulloy2.shadowperms.commands.ShadowPermsCommand;
import net.dmulloy2.shadowperms.types.Permission;
import net.dmulloy2.shadowperms.types.User;

/**
 * @author dmulloy2
 */

public class CmdAddPermission extends ShadowPermsCommand
{
	public CmdAddPermission(ShadowPerms plugin)
	{
		super(plugin);
		this.name = "addperm";
		this.aliases.add("ap");
		this.aliases.add("addpermission");
		this.addRequiredArg("user");
		this.addRequiredArg("permission");
		this.description = "Give a user a permission";
		this.permission = Permission.USER_ADD_PERMISSION;
	}

	@Override
	public void perform()
	{
		User user = getUser(0);

		String node = args[1];
		checkArgument(!node.contains("**"), "Permission \"&c{0}&4\" contains invalid characters: &c**", node);

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