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

		String type = args.length == 0 ? "all" : args[0];
		switch (type)
		{
			case "config":
				sendpMessage("&eReloading &bSwornPermissions &econfig...");
				plugin.reloadConfig();
				plugin.getChatHandler().reload();
				break;
			default:
				sendpMessage("&eReloading &bSwornPermissions&e...");
				plugin.reload();
				break;
		}

		sendpMessage("&aReload Complete! Took {0} ms!", System.currentTimeMillis() - start);
	}
}