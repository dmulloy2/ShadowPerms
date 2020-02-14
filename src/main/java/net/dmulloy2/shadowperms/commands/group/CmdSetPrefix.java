/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.shadowperms.commands.group;

import net.dmulloy2.shadowperms.ShadowPerms;
import net.dmulloy2.shadowperms.commands.ShadowPermsCommand;
import net.dmulloy2.shadowperms.types.Group;
import net.dmulloy2.shadowperms.types.Permission;
import net.dmulloy2.util.FormatUtil;

/**
 * @author dmulloy2
 */

public class CmdSetPrefix extends ShadowPermsCommand
{
	public CmdSetPrefix(ShadowPerms plugin)
	{
		super(plugin);
		this.name = "prefix";
		this.addRequiredArg("group");
		this.addRequiredArg("prefix");
		this.description = "Set a group's prefix";
		this.permission = Permission.GROUP_SET_PREFIX;
	}

	@Override
	public void perform()
	{
		Group group = getGroup(0);
		String prefix = FormatUtil.join(" ", args);
		prefix = prefix.replaceAll("\"", "");

		group.setPrefix(prefix);

		sendpMessage("Set group &b{0}&e''s prefix to \"&f{1}&e\"", group.getName(), prefix);
	}
}