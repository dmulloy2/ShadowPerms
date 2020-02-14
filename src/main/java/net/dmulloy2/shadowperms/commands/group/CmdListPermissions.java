/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.shadowperms.commands.group;

import java.util.List;

import net.dmulloy2.shadowperms.ShadowPerms;
import net.dmulloy2.shadowperms.commands.ShadowPermsCommand;
import net.dmulloy2.shadowperms.types.Group;
import net.dmulloy2.shadowperms.types.Permission;
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
		this.addRequiredArg("group");
		this.description = "List a group''s permissions";
		this.permission = Permission.GROUP_LIST_PERMISSIONS;
	}

	@Override
	public void perform()
	{
		Group group = getGroup(0);
		sendMessage("&3---- &e{0} &3----", group.getName());

		List<String> permissions = group.getPermissionNodes();
		if (! permissions.isEmpty())
		{
			sendMessage("&bPermissions&e: {0}", new StringJoiner("&b, &e").appendAll(permissions.toArray(new String[0])));
		}
		else
		{
			sendMessage("No group-specific permissions.");
		}

		List<Group> parents = group.getParentGroups();
		if (! parents.isEmpty())
		{
			StringJoiner joiner = new StringJoiner("&b, &e");
			for (Group parent : parents)
				joiner.append(parent.getName());
	
			sendMessage("&bAnd all from&e: {0}", joiner.toString());
		}
	}
}