/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.swornpermissions.conversion;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import lombok.AllArgsConstructor;
import net.dmulloy2.swornpermissions.SwornPermissions;

/**
 * @author dmulloy2
 */

@AllArgsConstructor
public class ConversionHandler
{
	private final SwornPermissions plugin;

	public final List<String> supported = Arrays.asList(new String[]
	{
			"GroupManager", "PermissionsEx"
	});

	public final boolean needsConversion()
	{
		File plugins = getPluginsFolder();
		for (String plugin : supported)
		{
			if (new File(plugins, plugin).exists())
				return true;
		}

		return false;
	}

	public final void attemptConversion()
	{
		if (needsConversion())
			convert();
	}

	private final void convert()
	{
		File plugins = getPluginsFolder();
		File groupManager = new File(plugins, "GroupManager");
		if (groupManager.exists())
		{
			new GroupManagerConverter(groupManager, plugin).convert();
			return;
		}

		File pex = new File(plugins, "PermissionsEx");
		if (pex.exists())
		{
			File perms = new File(pex, "permissions.yml");
			new PermissionsExConverter(perms, plugin).convert();
			return;
		}
	}

	public final File getPluginsFolder()
	{
		File dataFolder = plugin.getDataFolder();
		dataFolder.mkdirs();

		return dataFolder.getParentFile();
	}
}