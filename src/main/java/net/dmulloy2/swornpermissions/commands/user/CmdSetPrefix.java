/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.swornpermissions.commands.user;

import net.dmulloy2.swornpermissions.SwornPermissions;
import net.dmulloy2.swornpermissions.types.Permission;
import net.dmulloy2.swornpermissions.util.FormatUtil;

/**
 * @author dmulloy2
 */

public class CmdSetPrefix extends UserCommand
{

	public CmdSetPrefix(SwornPermissions plugin)
	{
		super(plugin);
		this.action = "set";
		this.name = "prefix";
		this.requiredArgs.add("prefix");
		this.description = "Set a user's prefix";
		this.permission = Permission.USER_SET_PREFIX;
	}

	@Override
	public void perform()
	{
		String prefix = FormatUtil.join(" ", args);
		prefix = prefix.replaceAll("\"", "");

		user.setPrefix(prefix);

		sendpMessage("Set user &b{0}&e''s prefix to \"&f{1}&e\"", user.getName(), prefix);
	}
}