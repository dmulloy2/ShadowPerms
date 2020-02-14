/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.shadowperms.commands;

import net.dmulloy2.shadowperms.ShadowPerms;
import net.dmulloy2.shadowperms.types.Permission;

/**
 * @author dmulloy2
 */

public class CmdSave extends ShadowPermsCommand
{
	public CmdSave(ShadowPerms plugin)
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