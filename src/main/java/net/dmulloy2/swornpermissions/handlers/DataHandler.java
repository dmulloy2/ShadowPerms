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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import lombok.Getter;
import net.dmulloy2.io.Closer;
import net.dmulloy2.swornpermissions.SwornPermissions;
import net.dmulloy2.swornpermissions.types.Group;
import net.dmulloy2.swornpermissions.types.User;
import net.dmulloy2.swornpermissions.types.WorldGroup;
import net.dmulloy2.types.Reloadable;
import net.dmulloy2.util.Util;

import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * @author dmulloy2
 */

@Getter
public class DataHandler implements Reloadable
{
	private List<String> loadedWorlds;

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

	private final void scheduleSaveTask()
	{
		if (plugin.getConfig().getBoolean("autoSave.enabled"))
		{
			class AutoSaveTask extends BukkitRunnable
			{
				@Override
				public void run()
				{
					save();
				}
			}

			int interval = plugin.getConfig().getInt("autoSave.interval", 15) * 20 * 60;
			new AutoSaveTask().runTaskTimerAsynchronously(plugin, interval, interval);
		}
	}

	public final void save()
	{
		long start = System.currentTimeMillis();
		plugin.getLogHandler().log("Saving users and groups...");

		saveUsers();
		saveGroups();

		plugin.getLogHandler().log("Saved! Took {0} ms!", System.currentTimeMillis() - start);
	}

