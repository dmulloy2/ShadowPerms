/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.swornpermissions.commands;

import net.dmulloy2.swornpermissions.SwornPermissions;
import net.dmulloy2.swornpermissions.types.Group;
import net.dmulloy2.swornpermissions.types.Permission;

/**
 * @author dmulloy2
 */

public class CmdCreateGroup extends SwornPermissionsCommand
{
	public CmdCreateGroup(SwornPermissions plugin)
	{
		super(plugin);
		this.name = "create";
		this.requiredArgs.add("group");
		this.description = "Creates a group";
		this.permission = Permission.GROUP_CREATE;
		this.usesPrefix = true;
	}

	@Override
	public void perform()
	{
		String name = args[0];
		boolean server = name.startsWith("s:");
		name = server ? name.substring(2) : name;
		
		if (! name.matches("^[a-zA-Z_0-9]+$"))
		{
			err("Name contains invalid characters!");
			return;
		}

		Group group;
		if (server) group = plugin.getPermissionHandler().createServerGroup(name);
		else group = plugin.getPermissionHandler().createWorldGroup(name, getWorld());

		if (group == null)
		{
			err("Failed to create group! Contact an administrator!");
			return;
		}

		// TODO: Some sort of creation wizard?zO

		sendpMessage("Group &b{0} &esuccessfully created!", group.getName());
	}
}