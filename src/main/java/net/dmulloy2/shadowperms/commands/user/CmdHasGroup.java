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

public class CmdHasGroup extends ShadowPermsCommand
{
	public CmdHasGroup(ShadowPerms plugin)
	{
		super(plugin);
		this.name = "ingroup";
		this.aliases.add("hasgroup");
		this.addRequiredArg("user");
		this.addRequiredArg("group");
		this.description = "Checks if a user is in a group";
		this.permission = Permission.USER_HAS_GROUP;
	}

	@Override
	public void perform()
	{
		User user = getUser(0);
		String group = args[1];

		if (user.isInGroup(group))
		{
			sendpMessage("User &b{0} &eis in group &b{1}&e.", user.getName(), group);
		}
		else if (user.isInSubGroup(group))
		{
			sendpMessage("User &b{0} &ehas sub group {1}.", user.getName(), group);
		}
		else
		{
			sendpMessage("User &b{0} &eis not in group {1}.", user.getName(), group);
		}
	}
}