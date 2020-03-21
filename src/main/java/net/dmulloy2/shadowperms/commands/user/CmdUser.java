/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.shadowperms.commands.user;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.dmulloy2.shadowperms.ShadowPerms;
import net.dmulloy2.shadowperms.commands.ShadowPermsCommand;
import net.dmulloy2.shadowperms.types.Permission;
import net.dmulloy2.shadowperms.types.User;
import net.dmulloy2.types.StringJoiner;

/**
 * @author dmulloy2
 */

public class CmdUser extends ShadowPermsCommand
{
	public CmdUser(ShadowPerms plugin)
	{
		super(plugin);
		this.name = "user";
		this.description = "View information about a user";
		this.permission = Permission.USER_VIEW_INFO;

		addSubCommand(new CmdAddPermission(plugin));
		addSubCommand(new CmdAddSubgroup(plugin));
		addSubCommand(new CmdHasGroup(plugin));
		addSubCommand(new CmdHasPermission(plugin));
		addSubCommand(new CmdListPermissions(plugin));
		addSubCommand(new CmdRemovePermission(plugin));
		addSubCommand(new CmdRemoveSubgroup(plugin));
		addSubCommand(new CmdReset(plugin));
		addSubCommand(new CmdSetGroup(plugin));
		addSubCommand(new CmdSetOption(plugin));
		addSubCommand(new CmdSetPrefix(plugin));
		addSubCommand(new CmdSetSuffix(plugin));
	}

	@Override
	public void perform()
	{
		User user = getUser(0);
		if (user == null) return;

		sendMessage("&3---- &e{0} &3----", user.getName());
		sendMessage("&bGroup&e: {0}", user.getGroupName());

		List<String> subGroups = user.getSubGroupNames();
		if (! subGroups.isEmpty())
		{
			sendMessage("&bSub Groups&e: {0}", new StringJoiner("&b, &e").appendAll(subGroups));
		}

		List<String> permissions = user.getPermissionNodes();
		if (! permissions.isEmpty())
		{
			sendMessage("&bPermissions&e: {0}", new StringJoiner("&b, &e").appendAll(permissions));
		}

		Map<String, Object> options = user.getOptions();
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