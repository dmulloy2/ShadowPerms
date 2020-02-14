/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.shadowperms.commands.group;

import net.dmulloy2.shadowperms.ShadowPerms;
import net.dmulloy2.shadowperms.commands.ShadowPermsCommand;
import net.dmulloy2.shadowperms.types.Group;
import net.dmulloy2.shadowperms.types.Permission;

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
		this.addRequiredArg("group");
		this.addRequiredArg("permission");
		this.description = "Give a group a permission";
		this.permission = Permission.GROUP_ADD_PERMISSION;
	}

	@Override
	public void perform()
	{
		Group group = getGroup(0);
		String node = args[1];

		if (node.contains("**"))
		{
			err("Permission \"&c{0}&4\" contains invalid characters: &cdouble star&4!", permission);
			return;
		}

		boolean negative = node.startsWith("-");
		String permission = negative ? node.substring(1) : node;

		if (group.hasPermission(permission) && ! negative)
		{
			sendpMessage("Group &b{0} &ealready has access to this permission.", group.getName());
			String matchingPerm = group.getMatchingPermission(permission);
			if (matchingPerm != null)
				sendpMessage("Node: &b{0}", group.getMatchingPermission(permission));
			return;
		}

		if (group.hasPermissionNode(node))
		{
			sendpMessage("Group &b{0} &ealready has this node.", group.getName());
			return;
		}

		group.addPermission(node);
		group.updatePermissions(true, true);

		sendpMessage("Permission ''&b{0}&e'' added to group &b{1}&e''s permissions.", node, group.getName());
	}
}