/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.shadowperms.commands;

import net.dmulloy2.shadowperms.ShadowPerms;
import net.dmulloy2.shadowperms.types.Permission;

/**
 * @author dmulloy2
 */

public class CmdReload extends ShadowPermsCommand
{
	public CmdReload(ShadowPerms plugin)
	{
		super(plugin);
		this.name = "reload";
		this.aliases.add("rl");
		this.addOptionalArg("type");
		this.description = "reload ShadowPerms";
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
			plugin.getLogHandler().log("Reloading ShadowPerms config...");
			sendpMessage("&eReloading &bShadowPerms &econfig...");

			plugin.reloadConfig();
			plugin.getChatHandler().reload();
		}
		else
		{
			plugin.getLogHandler().log("Reloading ShadowPerms...");
			sendpMessage("&eReloading &bShadowPerms&e...");

			plugin.reload();
		}

		sendpMessage("&aReload complete. Took {0} ms.", System.currentTimeMillis() - start);
		plugin.getLogHandler().log("Reload complete. Took {0} ms.", System.currentTimeMillis() - start);
	}
}