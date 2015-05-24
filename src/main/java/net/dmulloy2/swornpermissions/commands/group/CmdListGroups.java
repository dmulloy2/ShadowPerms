/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.swornpermissions.commands.group;

import java.util.Map;
import java.util.Map.Entry;

import net.dmulloy2.swornpermissions.SwornPermissions;
import net.dmulloy2.swornpermissions.commands.SwornPermissionsCommand;
import net.dmulloy2.swornpermissions.types.Permission;
import net.dmulloy2.swornpermissions.types.WorldGroup;
import net.dmulloy2.types.StringJoiner;

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
		StringJoiner joiner = new StringJoiner("&b, &e");
		
		sendMessage("&3[ &eServer Groups &3]");
		sendMessage("  &e{0}", joiner.appendAll(plugin.getPermissionHandler().getServerGroups().keySet()));

		sendMessage("&3[ &eWorld Groups &3]");
		for (Entry<String, Map<String, WorldGroup>> entry : plugin.getPermissionHandler().getWorldGroups().entrySet())
		{
			Map<String, WorldGroup> groups = entry.getValue();
			if (! groups.isEmpty())
				sendMessage("  &b{0}&e: {1}", entry.getKey(), joiner.newString().appendAll(groups.keySet()));
		}
	}
}