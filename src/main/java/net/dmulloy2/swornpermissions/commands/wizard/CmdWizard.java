/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.swornpermissions.commands.wizard;

import java.util.ArrayList;
import java.util.List;

import net.dmulloy2.swornpermissions.SwornPermissions;
import net.dmulloy2.swornpermissions.commands.SwornPermissionsCommand;

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
		String action = "";
		String name = "";
		List<String> argsList = new ArrayList<>();

		if (args.length == 2)
		{
			action = "";
			name = args[1];
			for (int i = 2; i < args.length; i++)
				argsList.add(args[i]);
		}
		else
		{
			action = args[1];
			name = args[2];
			for (int i = 3; i < args.length; i++)
				argsList.add(args[i]);
		}

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
	public List<WizardCommand> getSubCommands()
	{
		return subCommands;
	}
}