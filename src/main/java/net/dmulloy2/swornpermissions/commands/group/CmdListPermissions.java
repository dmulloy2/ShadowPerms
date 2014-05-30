/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.swornpermissions.commands.group;

import java.util.List;

import net.dmulloy2.swornpermissions.SwornPermissions;
import net.dmulloy2.swornpermissions.permissions.Group;
import net.dmulloy2.swornpermissions.types.Permission;
import net.dmulloy2.swornpermissions.types.StringJoiner;

/**
 * @author dmulloy2
 */

public class CmdListPermissions extends GroupCommand
{
	public CmdListPermissions(SwornPermissions plugin)
	{
		super(plugin);
		this.action = "list";
		this.name = "permissions";
		this.description = "List a group''s permissions";
		this.permission = Permission.GROUP_LIST_PERMISSIONS;
	}

	@Override
	public void perform()
	{
		sendMessage("&3====[ &e{0} &3]====", group.getName());

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