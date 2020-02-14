/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.shadowperms.commands.user;

import net.dmulloy2.shadowperms.ShadowPerms;
import net.dmulloy2.shadowperms.commands.ShadowPermsCommand;
import net.dmulloy2.shadowperms.types.Group;
import net.dmulloy2.shadowperms.types.Permission;
import net.dmulloy2.shadowperms.types.ServerGroup;
import net.dmulloy2.shadowperms.types.User;

/**
 * @author dmulloy2
 */

public class CmdSetGroup extends ShadowPermsCommand
{
	public CmdSetGroup(ShadowPerms plugin)
	{
		super(plugin);
		this.name = "group";
		this.addRequiredArg("user");
		this.addRequiredArg("group");
		this.description = "Set a user's group";
		this.permission = Permission.USER_SET_GROUP;
	}

	@Override
	public void perform()
	{
		User user = getUser(0);
		Group group = getGroup(1);

		if (group instanceof ServerGroup)
		{
			err("A user''s primary group cannot be a Server Group!");
			return;
		}

		user.setGroup(group);
		user.updatePermissions(true);

		sendpMessage("User &b{0} &emoved to group &b{1}.", user.getName(), group.getName());
	}
}