/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.shadowperms.commands.user;

import net.dmulloy2.shadowperms.ShadowPerms;
import net.dmulloy2.shadowperms.commands.ShadowPermsCommand;
import net.dmulloy2.shadowperms.types.Group;
import net.dmulloy2.shadowperms.types.Permission;
import net.dmulloy2.shadowperms.types.User;

/**
 * @author dmulloy2
 */

public class CmdAddSubgroup extends ShadowPermsCommand
{
	public CmdAddSubgroup(ShadowPerms plugin)
	{
		super(plugin);
		this.name = "addsub";
		this.aliases.add("addsubgroup");
		this.addRequiredArg("user");
		this.addRequiredArg("group");
		this.description = "Give a subgroup to a user";
		this.permission = Permission.USER_ADD_SUBGROUP;
	}

	@Override
	public void perform()
	{
		User user = getUser(0);
		Group group = getGroup(1);

		if (user.isInGroup(group.getName()) || user.isInSubGroup(group.getName()))
		{
			sendpMessage("Group &b{0} &eis already available to user {1}.", group.getName(), user.getName());
			return;
		}

		user.addSubGroup(group);
		user.updatePermissions(true);

		sendpMessage("Group &b{0} &eadded to user &b{1}&e''s permissions.", group.getName(), user.getName());
	}
}