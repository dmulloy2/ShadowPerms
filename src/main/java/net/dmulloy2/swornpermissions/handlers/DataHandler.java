/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.swornpermissions.handlers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import lombok.Getter;
import net.dmulloy2.swornpermissions.SwornPermissions;
import net.dmulloy2.swornpermissions.permissions.Group;
import net.dmulloy2.swornpermissions.permissions.OfflineUser;
import net.dmulloy2.swornpermissions.permissions.User;
import net.dmulloy2.swornpermissions.types.Reloadable;
import net.dmulloy2.swornpermissions.util.Util;

import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.collect.Maps;

/**
 * @author dmulloy2
 */

@Getter
public class DataHandler implements Reloadable
{
	private List<String> loadedWorlds;

	private Map<String, List<String>> groupMirrors;
	private Map<String, List<String>> userMirrors;

	private Map<String, FileConfiguration> groupConfigs;
	private Map<String, FileConfiguration> userConfigs;

	private FileConfiguration serverGroups;

	private final SwornPermissions plugin;
	public DataHandler(SwornPermissions plugin)
	{
		this.plugin = plugin;
		this.scheduleSaveTask();
		this.reload();
	}

	// ---- I/O

	public final void scheduleSaveTask()
	{
		if (! plugin.getConfig().getBoolean("autoSave.enabled"))
			return;

		int interval = plugin.getConfig().getInt("autoSave.interval", 15) * 20 * 60;

		new BukkitRunnable()
		{
			@Override
			public void run()
			{
				save();
			}
		}.runTaskTimer(plugin, interval, interval);
	}

	public final void save()
	{
		long start = System.currentTimeMillis();
		plugin.getLogHandler().log("Saving users and groups...");

		saveUsers();
		saveGroups();

		plugin.getLogHandler().log("Saved! Took {0} ms!", System.currentTimeMillis() - start);
	}

	public final void saveUsers()
	{
		for (String world : loadedWorlds)
		{
			if (! areUsersMirrored(world))
			{
				try
				{
					File worlds = new File(plugin.getDataFolder(), "worlds");
					if (! worlds.exists())
						worlds.mkdirs();

					File worldFile = new File(worlds, world);
					if (! worldFile.exists())
						worldFile.mkdirs();

					File file = new File(worldFile, "users.yml");
					if (! file.exists())
						file.createNewFile();

					FileConfiguration fc = YamlConfiguration.loadConfiguration(file);
					for (User user : plugin.getPermissionHandler().getUsers(world))
					{
						if (user.shouldBeSaved())
						{
							fc.set("users." + user.getSaveName(), user.serialize());
						}
						else
						{
							if (fc.isSet("users." + user.getSaveName()))
								fc.set("users." + user.getSaveName(), null);
						}
					}

					fc.save(file);
					userConfigs.put(world, fc);
				}
				catch (Exception e)
				{
					plugin.getLogHandler().log(Level.SEVERE, Util.getUsefulStack(e, "saving users for world " + world));
				}
			}
		}

		plugin.getPermissionHandler().cleanupUsers(20L);
	}

	public final void saveGroups()
	{
		for (String world : loadedWorlds)
		{
			if (! areGroupsMirrored(world))
			{
				try
				{
					File worlds = new File(plugin.getDataFolder(), "worlds");
					if (! worlds.exists())
						worlds.mkdirs();

					File worldFile = new File(worlds, world);
					if (! worldFile.exists())
						worldFile.mkdirs();

					File file = new File(worldFile, "groups.yml");
					if (! file.exists())
						file.createNewFile();

					FileConfiguration fc = YamlConfiguration.loadConfiguration(file);
					for (Group group : plugin.getPermissionHandler().getGroups(world))
					{
						fc.set("groups." + group.getSaveName(), group.serialize());
					}

					fc.save(file);
					groupConfigs.put(world, fc);
				}
				catch (Exception e)
				{
					plugin.getLogHandler().log(Level.SEVERE, Util.getUsefulStack(e, "saving groups for world " + world));
				}
			}
		}
	}

