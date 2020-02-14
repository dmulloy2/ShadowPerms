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

public class CmdRemovePermission extends ShadowPermsCommand
{
	public CmdRemovePermission(ShadowPerms plugin)
	{
		super(plugin);
		this.name = "delperm";
		this.aliases.add("rmperm");
		this.addRequiredArg("group");
		this.addRequiredArg("permission");
		this.description = "Remove a permission from a group";
		this.permission = Permission.GROUP_REMOVE_PERMISSION;
	}

	@Override
	public void perform()
	{
		Group group = getGroup(0);
		String permission = args[1];

		if (! group.hasPermissionNode(permission))
		{
			sendpMessage("Group &b{0} &edoes not have permission ''&b{1}&e.''", group.getName(), permission);
			return;
		}

		group.removePermission(permission);
		group.updatePermissions(true, true);

		sendpMessage("Permission ''&b{0}&e'' removed from group &b{1}&e''s permissions.", permission, group.getName());
	}
}