package net.dmulloy2.swornpermissions.commands;

import net.dmulloy2.swornpermissions.SwornPermissions;
import net.dmulloy2.swornpermissions.permissions.User;
import net.dmulloy2.swornpermissions.types.Permission;

/**
 * @author dmulloy2
 */

public class CmdSuffixReset extends SwornPermissionsCommand
{
	public CmdSuffixReset(SwornPermissions plugin)
	{
		super(plugin);
		this.name = "suffixreset";
		this.aliases.add("prer");
		this.optionalArgs.add("player");
		this.description = "Reset your suffix";
		this.permission = Permission.CMD_SUFFIX_RESET;
	}

	@Override
	public void perform()
	{
		if (args.length == 0)
		{
			User user = getUser(true);
			if (user == null)
				return;

			user.resetSuffix();

			sendpMessage("Your suffix has been reset!");
		}
		else if (args.length == 1)
		{
			User user = getUser(0, getWorld(), true);
			if (user == null)
				return;

			// Permission check
			if (! sender.getName().equals(user.getName()) && ! hasPermission(Permission.CMD_SUFFIX_RESET_OTHERS))
			{
				err("You do not have permission to perform this command!");
				return;
			}

			user.resetSuffix();

			sendpMessage("&eYou have reset {0}''s suffix!", user.getName());
			sendpMessage(user.getPlayer(), "&eYour suffix has been reset!");
		}
	}
}