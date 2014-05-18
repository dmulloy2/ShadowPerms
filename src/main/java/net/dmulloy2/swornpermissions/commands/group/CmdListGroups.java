/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.swornpermissions.commands.group;

import java.util.ArrayList;
import java.util.List;

import net.dmulloy2.swornpermissions.SwornPermissions;
import net.dmulloy2.swornpermissions.commands.SwornPermissionsCommand;
import net.dmulloy2.swornpermissions.types.Permission;
import net.dmulloy2.swornpermissions.types.StringJoiner;

/**
 * @author dmulloy2
 */

public class CmdListGroups extends SwornPermissionsCommand
{
	public CmdListGroups(SwornPermissions plugin)
	{
		super(plugin);
		this.name = "listgroups";
		this.description = "List available groups";
		this.permission = Permission.GROUP_LIST;
		this.usesPrefix = true;
	}

	@Override
	public void perform()
	{
		List<String> lines = new ArrayList<String>();
		StringBuilder line = new StringBuilder();

		// Server Groups
		line.append("&bServer Groups&e: ");

		StringJoiner joiner = new StringJoiner("&b, &e");
		joiner.appendAll(plugin.getPermissionHandler().getServerGroups().keySet());
		line.append(joiner.toString());
		lines.add(line.toString());

		// World Groups
		line = new StringBuilder();
		line.append("&bWorld Groups&e: ");

		joiner.newString();
		joiner.appendAll(plugin.getPermissionHandler().getWorldGroups().get(getWorld().getName()).keySet());
		line.append(joiner.toString());
		lines.add(line.toString());

		for (String s : lines)
			sendMessage(s);
	}
}