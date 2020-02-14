/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.shadowperms.commands;

import net.dmulloy2.shadowperms.ShadowPerms;
import net.dmulloy2.shadowperms.types.Permission;
import net.dmulloy2.shadowperms.types.User;

import org.bukkit.entity.Player;

/**
 * @author dmulloy2
 */

public class CmdNick extends ShadowPermsCommand
{
	public CmdNick(ShadowPerms plugin)
	{
		super(plugin);
		this.name = "nick";
		this.addOptionalArg("user");
		this.addRequiredArg("nick");
		this.description = "Set a player''s nickname";
		this.permission = Permission.CMD_NICK;
		this.mustBePlayer = true;
		this.usesPrefix = false;
	}

	@Override
	public void perform()
	{
		User user = plugin.getPermissionHandler().getUser(player);

		String nick = args[0];
		if (nick.equalsIgnoreCase("off") || nick.equalsIgnoreCase("null"))
		{
			user.setOption("name", null);
			sendpMessage("You have removed your nickname.");
			return;
		}

		user.setOption("name", args[0]);
		sendpMessage("You have set your nickname to \"&r{0}&e\"", args[0]);
	}
}