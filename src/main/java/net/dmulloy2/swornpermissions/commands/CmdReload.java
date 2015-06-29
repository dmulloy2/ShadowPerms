/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.swornpermissions.commands;

import net.dmulloy2.swornpermissions.SwornPermissions;
import net.dmulloy2.swornpermissions.types.Permission;

/**
 * @author dmulloy2
 */

public class CmdReload extends SwornPermissionsCommand
{
	public CmdReload(SwornPermissions plugin)
	{
		super(plugin);
		this.name = "reload";
		this.aliases.add("rl");
		this.optionalArgs.add("type");
		this.description = "reload SwornPermissions";
		this.permission = Permission.CMD_RELOAD;
		this.mustBePlayer = false;
		this.usesPrefix = true;
	}

	@Override
	public void perform()
	{
		long start = System.currentTimeMillis();

		if (args.length > 0 && args[0].equalsIgnoreCase("config"))
		{
			plugin.getLogHandler().log("Reloading SwornPermissions config...");
			sendpMessage("&eReloading &bSwornPermissions &econfig...");

			plugin.reloadConfig();
			plugin.getChatHandler().reload();
		}
		else
		{
			plugin.getLogHandler().log("Reloading SwornPermissions...");
			sendpMessage("&eReloading &bSwornPermissions&e...");

			plugin.reload();
		}

		sendpMessage("&aReload complete. Took {0} ms.", System.currentTimeMillis() - start);
		plugin.getLogHandler().log("Reload complete. Took {0} ms.", System.currentTimeMillis() - start);
	}
}