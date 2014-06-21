/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.swornpermissions.commands.wizard;

import lombok.Getter;
import net.dmulloy2.swornpermissions.SwornPermissions;
import net.dmulloy2.swornpermissions.commands.SwornPermissionsCommand;
import net.dmulloy2.util.FormatUtil;

/**
 * @author dmulloy2
 */

public abstract class WizardCommand extends SwornPermissionsCommand
{
	protected @Getter String action;

	public WizardCommand(SwornPermissions plugin)
	{
		super(plugin);
	}

	@Override
	public String getUsageTemplate(boolean displayHelp)
	{
		StringBuilder ret = new StringBuilder();
		ret.append(String.format("&b/%s &buser &3<user> &b%s %s", plugin.getCommandHandler().getCommandPrefix(), action, name));

		ret.append("&3 ");
		for (String s : requiredArgs.subList(1, requiredArgs.size()))
			ret.append(String.format("<%s> ", s));

		for (String s : optionalArgs)
			ret.append(String.format("[%s] ", s));

		if (displayHelp)
			ret.append("&e" + description);

		return FormatUtil.format(ret.toString());
	}
}