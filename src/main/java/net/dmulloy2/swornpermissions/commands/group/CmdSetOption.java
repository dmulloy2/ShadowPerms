/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.swornpermissions.commands.group;

import net.dmulloy2.swornpermissions.SwornPermissions;
import net.dmulloy2.swornpermissions.types.Permission;
import net.dmulloy2.swornpermissions.util.NumberUtil;
import net.dmulloy2.swornpermissions.util.Util;

/**
 * @author dmulloy2
 */

public class CmdSetOption extends GroupCommand
{
	public CmdSetOption(SwornPermissions plugin)
	{
		super(plugin);
		this.action = "set";
		this.name = "option";
		this.requiredArgs.add("option");
		this.requiredArgs.add("value");
		this.description = "Set an option for a group";
		this.permission = Permission.GROUP_SET_OPTION;
	}

	@Override
	public void perform()
	{
		// Syntax for options: String: "[value]", Boolean: "b:[value]", Integer: "i:[value]", Double: "d:[value]"

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

		group.setOption(key, val);

		if (val == null)
			sendpMessage("Option ''{0}'' removed from group {1}.", key, group.getName());
		else
			sendpMessage("Option ''{0}'' set to ''{1}'' for group {2}.", key, val, group.getName());
	}
}