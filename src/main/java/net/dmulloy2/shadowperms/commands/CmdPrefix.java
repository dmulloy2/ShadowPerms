package net.dmulloy2.shadowperms.commands;

import net.dmulloy2.shadowperms.ShadowPerms;
import net.dmulloy2.shadowperms.types.Permission;
import net.dmulloy2.shadowperms.types.User;
import net.dmulloy2.swornapi.util.FormatUtil;

import org.bukkit.ChatColor;

/**
 * @author dmulloy2
 */

public class CmdPrefix extends ShadowPermsCommand
{
	public CmdPrefix(ShadowPerms plugin)
	{
		super(plugin);
		this.name = "prefix";
		this.aliases.add("pre");
		this.addRequiredArg("prefix");
		this.description = "Change your prefix";
		this.permission = Permission.CMD_PREFIX;
		this.mustBePlayer = true;
		this.usesPrefix = false;
	}

	@Override
	public void perform()
	{
		User user = getUser(0);

		String prefix = getFinalArg(1).replace("\"", "");

		String argsCheck = ChatColor.stripColor(FormatUtil.replaceColors(prefix));
		argsCheck = argsCheck.replace("\\[", "").replace("]", "");

		// Perform and enforce args check
		int maxLength = plugin.getConfig().getInt("prefix.maxLength");
		if (argsCheck.length() > maxLength)
		{
			err("Your prefix is too long! (Max &c{0} &4characters)", maxLength);
			return;
		}

		// Trim the string
		prefix = prefix.trim();

		if (prefix.isEmpty())
		{
			sendpMessage("&eYour prefix has been reset!");
			user.resetPrefix();
			return;
		}

		// Apply configuration settings
		if (plugin.getConfig().getBoolean("prefix.forceReset"))
			prefix = prefix + "&r";
		if (plugin.getConfig().getBoolean("prefix.forceSpace"))
			prefix = prefix + " ";

		user.setPrefix(prefix);

		sendpMessage("&eYour prefix is now \"{0}&e\"", FormatUtil.replaceColors(prefix));
	}
}