	public final FileConfiguration getUserConfig(World world)
	{
		return getUserConfig(world.getName());
	}

	public final FileConfiguration getUserConfig(String world)
	{
		String parent = getUsersParent(world);
		if (parent != null)
			world = parent;

		return userConfigs.get(world);
	}

	public final FileConfiguration getGroupConfig(World world)
	{
		return getGroupConfig(world.getName());
	}

	public final FileConfiguration getGroupConfig(String world)
	{
		String parent = getGroupsParent(world);
		if (parent != null)
			world = parent;

		return groupConfigs.get(world);
	}

	// ---- User Mirrors

	public final boolean areUsersMirrored(World world)
	{
		return areUsersMirrored(world.getName());
	}

	public final boolean areUsersMirrored(String world)
	{
		return ! getUsersParent(world).equals(world);
	}

	public final String getUsersParent(World world)
	{
		return getUsersParent(world.getName());
	}

	public final String getUsersParent(String world)
	{
		for (Entry<String, List<String>> entry : userMirrors.entrySet())
		{
			if (entry.getValue().contains(world))
				return entry.getKey();
		}

		return world;
	}

	public final boolean areUsersLinked(World world1, World world2)
	{
		return areUsersLinked(world1.getName(), world2.getName());
	}

	public final boolean areUsersLinked(String world1, String world2)
	{
		if (userMirrors.containsKey(world1))
		{
			return userMirrors.get(world1).contains(world2);
		}

		if (userMirrors.containsKey(world2))
		{
			return userMirrors.get(world2).contains(world1);
		}

		return false;
	}

	// ---- Group Mirrors

	public final boolean areGroupsMirrored(World world)
	{
		return areGroupsMirrored(world.getName());
	}

	public final boolean areGroupsMirrored(String world)
	{
		return ! getGroupsParent(world).equals(world);
	}

	public final String getGroupsParent(World world)
	{
		return getGroupsParent(world.getName());
	}

	public final String getGroupsParent(String world)
	{
		for (Entry<String, List<String>> entry : groupMirrors.entrySet())
		{
			if (entry.getValue().contains(world))
				return entry.getKey();
		}

		return world;
	}

	public final boolean areGroupsLinked(World world1, World world2)
	{
		return areGroupsLinked(world1.getName(), world2.getName());
	}

	public final boolean areGroupsLinked(String world1, String world2)
	{
		if (groupMirrors.containsKey(world1))
		{
			return groupMirrors.get(world1).contains(world2);
		}

		if (groupMirrors.containsKey(world2))
		{
			return groupMirrors.get(world2).contains(world1);
		}

		return false;
	}

	// ---- Loading

	@SuppressWarnings("unchecked")
	public final void loadMirrors()
	{
		FileConfiguration config = plugin.getConfig();
		if (config.isSet("userMirrors"))
		{
			Map<String, Object> values = config.getConfigurationSection("userMirrors").getValues(false);
			for (Entry<String, Object> entry : values.entrySet())
			{
				String parent = entry.getKey();
				List<String> children = (List<String>) entry.getValue();
				userMirrors.put(parent, children);
			}
		}

		if (config.isSet("groupMirrors"))
		{
			Map<String, Object> values = config.getConfigurationSection("groupMirrors").getValues(false);
			for (Entry<String, Object> entry : values.entrySet())
			{
				String parent = entry.getKey();
				List<String> children = (List<String>) entry.getValue();
				groupMirrors.put(parent, children);
			}
		}
	}

	public final void loadWorld(World world)
	{
		if (isWorldLoaded(world) || areGroupsMirrored(world))
			return;

		try
		{
			File dir = new File(plugin.getDataFolder(), "worlds");
			if (! dir.exists())
				dir.mkdirs();

			File worldFolder = new File(dir, world.getName());
			if (! worldFolder.exists())
				worldFolder.mkdirs();

			File groupsFile = new File(worldFolder, "groups.yml");
			if (! groupsFile.exists())
				copy(plugin.getResource("groups.yml"), groupsFile);

			FileConfiguration groups = YamlConfiguration.loadConfiguration(groupsFile);
			groupConfigs.put(world.getName(), groups);

			File usersFile = new File(worldFolder, "users.yml");
			if (! usersFile.exists())
				usersFile.createNewFile();

			FileConfiguration users = YamlConfiguration.loadConfiguration(usersFile);
			userConfigs.put(world.getName(), users);

			loadedWorlds.add(world.getName());
		}
		catch (Exception e)
		{
			plugin.getLogHandler().log(Level.SEVERE, Util.getUsefulStack(e, "loading world " + world.getName()));
		}
	}

