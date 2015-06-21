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

import org.apache.commons.lang.Validate;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.configuration.MemorySection;
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

	private Map<String, YamlConfiguration> groupConfigs;
	private Map<String, YamlConfiguration> userConfigs;

	private YamlConfiguration serverGroups;

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

					YamlConfiguration config = new YamlConfiguration();
					config.load(file);

					for (User user : plugin.getPermissionHandler().getUsers(world))
					{
						try
						{
							if (user.shouldBeSaved())
							{
								config.createSection("users." + user.getSaveName(), user.serialize());
							}
							else
							{
								if (config.isSet("users." + user.getSaveName()))
									config.set("users." + user.getSaveName(), null);
							}
						}
						catch (Throwable ex)
						{
							plugin.getLogHandler().log(Level.SEVERE, Util.getUsefulStack(ex, "saving user " + user.getName()));
						}
					}

					config.save(file);
					userConfigs.put(world, config);
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

					YamlConfiguration config = new YamlConfiguration();
					config.load(file);

					List<WorldGroup> groups = plugin.getPermissionHandler().getGroups(world);
					if (groups != null)
					{
						for (Group group : plugin.getPermissionHandler().getGroups(world))
						{
							try
							{
								config.createSection("groups." + group.getSaveName(), group.serialize());
							}
							catch (Throwable ex)
							{
								plugin.getLogHandler().log(Level.SEVERE, Util.getUsefulStack(ex, "saving group " + group.getName()));
							}
						}

						config.save(file);
					}

					groupConfigs.put(world, config);
				}
				catch (Throwable ex)
				{
					plugin.getLogHandler().log(Level.SEVERE, Util.getUsefulStack(ex, "saving groups for world " + world));
				}
			}
		}

		saveServerGroups();
	}

	public final YamlConfiguration getUserConfig(World world)
	{
		return getUserConfig(world.getName());
	}

	public final YamlConfiguration getUserConfig(String world)
	{
		world = plugin.getMirrorHandler().getUsersParent(world);
		YamlConfiguration config = userConfigs.get(world);
		return config;
	}

	public final YamlConfiguration getGroupConfig(World world)
	{
		return getGroupConfig(world.getName());
	}

	public final YamlConfiguration getGroupConfig(String world)
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
				String defaultGroupWorld = mirrors.getDefaultGroupWorld();
				if (defaultGroupWorld != null && ! name.equals(defaultGroupWorld))
					mirrors.addGroupMirror(name);
				else
					copy(plugin.getResource("groups.yml"), groups);
			}

			if (groups.exists())
			{
				YamlConfiguration config = new YamlConfiguration();
				config.load(groups);

				groupConfigs.put(name, config);
			}

			File users = new File(folder, "users.yml");
			if (! users.exists())
			{
				String defaultUserWorld = mirrors.getDefaultUserWorld();
				if (defaultUserWorld != null && ! name.equals(defaultUserWorld))
					mirrors.addUserMirror(name);
				else
					users.createNewFile();
//					copy(plugin.getResource("users.yml"), users);
			}

			if (users.exists())
			{
				YamlConfiguration config = new YamlConfiguration();
				config.load(users);

				userConfigs.put(name, config);
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
			YamlConfiguration config = getUserConfig(world);
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
		YamlConfiguration config = getUserConfig(world);
		if (! config.isSet("users." + key))
			return new User(plugin, player, world);

		return new User(plugin, player, world, (MemorySection) config.get("users." + key));
	}

	public final List<User> loadAllUsers(String world)
	{
		world = plugin.getMirrorHandler().getUsersParent(world);

		List<User> ret = new ArrayList<User>();

		YamlConfiguration config = getUserConfig(world);
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

			YamlConfiguration config = new YamlConfiguration();
			config.load(file);

			this.serverGroups = config;
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

	private void copy(InputStream stream, File destination) throws IOException
	{
		Validate.notNull(stream, "stream cannot be null!");
		Validate.notNull(destination, "destination cannot be null!");

		try (Closer closer = new Closer())
		{
			if (! destination.exists())
				destination.createNewFile();

			closer.register(stream);

			OutputStream out = closer.register(new FileOutputStream(destination));

			int len;
			byte[] buf = new byte[1024];

			while ((len = stream.read(buf)) > 0)
			{
				out.write(buf, 0, len);
			}
		}
	}

	@Override
	public void reload()
	{
		// ---- Initialize Maps
		this.groupConfigs = new HashMap<>();
		this.userConfigs = new HashMap<>();

		// ---- Load Worlds
		this.loadedWorlds = new ArrayList<>();
		for (World world : plugin.getServer().getWorlds())
			loadWorld(world);

		// ---- Load Server Groups
		this.loadServerGroups();
	}
}