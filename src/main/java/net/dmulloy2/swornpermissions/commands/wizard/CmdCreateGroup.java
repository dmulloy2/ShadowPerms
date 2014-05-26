/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.swornpermissions.commands.wizard;

import net.dmulloy2.swornpermissions.SwornPermissions;
import net.dmulloy2.swornpermissions.types.Permission;

/**
 * @author dmulloy2
 */

public class CmdCreateGroup extends WizardCommand
{
	public CmdCreateGroup(SwornPermissions plugin)
	{
		super(plugin);
		this.action = "create";
		this.name = "group";
		this.requiredArgs.add("name");
		this.description = "Create a group via wizard";
		this.permission = Permission.GROUP_CREATE;
	}

	@Override
	public void perform()
	{
		plugin.getWizardHandler().createGroup(player, getWorld().getName());
	}
}