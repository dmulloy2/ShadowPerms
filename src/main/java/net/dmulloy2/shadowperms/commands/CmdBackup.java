/**
 * (c) 2016 dmulloy2
 */
package net.dmulloy2.shadowperms.commands;

import net.dmulloy2.shadowperms.ShadowPerms;
import net.dmulloy2.shadowperms.types.Permission;

/**
 * @author dmulloy2
 */

public class CmdBackup extends ShadowPermsCommand
{
	public CmdBackup(ShadowPerms plugin)
	{
		super(plugin);
		this.description = "Backs up all ShadowPerms files";
		this.permission = Permission.CMD_BACKUP;
		this.usesPrefix = true;
	}

	@Override
	public void perform()
	{
		plugin.getDataHandler().backup(sender);
	}
}