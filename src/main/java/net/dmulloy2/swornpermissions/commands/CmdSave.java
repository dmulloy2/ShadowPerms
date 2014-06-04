/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.swornpermissions.commands;

import net.dmulloy2.swornpermissions.SwornPermissions;
import net.dmulloy2.swornpermissions.types.Permission;

/**
 * @author dmulloy2
 */

public class CmdSave extends SwornPermissionsCommand
{
	public CmdSave(SwornPermissions plugin)
	{
		super(plugin);
		this.name = "save";
		this.description = "Saves users and groups to disk";
		this.permission = Permission.CMD_SAVE;
		this.usesPrefix = true;
	}

	@Override
	public void perform()
	{
		long start = System.currentTimeMillis();
		sendpMessage("Saving users and groups to disk...");

		plugin.getDataHandler().save();

		sendpMessage("Users and groups saved to disk! Took {0} ms!", System.currentTimeMillis() - start);
	}
}