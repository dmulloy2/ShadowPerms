/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.shadowperms.commands.group;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.dmulloy2.shadowperms.ShadowPerms;
import net.dmulloy2.shadowperms.commands.ShadowPermsCommand;
import net.dmulloy2.shadowperms.types.Group;
import net.dmulloy2.shadowperms.types.Permission;
import net.dmulloy2.swornapi.types.StringJoiner;

/**
 * @author dmulloy2
 */

public class CmdGroup extends ShadowPermsCommand
{
	private List<ShadowPermsCommand> subCommands;

	public CmdGroup(ShadowPerms plugin)
	{
		super(plugin);
		this.name = "group";
		this.addRequiredArg("group");
		this.description = "View information about a group";
		this.permission = Permission.GROUP_VIEW_INFO;

		addSubCommand(new CmdAddPermission(plugin));
		addSubCommand(new CmdHasPermission(plugin));
		addSubCommand(new CmdListUsers(plugin));
		addSubCommand(new CmdListPermissions(plugin));
		addSubCommand(new CmdRemovePermission(plugin));
		addSubCommand(new CmdSetOption(plugin));
		addSubCommand(new CmdSetPrefix(plugin));
	}

	@Override
	public void perform()
	{
		Group group = getGroup(0);
		sendMessage("&3---- &e{0} &3----", group.getName());

		List<String> permissions = group.getPermissionNodes();
		if (permissions.size() > 0)
		{
			sendMessage("&bPermissions&e: {0}", new StringJoiner("&b, &e").appendAll(permissions));
		}

		List<Group> parents = group.getParentGroups();
		if (parents.size() > 0)
		{
			sendMessage("&bParents&e:");
			for (Group parent : parents)
			{
				sendMessage("  &b- &e{0}", parent.getName());
			}
		}

		Map<String, Object> options = group.getOptions();
		if (options.size() > 0)
		{
			sendMessage("&bOptions&e:");
			for (Entry<String, Object> entry : options.entrySet())
			{
				sendMessage("  &b{0}&e: \"&f{1}&e\"", entry.getKey(), entry.getValue());
			}
		}
	}
}