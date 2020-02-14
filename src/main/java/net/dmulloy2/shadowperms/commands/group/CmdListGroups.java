/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.shadowperms.commands.group;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.dmulloy2.shadowperms.ShadowPerms;
import net.dmulloy2.shadowperms.commands.ShadowPermsCommand;
import net.dmulloy2.shadowperms.types.Permission;
import net.dmulloy2.shadowperms.types.WorldGroup;
import net.dmulloy2.types.StringJoiner;

/**
 * @author dmulloy2
 */

public class CmdListGroups extends ShadowPermsCommand
{
	public CmdListGroups(ShadowPerms plugin)
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

		Set<String> serverGroups = plugin.getPermissionHandler().getServerGroups().keySet();
		if (! serverGroups.isEmpty())
			sendMessage("  &e{0}", joiner.appendAll(serverGroups));
		else
			sendMessage("  &eNone");

		sendMessage("&3[ &eWorld Groups &3]");
		for (Entry<String, Map<String, WorldGroup>> entry : plugin.getPermissionHandler().getWorldGroups().entrySet())
		{
			Map<String, WorldGroup> groups = entry.getValue();
			if (! groups.isEmpty())
				sendMessage("  &b{0}&e: {1}", entry.getKey(), joiner.newString().appendAll(groups.keySet()));
		}
	}
}