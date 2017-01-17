/**
 * (c) 2017 dmulloy2
 */
package net.dmulloy2.swornpermissions.conversion;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import net.dmulloy2.swornpermissions.SwornPermissions;
import net.dmulloy2.swornpermissions.data.backend.Backend;
import net.dmulloy2.swornpermissions.types.User;
import net.dmulloy2.util.Util;

import org.bukkit.World;

import lombok.RequiredArgsConstructor;

/**
 * @author dmulloy2
 */
@SuppressWarnings("deprecation")
@RequiredArgsConstructor
public class BackendConverter
{
	private final Backend oldBackend;
	private final Backend newBackend;

	private final SwornPermissions plugin;

	void convertUsers()
	{
		plugin.getLogHandler().log("Converting users from {0} to {1}...", oldBackend, newBackend);

		for (World bWorld : plugin.getServer().getWorlds())
		{
			String world = bWorld.getName().toLowerCase();
			plugin.getLogHandler().log("Converting world {0}...", world);

			try
			{
				oldBackend.reload();
				oldBackend.loadWorld(bWorld);
			}
			catch (Throwable ex)
			{
				throw new RuntimeException("Failed to load world " + world, ex);
			}

			// Avoid copying the same users multiple times
			if (plugin.getMirrorHandler().areUsersMirrored(world))
				continue;

			// Load all the users with the old backend
			Set<String> uuids = oldBackend.getUsers(world);
			List<User> users = new ArrayList<>(uuids.size());

			for (String uuid : uuids)
			{
				try
				{
					User user = oldBackend.loadUser(world, uuid);
					if (user != null) // Really shouldn't happen, but just in case
						users.add(user);
				}
				catch (Throwable ex)
				{
					plugin.getLogHandler().log(Level.WARNING, Util.getUsefulStack(ex, "loading user {0}", uuid));
				}
			}

			// Save them with the new
			plugin.getPermissionHandler().setUsers(world, users);

			try
			{
				newBackend.reload();
				newBackend.saveUsers(world);
			}
			catch (Throwable ex)
			{
				throw new RuntimeException("Failed to save users from world " + world, ex);
			}

			plugin.getPermissionHandler().setUsers(world, new ArrayList<User>());
		}

		plugin.getLogHandler().log("Conversion complete! Starting plugin...");
	}
}