package net.dmulloy2.shadowperms.commands;

import net.dmulloy2.shadowperms.ShadowPerms;
import net.dmulloy2.shadowperms.types.Permission;
import net.dmulloy2.shadowperms.types.User;

import org.bukkit.entity.Player;

/**
 * @author dmulloy2
 */

public class CmdPrefixReset extends ShadowPermsCommand
{
	public CmdPrefixReset(ShadowPerms plugin)
	{
		super(plugin);
		this.name = "prefixreset";
		this.aliases.add("prefixr");
		this.aliases.add("prer");
		this.addOptionalArg("player");
		this.description = "Reset your prefix";
		this.permission = Permission.CMD_PREFIX_RESET;
		this.usesPrefix = false;
	}

	@Override
	public void perform()
	{
		if (args.length == 0)
		{
			User user = plugin.getPermissionHandler().getUser(player);

			user.resetPrefix();

			sendpMessage("Your prefix has been reset!");
		}
		else if (args.length == 1)
		{
			User user = getUser(0);
			
			// Permission check
			if (! sender.getName().equals(user.getName()) && ! hasPermission(Permission.CMD_PREFIX_RESET_OTHERS))
			{
				err("You do not have permission to perform this command!");
				return;
			}

			user.resetPrefix();

			sendpMessage("&eYou have reset {0}''s prefix!", user.getName());

			Player target = user.getPlayer();
			if (target != null && ! hasArgument("--silent"))
				sendpMessage(target, "&eYour prefix has been reset!");
		}
	}
}