	private final void saveUsers()
	{
		for (String world : loadedWorlds)
		{
			if (! plugin.getMirrorHandler().areUsersMirrored(world))
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
						try
						{
							if (user.shouldBeSaved())
							{
								fc.createSection("users." + user.getSaveName(), user.serialize());
							}
							else
							{
								if (fc.isSet("users." + user.getSaveName()))
									fc.set("users." + user.getSaveName(), null);
							}
						}
						catch (Throwable ex)
						{
							plugin.getLogHandler().log(Level.SEVERE, Util.getUsefulStack(ex, "saving user " + user.getName()));
						}
					}

					fc.save(file);
					userConfigs.put(world, fc);
				}
				catch (Throwable ex)
				{
					plugin.getLogHandler().log(Level.SEVERE, Util.getUsefulStack(ex, "saving users for world " + world));
				}
			}
		}

		plugin.getPermissionHandler().cleanupUsers(20L);
	}

	private final void saveGroups()
	{
		for (String world : loadedWorlds)
		{
			if (! plugin.getMirrorHandler().areGroupsMirrored(world))
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
					List<WorldGroup> groups = plugin.getPermissionHandler().getGroups(world);
					if (groups != null)
					{
						for (Group group : plugin.getPermissionHandler().getGroups(world))
						{
							try
							{
								fc.createSection("groups." + group.getSaveName(), group.serialize());
							}
							catch (Throwable ex)
							{
								plugin.getLogHandler().log(Level.SEVERE, Util.getUsefulStack(ex, "saving group " + group.getName()));
							}
						}

						fc.save(file);
					}

					groupConfigs.put(world, fc);
				}
				catch (Throwable ex)
				{
					plugin.getLogHandler().log(Level.SEVERE, Util.getUsefulStack(ex, "saving groups for world " + world));
				}
			}
		}

		saveServerGroups();
	}

	public final FileConfiguration getUserConfig(World world)
	{
		return getUserConfig(world.getName());
	}

	public final FileConfiguration getUserConfig(String world)
	{
		world = plugin.getMirrorHandler().getUsersParent(world);
		return userConfigs.get(world);
	}

	public final FileConfiguration getGroupConfig(World world)
	{
		return getGroupConfig(world.getName());
	}

	public final FileConfiguration getGroupConfig(String world)
	{
		world = plugin.getMirrorHandler().getGroupsParent(world);
		return groupConfigs.get(world);
	}

	// ---- Loading

	public final void loadWorld(World world)
	{
		if (isWorldLoaded(world))
			return;

		try
		{
			File worlds = new File(plugin.getDataFolder(), "worlds");
			if (! worlds.exists())
				worlds.mkdirs();

			String name = world.getName().toLowerCase();
			File folder = new File(worlds, name);
			if (! folder.exists())
				folder.mkdirs();

			MirrorHandler mirrors = plugin.getMirrorHandler();

			File groups = new File(folder, "groups.yml");
			if (! groups.exists())
			{
				if (mirrors.areGroupsMirroredByDefault())
					mirrors.addGroupMirror(name);
				else
					copy(plugin.getResource("groups.yml"), groups);
			}

			if (groups.exists())
			{
				FileConfiguration fc = YamlConfiguration.loadConfiguration(groups);
				groupConfigs.put(name, fc);
			}

			File users = new File(folder, "users.yml");
			if (! users.exists())
			{
				if (mirrors.areUsersMirroredByDefault())
					mirrors.addUserMirror(name);
				else
					copy(plugin.getResource("users.yml"), users);
			}

			if (users.exists())
			{
				FileConfiguration fc = YamlConfiguration.loadConfiguration(users);
				userConfigs.put(name, fc);
			}

			loadedWorlds.add(name);
		}
		catch (Throwable ex)
		{
			plugin.getLogHandler().log(Level.SEVERE, Util.getUsefulStack(ex, "loading world: " + world.getName()));
		}
	}

	public final boolean isWorldLoaded(World world)
	{
		return isWorldLoaded(world.getName());
	}

	public final boolean isWorldLoaded(String world)
	{
		return loadedWorlds.contains(world.toLowerCase());
	}

	public final User loadUser(String world, String identifier)
	{
		try
		{
			OfflinePlayer player = Util.matchOfflinePlayer(identifier);
			if (player == null)
				return null;

			world = plugin.getMirrorHandler().getUsersParent(world);

			String key = player.getUniqueId().toString();
			FileConfiguration config = getUserConfig(world);
			if (! config.isSet("users." + key))
			{
				// New user
				return new User(plugin, player, world);
			}

			return new User(plugin, player, world, (MemorySection) config.get("users." + key));
		}
		catch (Throwable ex)
		{
			plugin.getLogHandler().log(Level.SEVERE, Util.getUsefulStack(ex, "loading user: " + identifier));
			return null;
		}
	}

	public final User loadUser(Player player)
	{
		String world = player.getWorld().getName();
		world = plugin.getMirrorHandler().getUsersParent(world);

		String key = player.getUniqueId().toString();
		FileConfiguration config = getUserConfig(world);
		if (! config.isSet("users." + key))
			return new User(plugin, player, world);

		return new User(plugin, player, world, (MemorySection) config.get("users." + key));
	}

	public final List<User> loadAllUsers(String world)
	{
		world = plugin.getMirrorHandler().getUsersParent(world);

		List<User> ret = new ArrayList<User>();

		FileConfiguration config = getUserConfig(world);
		Map<String, Object> values = config.getConfigurationSection("users").getValues(false);
		for (String id : values.keySet())
		{
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
		catch (Throwable ex)
		{
			plugin.getLogHandler().log(Level.SEVERE, Util.getUsefulStack(ex, "loading server groups"));
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
		catch (Throwable ex)
		{
			plugin.getLogHandler().log(Level.SEVERE, Util.getUsefulStack(ex, "saving server groups"));
		}
	}

	private final void copy(InputStream stream, File destination) throws IOException
	{
		if (! destination.exists())
			destination.createNewFile();

		Closer closer = new Closer();
		closer.register(stream);

		OutputStream out = closer.register(new FileOutputStream(destination));

		int len;
		byte[] buf = new byte[1024];

		while ((len = stream.read(buf)) > 0)
		{
			out.write(buf, 0, len);
		}

		closer.close();
	}

	@Override
	public void reload()
	{
		// ---- Initialize Maps
		this.groupConfigs = new HashMap<String, FileConfiguration>();
		this.userConfigs = new HashMap<String, FileConfiguration>();

		// ---- Load Worlds
		this.loadedWorlds = new ArrayList<String>();
		for (World world : plugin.getServer().getWorlds())
			loadWorld(world);

		// ---- Load Server Groups
		this.loadServerGroups();
	}
}