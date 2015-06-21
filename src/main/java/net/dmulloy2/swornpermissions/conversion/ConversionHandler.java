/**
 * (c) 2015 dmulloy2
 */
package net.dmulloy2.swornpermissions.conversion;

import java.io.File;

import net.dmulloy2.swornpermissions.SwornPermissions;

/**
 * @author dmulloy2
 */

public class ConversionHandler
{
	public static void convert(SwornPermissions plugin)
	{
		// Obtain plugins folder
		File dataFolder = plugin.getDataFolder();
		dataFolder.mkdirs();

		File plugins = dataFolder.getParentFile();

		// Check for GroupManager
		File groupManager = new File(plugins, "GroupManager");
		if (groupManager.exists())
		{
			new GroupManagerConverter(groupManager, plugin).convert();
			return;
		}

		// Check for PEX
		File pex = new File(plugins, "PermissionsEx");
		if (pex.exists())
		{
			File perms = new File(pex, "permissions.yml");
			new PermissionsExConverter(perms, plugin).convert();
		}
	}
}