	public final boolean isWorldLoaded(World world)
	{
		return loadedWorlds.contains(world.getName());
	}

	public final User loadUser(String world, String name)
	{
		OfflinePlayer op = Util.matchOfflinePlayer(name);
		if (op == null)
			return null;

		world = getUsersParent(world);

		String key = op.getUniqueId().toString();
		FileConfiguration config = getUserConfig(world);
		if (! config.isSet("users." + key))
		{
			if (op.isOnline())
				return new User(plugin, op.getPlayer());
			else
				return new OfflineUser(plugin, op);
		}

		if (op.isOnline())
			return new User(plugin, op.getPlayer(), (MemorySection) config.get("users." + key));
		else
			return new OfflineUser(plugin, op, (MemorySection) config.get("users." + key));
	}

	public final User loadUser(Player player)
	{
		String world = player.getWorld().getName();

		world = getUsersParent(world);

		String key = player.getUniqueId().toString();
		FileConfiguration config = getUserConfig(world);
		if (! config.isSet("users." + key))
		{
			return new User(plugin, player);
		}

		return new User(plugin, player, (MemorySection) config.get("users." + key));
	}

	public final List<User> loadAllUsers(String world)
	{
		world = getUsersParent(world);

		List<User> ret = new ArrayList<User>();

		FileConfiguration config = getUserConfig(world);
		Map<String, Object> values = config.getConfigurationSection("users").getValues(false);
		for (Entry<String, Object> entry : values.entrySet())
		{
			String id = entry.getKey();
			if (plugin.getPermissionHandler().isRegistered(id, world))
				continue;

			User user = loadUser(world, id);
			if (user != null)
				ret.add(user);
		}

		return ret;
	}

	private final void loadServerGroups()
	{
		try
		{
			File file = new File(plugin.getDataFolder(), "serverGroups.yml");
			if (! file.exists())
				file.createNewFile();

			this.serverGroups = YamlConfiguration.loadConfiguration(file);
		}
		catch (Exception e)
		{
			plugin.getLogHandler().log(Level.SEVERE, Util.getUsefulStack(e, "loading server groups"));
		}
	}

	public final void saveServerGroups()
	{
		try
		{
			File file = new File(plugin.getDataFolder(), "serverGroups.yml");
			if (! file.exists())
				file.createNewFile();

			serverGroups.save(file);
		}
		catch (Exception e)
		{
			plugin.getLogHandler().log(Level.SEVERE, Util.getUsefulStack(e, "saving server groups"));
		}
	}

	private final void copy(InputStream stream, File destination) throws IOException
	{
		if (! destination.exists())
			destination.createNewFile();

		OutputStream out = new FileOutputStream(destination);

		int len;
		byte[] buf = new byte[1024];

		while ((len = stream.read(buf)) > 0)
		{
			out.write(buf, 0, len);
		}

		try
		{
			out.close();
			stream.close();
		} catch (Throwable ex) { }
	}

	@Override
	public void reload()
	{
		// ---- Initialize Maps
		this.groupMirrors = Maps.newHashMap();
		this.userMirrors = Maps.newHashMap();

		this.groupConfigs = Maps.newHashMap();
		this.userConfigs = Maps.newHashMap();

		// ---- Load Mirrors
		this.loadMirrors();

		// ---- Load Worlds
		this.loadedWorlds = new ArrayList<String>();
		for (World world : plugin.getServer().getWorlds())
		{
			loadWorld(world);
		}

		// ---- Load Server Groups
		this.loadServerGroups();
	}
}