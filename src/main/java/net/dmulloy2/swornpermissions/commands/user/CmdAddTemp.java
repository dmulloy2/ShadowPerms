/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.swornpermissions.commands.user;

import net.dmulloy2.exception.BadTimeException;
import net.dmulloy2.swornpermissions.SwornPermissions;
import net.dmulloy2.swornpermissions.types.Permission;
import net.dmulloy2.util.TimeUtil;

/**
 * @author dmulloy2
 */

public class CmdAddTemp extends UserCommand
{
	public CmdAddTemp(SwornPermissions plugin)
	{
		super(plugin);
		this.action = "add";
		this.name = "temp";
		this.requiredArgs.add("permission");
		this.requiredArgs.add("time");
		this.description = "Temporarily add a permission";
		this.permission = Permission.USER_ADD_PERMISSION;
	}

	@Override
	public void perform()
	{
		String permission = args[0];
		if (user.hasPermission(permission))
		{
			sendpMessage("User &b{0} &ealready has this permission.", user.getName());
			sendpMessage("Node: &b{0}", user.getMatchingPermission(permission));
			return;
		}

		long time;

		try
		{
			time = TimeUtil.parseTime(args[1]);
			if (time <= 0)
				throw new BadTimeException("Time must be greater than 0");
		}
		catch (BadTimeException ex)
		{
			err("Failed to parse time \"{0}\": {1}", args[1], ex);
			return;
		}

		user.addTempPermission(permission, System.currentTimeMillis() + time);
		user.updatePermissions(true);

		sendpMessage("Permission ''&b{0}&e'' added to user &b{1}&e''s permissions for &b{2}&e.", permission, user.getName(),
				TimeUtil.formatTime(time));
	}
}