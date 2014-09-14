/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.swornpermissions.commands;

import net.dmulloy2.swornpermissions.SwornPermissions;
import net.dmulloy2.swornpermissions.types.Permission;
import net.dmulloy2.swornpermissions.types.User;

/**
 * @author dmulloy2
 */

public class CmdNick extends SwornPermissionsCommand
{
	public CmdNick(SwornPermissions plugin)
	{
		super(plugin);
		this.name = "nick";
		this.requiredArgs.add("nick");
		this.optionalArgs.add("user");
		this.description = "Set a player''s nickname";
		this.permission = Permission.CMD_NICK;
	}

	@Override
	public void perform()
	{
		if (args.length == 1)
		{
			User user = getUser();
			if (user == null)
				return;

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
		else
		{
			User user = getUser(0);
			if (user == null)
				return;

			String nick = args[1];
			if (nick.equalsIgnoreCase("off") || nick.equalsIgnoreCase("null"))
			{
				user.setOption("name", null);
				sendpMessage(player, "Your nickname has been removed.");
				sendpMessage("You have removed &b{0}&e''s nickname.", player.getName());
				return;
			}

			user.setOption("name", args[1]);
			sendpMessage(player, "Your nickname is now \"&r{0}&e\"", args[1]);
			sendpMessage("You have set &b{0}&e''s nickname to \"&r{0}&e\"", player.getName(), args[1]);
		}
	}
}