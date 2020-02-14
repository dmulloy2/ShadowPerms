/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.shadowperms.commands;

import net.dmulloy2.shadowperms.ShadowPerms;
import net.dmulloy2.shadowperms.types.Permission;

/**
 * @author dmulloy2
 */

public class CmdVersion extends ShadowPermsCommand
{
	public CmdVersion(ShadowPerms plugin)
	{
		super(plugin);
		this.name = "version";
		this.aliases.add("v");
		this.description = "Displays version information";
		this.permission = Permission.CMD_VERSION;
		this.usesPrefix = true;
	}

	@Override
	public void perform()
	{
		sendMessage("&3---- &eShadowPerms &3----");
		sendMessage("&bVersion&e: {0}", plugin.getDescription().getVersion());
		sendMessage("&bAuthor&e: dmulloy2");
		sendMessage("&bIssues&e: https://github.com/dmulloy2/ShadowPerms/issues");
	}
}