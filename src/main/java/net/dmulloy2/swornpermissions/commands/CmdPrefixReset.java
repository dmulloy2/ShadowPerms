package net.dmulloy2.swornpermissions.commands;

import net.dmulloy2.swornpermissions.SwornPermissions;
import net.dmulloy2.swornpermissions.types.Permission;
import net.dmulloy2.swornpermissions.types.User;

/**
 * @author dmulloy2
 */

public class CmdPrefixReset extends SwornPermissionsCommand
{
	public CmdPrefixReset(SwornPermissions plugin)
	{
		super(plugin);
		this.name = "prefixreset";
		this.aliases.add("prer");
		this.optionalArgs.add("player");
		this.description = "Reset your prefix";
		this.permission = Permission.CMD_PREFIX_RESET;
	}

	@Override
	public void perform()
	{
		if (args.length == 0)
		{
			User user = getUser(true);
			if (user == null)
				return;

			user.resetPrefix();

			sendpMessage("Your prefix has been reset!");
		}
		else if (args.length == 1)
		{
			User user = getUser(0, getWorld(), true);
			if (user == null)
				return;

			// Permission check
			if (! sender.getName().equals(user.getName()) && ! hasPermission(Permission.CMD_PREFIX_RESET_OTHERS))
			{
				err("You do not have permission to perform this command!");
				return;
			}

			user.resetPrefix();

			sendpMessage("&eYou have reset {0}''s prefix!", user.getName());
			sendpMessage(user.getPlayer(), "&eYour prefix has been reset!");
		}
	}
}