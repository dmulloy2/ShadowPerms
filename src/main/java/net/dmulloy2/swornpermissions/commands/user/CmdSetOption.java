/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.swornpermissions.commands.user;

import net.dmulloy2.swornpermissions.SwornPermissions;
import net.dmulloy2.swornpermissions.types.Permission;
import net.dmulloy2.swornpermissions.util.NumberUtil;
import net.dmulloy2.swornpermissions.util.Util;

/**
 * @author dmulloy2
 */

public class CmdSetOption extends UserCommand
{
	public CmdSetOption(SwornPermissions plugin)
	{
		super(plugin);
		this.action = "set";
		this.name = "option";
		this.requiredArgs.add("option");
		this.requiredArgs.add("value");
		this.description = "Set an option for a user";
		this.permission = Permission.USER_SET_OPTION;
	}

	@Override
	public void perform()
	{
		String key = args[3];
		Object val = null;

		String valStr = args[4];
		if (valStr.contains("b:"))
			val = Util.toBoolean(valStr);
		else if (valStr.contains("i:"))
			val = NumberUtil.toInt(valStr);
		else if (valStr.contains("d:"))
			val = NumberUtil.toDouble(valStr);
		else if (valStr.equalsIgnoreCase("null"))
			val = null;
		else
			val = valStr;

		user.setOption(key, val);

		if (val == null)
			sendpMessage("Option ''&b{0}&e'' removed from user &b{1}&e.", key, user.getName());
		else
			sendpMessage("Option ''&b{0}&e'' set to ''&b{1}&e'' for user &b{2}&e.", key, val, user.getName());
	}
}