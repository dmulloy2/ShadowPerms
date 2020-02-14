package net.dmulloy2.shadowperms.commands;

import net.dmulloy2.shadowperms.ShadowPerms;
import net.dmulloy2.shadowperms.types.Permission;
import net.dmulloy2.shadowperms.types.User;
import net.dmulloy2.util.FormatUtil;

import org.bukkit.ChatColor;

/**
 * @author dmulloy2
 */

public class CmdSuffix extends ShadowPermsCommand
{
	public CmdSuffix(ShadowPerms plugin)
	{
		super(plugin);
		this.name = "suffix";
		this.aliases.add("suf");
		this.addRequiredArg("suffix");
		this.description = "Change your suffix";
		this.permission = Permission.CMD_SUFFIX;
		this.mustBePlayer = true;
		this.usesPrefix = false;
	}

	@Override
	public void perform()
	{
		User user = plugin.getPermissionHandler().getUser(player);
		if (user == null)
			return;

		String suffix = FormatUtil.join(" ", args);
		suffix = suffix.replace("\"", "");

		String argsCheck = ChatColor.stripColor(FormatUtil.replaceColors(suffix));
		argsCheck = argsCheck.replace("\\[", "").replace("]", "");

		// Perform and enforce args check
		int maxLength = plugin.getConfig().getInt("suffix.maxLength");
		if (argsCheck.length() > maxLength)
		{
			err("Your suffix is too long! (Max &c{0} &4characters)", maxLength);
			return;
		}

		// Trim the string
		suffix = suffix.trim();

		if (suffix.isEmpty())
		{
			sendpMessage("&eYour suffix has been reset!");
			user.resetSuffix();
			return;
		}

		// Apply configuration settings
		if (plugin.getConfig().getBoolean("suffix.forceReset"))
			suffix = suffix + "&r";
		if (plugin.getConfig().getBoolean("suffix.forceSpace"))
			suffix = " " + suffix;

		user.setSuffix(suffix);

		sendpMessage("&eYour suffix is now \"{0}&e\"", FormatUtil.replaceColors(suffix));
	}
}