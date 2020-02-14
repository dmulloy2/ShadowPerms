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

public class CmdRemoveSubgroup extends ShadowPermsCommand
{
	public CmdRemoveSubgroup(ShadowPerms plugin)
	{
		super(plugin);
		this.name = "delsub";
		this.aliases.add("rmsub");
		this.addRequiredArg("user");
		this.addRequiredArg("group");
		this.description = "Remove a subgroup from a user";
		this.permission = Permission.USER_REMOVE_SUBGROUP;
	}

	@Override
	public void perform()
	{
		User user = getUser(0);
		String group = args[1];

		if (! user.isInSubGroup(group))
		{
			sendpMessage("User &b{0} &eis not in sub group &b{1}&e.", user.getName(), group);
			return;
		}

		user.removeSubGroup(group);
		user.updatePermissions(true);

		sendpMessage("User &b{0}&e removed from sub group &b{1}", group, user.getName());
	}
}