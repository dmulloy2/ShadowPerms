/**
 * (c) 2015 dmulloy2
 */
package net.dmulloy2.swornpermissions.conversion;

import java.io.File;

import net.dmulloy2.swornpermissions.SwornPermissions;
import net.dmulloy2.swornpermissions.data.DataHandler;
import net.dmulloy2.swornpermissions.data.backend.Backend;
import net.dmulloy2.swornpermissions.data.backend.Backend.BackendType;

import lombok.AllArgsConstructor;

/**
 * @author dmulloy2
 */

@AllArgsConstructor
public class ConversionHandler
{
	private final SwornPermissions plugin;

	public void fromOtherPlugin()
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

	public void switchUserBackend(SwornPermissions plugin, BackendType from, BackendType to)
	{
		if (from == to) return;

		Backend oldBackend = DataHandler.newBackend(from, plugin);
		Backend newBackend = DataHandler.newBackend(to, plugin);

		new BackendConverter(oldBackend, newBackend, plugin).convertUsers();
	}
}