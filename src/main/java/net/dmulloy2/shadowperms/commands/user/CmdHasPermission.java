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

public class CmdHasPermission extends ShadowPermsCommand
{
	public CmdHasPermission(ShadowPerms plugin)
	{
		super(plugin);
		this.name = "hasperm";
		this.addRequiredArg("user");
		this.addRequiredArg("permission");
		this.description = "Checks if a user has a permission";
		this.permission = Permission.USER_HAS_PERMISSION;
	}

	@Override
	public void perform()
	{
		User user = getUser(0);
		String permission = args[1];

		if (user.hasPermission(permission))
		{
			sendpMessage("User &b{0} &ehas access to permission &b{1}&e.", user.getName(), permission);

			String node = user.getMatchingPermission(permission);
			if (node != null && ! node.equalsIgnoreCase(permission))
			{
				sendpMessage("Matching node: &b{0}", node);
			}
		}
		else
		{
			sendpMessage("User &b{0} &edoes not have access to permission &b{1}&e.", user.getName(), permission);

			String node = user.getMatchingPermission(permission);
			if (node != null)
				sendpMessage("Negated, node: &b{0}", node);
		}
	}
}