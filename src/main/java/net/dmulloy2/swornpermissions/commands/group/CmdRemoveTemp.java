/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.swornpermissions.commands.group;

import net.dmulloy2.swornpermissions.SwornPermissions;
import net.dmulloy2.swornpermissions.types.Permission;

/**
 * @author dmulloy2
 */

public class CmdRemoveTemp extends GroupCommand
{
	public CmdRemoveTemp(SwornPermissions plugin)
	{
		super(plugin);
		this.action = "remove";
		this.name = "temp";
		this.requiredArgs.add("permission");
		this.description = "Removed a temp permission";
		this.permission = Permission.GROUP_REMOVE_PERMISSION;
	}

	@Override
	public void perform()
	{
		String permission = args[0];
		if (! group.hasTempPermission(permission))
		{
			sendpMessage("Group &b{0} &edoes not have temp permission ''&b{1}&e.''", group.getName(), permission);
			return;
		}

		group.removeTempPermission(permission);
		group.updatePermissions(true);

		sendpMessage("Temp permission ''&b{0}&e'' removed from group &b{1}&e''s permissions.", permission, group.getName());
	}
}