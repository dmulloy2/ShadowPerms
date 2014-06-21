/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.swornpermissions.commands.group;

import net.dmulloy2.swornpermissions.SwornPermissions;
import net.dmulloy2.swornpermissions.types.Permission;
import net.dmulloy2.util.TimeUtil;

/**
 * @author dmulloy2
 */

public class CmdAddTemp extends GroupCommand
{
	public CmdAddTemp(SwornPermissions plugin)
	{
		super(plugin);
		this.action = "add";
		this.name = "temp";
		this.requiredArgs.add("permission");
		this.requiredArgs.add("time");
		this.description = "Temporarily add a permission";
		this.permission = Permission.GROUP_ADD_PERMISSION;
	}

	@Override
	public void perform()
	{
		String permission = args[0];
		if (group.hasPermission(permission))
		{
			sendpMessage("Group &b{0} &ealready has this permission.", group.getName());
			sendpMessage("Node: &b{0}", group.getMatchingPermission(permission));
			return;
		}

		long time;

		try
		{
			time = TimeUtil.parseTime(args[1]);
			if (time <= 0)
				throw new Exception();
		}
		catch (Throwable ex)
		{
			err("Please specify a valid time!");
			return;
		}

		group.addTempPermission(permission, System.currentTimeMillis() + time);
		group.updatePermissions(true);

		sendpMessage("Permission ''&b{0}&e'' added to group &b{1}&e''s permissions for &b{2}&e.", permission, group.getName(),
				TimeUtil.formatTime(time));
	}
}