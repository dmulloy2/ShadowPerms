/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.swornpermissions.handlers;

import java.util.logging.Level;

import lombok.AllArgsConstructor;
import net.dmulloy2.swornpermissions.SwornPermissions;
import net.dmulloy2.util.FormatUtil;

/**
 * @author dmulloy2
 */

@AllArgsConstructor
public class LogHandler
{
	private final SwornPermissions plugin;

	public final void log(Level level, String msg, Object... objects)
	{
		plugin.getLogger().log(level, FormatUtil.format(msg, objects));
	}

	public final void log(String msg, Object... objects)
	{
		plugin.getLogger().info(FormatUtil.format(msg, objects));
	}

	public final void debug(String msg, Object... objects)
	{
		if (plugin.getConfig().getBoolean("debug", false))
		{
			plugin.getLogger().info(FormatUtil.format("[Debug] " + msg, objects));
		}
	}
}