/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.shadowperms.commands.group;

import java.util.Arrays;

import net.dmulloy2.shadowperms.ShadowPerms;
import net.dmulloy2.shadowperms.commands.ShadowPermsCommand;
import net.dmulloy2.shadowperms.types.Group;
import net.dmulloy2.shadowperms.types.Permission;
import net.dmulloy2.util.FormatUtil;
import net.dmulloy2.util.NumberUtil;
import net.dmulloy2.util.Util;

/**
 * @author dmulloy2
 */

public class CmdSetOption extends ShadowPermsCommand
{
	public CmdSetOption(ShadowPerms plugin)
	{
		super(plugin);
		this.name = "option";
		this.addRequiredArg("group");
		this.addRequiredArg("option");
		this.addRequiredArg("value");
		this.description = "Set an option for a group";
		this.permission = Permission.GROUP_SET_OPTION;
	}

	@Override
	public void perform()
	{
		Group group = getGroup(0);
		String key = args[1];
		Object val = null;

		String valStr = FormatUtil.join(" ", Arrays.copyOfRange(args, 2, args.length));
		valStr = valStr.replaceAll("\"", "");
		valStr = valStr.trim();

		if (valStr.contains("b:"))
			val = Util.toBoolean(valStr);
		else if (valStr.contains("i:"))
			val = NumberUtil.toInt(valStr);
		else if (valStr.contains("d:"))
			val = NumberUtil.toDouble(valStr);
		else if (! valStr.equalsIgnoreCase("null") && ! valStr.isEmpty())
			val = valStr;

		group.setOption(key, val);

		if (val == null)
			sendpMessage("Option ''&b{0}&e'' removed from group &b{1}&e.", key, group.getName());
		else
			sendpMessage("Option ''&b{0}&e'' set to ''&b{1}&e'' for group &b{2}&e.", key, val, group.getName());
	}
}