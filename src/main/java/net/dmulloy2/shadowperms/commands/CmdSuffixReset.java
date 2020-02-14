package net.dmulloy2.shadowperms.commands;

import net.dmulloy2.shadowperms.ShadowPerms;
import net.dmulloy2.shadowperms.types.Permission;
import net.dmulloy2.shadowperms.types.User;

import org.bukkit.entity.Player;

/**
 * @author dmulloy2
 */

public class CmdSuffixReset extends ShadowPermsCommand
{
	public CmdSuffixReset(ShadowPerms plugin)
	{
		super(plugin);
		this.name = "suffixreset";
		this.aliases.add("suffixr");
		this.aliases.add("sufr");
		this.addOptionalArg("player");
		this.description = "Reset your suffix";
		this.permission = Permission.CMD_SUFFIX_RESET;
		this.mustBePlayer = true;
		this.usesPrefix = false;
	}

	@Override
	public void perform()
	{
		if (args.length == 0)
		{
			User user = plugin.getPermissionHandler().getUser(player);

			user.resetSuffix();

			sendpMessage("Your suffix has been reset!");
		}
		else if (args.length == 1)
		{
			User user = getUser(0);
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

			Player target = user.getPlayer();
			if (target != null && ! hasArgument("--silent"))
				sendpMessage(target, "&eYour suffix has been reset!");
		}
	}
}