/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.swornpermissions.commands.wizard;

import java.util.ArrayList;
import java.util.List;

import net.dmulloy2.swornpermissions.SwornPermissions;
import net.dmulloy2.swornpermissions.commands.SwornPermissionsCommand;

import org.bukkit.command.CommandSender;

/**
 * @author dmulloy2
 */

public class CmdWizard extends SwornPermissionsCommand
{
	private List<WizardCommand> subCommands;

	public CmdWizard(SwornPermissions plugin)
	{
		super(plugin);
		this.name = "wizard";
		this.requiredArgs.add("action");
		this.requiredArgs.add("args");
		this.optionalArgs.add("world");
		this.description = "Modify permissions with a wizard";
		this.hasSubCommands = true;
		this.mustBePlayer = true;
		this.usesPrefix = true;

		this.registerSubCommands();
	}

	private final void registerSubCommands()
	{
		this.subCommands = new ArrayList<WizardCommand>();

		subCommands.add(new CmdCreateGroup(plugin));
	}

	@Override
	public void perform()
	{
		List<String> argsList = new ArrayList<String>();

		String action = args[1];
		String name = args[2];
		for (int i = 3; i < args.length; i++)
			argsList.add(args[i]);

		for (WizardCommand command : subCommands)
		{
			if (command.getAction().equalsIgnoreCase(action))
			{
				if (name.equalsIgnoreCase(command.getName()) || command.getAliases().contains(name.toLowerCase()))
				{
					command.execute(sender, argsList.toArray(new String[0]));
					return;
				}
			}
		}

		err("Invalid arguments! Try &c/swornperms help&4!");
	}

	@Override
	public List<String> getSubCommandHelp(CommandSender sender)
	{
		List<String> ret = new ArrayList<String>();
		for (WizardCommand command : subCommands)
		{
			if (plugin.getPermissionHandler().hasPermission(sender, command.getPermission()))
				ret.add(command.getUsageTemplate(true));
		}

		return ret;
	}
}