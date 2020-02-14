/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.shadowperms.commands.user;

import java.util.List;

import net.dmulloy2.shadowperms.ShadowPerms;
import net.dmulloy2.shadowperms.commands.ShadowPermsCommand;
import net.dmulloy2.shadowperms.types.Permission;
import net.dmulloy2.shadowperms.types.User;
import net.dmulloy2.types.StringJoiner;

/**
 * @author dmulloy2
 */

public class CmdListPermissions extends ShadowPermsCommand
{
	public CmdListPermissions(ShadowPerms plugin)
	{
		super(plugin);
		this.name = "perms";
		this.addRequiredArg("user");
		this.description = "List a user''s permissions";
		this.permission = Permission.USER_LIST_PERMISSIONS;
	}

	@Override
	public void perform()
	{
		User user = getUser(0);
		sendMessage("&3---- &e{0} &3----", user.getName());

		List<String> permissions = user.getPermissionNodes();
		if (! permissions.isEmpty())
		{
			sendMessage("&bPermissions&e: {0}", new StringJoiner("&b, &e").appendAll(permissions));
		}
		else
		{
			sendMessage("No user-specific permissions.");
		}

		StringJoiner joiner = new StringJoiner("&b, &e");
		joiner.append(user.getGroupName());
		joiner.appendAll(user.getSubGroupNames());

		sendMessage("&bAnd all from&e: {0}", joiner.toString());
	}
}