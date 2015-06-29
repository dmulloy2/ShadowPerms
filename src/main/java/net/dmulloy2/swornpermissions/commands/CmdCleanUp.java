/**
 * (c) 2015 dmulloy2
 */
package net.dmulloy2.swornpermissions.commands;

import net.dmulloy2.swornpermissions.SwornPermissions;
import net.dmulloy2.swornpermissions.types.Permission;

/**
 * @author dmulloy2
 */

public class CmdCleanUp extends SwornPermissionsCommand
{
	public CmdCleanUp(SwornPermissions plugin)
	{
		super(plugin);
		this.name = "cleanup";
		this.aliases.add("unload");
		this.description = "Unload offline users to save memory";
		this.permission = Permission.CMD_SAVE;
		this.usesPrefix = true;
	}

	@Override
	public void perform()
	{
		long start = System.currentTimeMillis();

		plugin.getLogHandler().log("Unloading offline users...");
		sendpMessage("&eUnloading offline users...");

		plugin.getPermissionHandler().cleanupUsers(0);
		System.gc();

		sendpMessage("&aUnload complete. Took {0} ms.", System.currentTimeMillis() - start);
		plugin.getLogHandler().log("Unload complete. Took {0} ms.", System.currentTimeMillis() - start);
	}
}