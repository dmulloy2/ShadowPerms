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

public class CmdHasPermission extends ShadowPermsCommand
{
	public CmdHasPermission(ShadowPerms plugin)
	{
		super(plugin);
		this.name = "has";
		this.addRequiredArg("group");
		this.addRequiredArg("permission");
		this.description = "Checks if a group has a permission";
		this.permission = Permission.GROUP_HAS_PERMISSION;
	}

	@Override
	public void perform()
	{
		Group group = getGroup(0);
		String permission = args[1];

		if (group.hasPermission(permission))
		{
			sendpMessage("Group &b{0} &ehas access to permission &b{1}&e.", group.getName(), permission);

			String node = group.getMatchingPermission(permission);
			if (node != null && ! node.equalsIgnoreCase(permission))
			{
				sendpMessage("Matching node: &b{0}", node);
			}
		}
		else
		{
			sendpMessage("Group &b{0} &edoes not have access to permission &b{1}&e.", group.getName(), permission);

			String node = group.getMatchingPermission(permission);
			if (node != null)
				sendpMessage("Negated, node: &b{0}", node);
		}
	}
}