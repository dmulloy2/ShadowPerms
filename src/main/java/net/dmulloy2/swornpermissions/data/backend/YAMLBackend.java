/**
 * (c) 2017 dmulloy2
 */
package net.dmulloy2.swornpermissions.data.backend;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;

import net.dmulloy2.io.Closer;
import net.dmulloy2.swornpermissions.SwornPermissions;
import net.dmulloy2.swornpermissions.handlers.MirrorHandler;
import net.dmulloy2.swornpermissions.types.Group;
import net.dmulloy2.swornpermissions.types.ServerGroup;
import net.dmulloy2.swornpermissions.types.User;
import net.dmulloy2.swornpermissions.types.WorldGroup;
import net.dmulloy2.util.Util;

import org.apache.commons.lang.Validate;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * @author dmulloy2
 */
public class YAMLBackend implements Backend
{
	private Map<String, YamlConfiguration> userConfigs;
	private Map<String, YamlConfiguration> groupConfigs;
	private YamlConfiguration serverGroups;

	private final SwornPermissions plugin;

	public YAMLBackend(SwornPermissions plugin)
	{
		this.plugin = plugin;
	}

	@Override
	public void saveUsers(String world) throws Exception
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

	@Override
	public void saveGroups(String world) throws Exception
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

	@Override
	public void saveServerGroups() throws Exception
	{
		File file = new File(plugin.getDataFolder(), "serverGroups.yml");
		if (! file.exists())
			file.createNewFile();

		serverGroups.save(file);
	}

	@Override
	public void loadWorld(World world) throws Exception
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
		}

		if (users.exists())
		{
			YamlConfiguration config = new YamlConfiguration();
			config.load(users);

			userConfigs.put(name, config);
		}
	}

	@Override
	public Map<String, ServerGroup> loadServerGroups() throws Exception
	{
		Map<String, ServerGroup> ret = new HashMap<>();

		File file = new File(plugin.getDataFolder(), "serverGroups.yml");
		if (! file.exists())
			file.createNewFile();

		YamlConfiguration config = new YamlConfiguration();
		config.load(file);

		this.serverGroups = config;

		if (config.isSet("groups"))
		{
			Map<String, Object> values = config.getConfigurationSection("groups").getValues(false);
			for (Entry<String, Object> entry : values.entrySet())
			{
				String groupName = entry.getKey();

				try
				{
					if (! groupName.startsWith("s:"))
						groupName = "s:" + groupName;

					ServerGroup group = new ServerGroup(plugin, groupName, (MemorySection) entry.getValue());
					ret.put(groupName.toLowerCase(), group);
				}
				catch (Throwable ex)
				{
					plugin.getLogHandler().log(Level.SEVERE, Util.getUsefulStack(ex, "loading server group " + groupName));
				}
			}
		}

		return ret;
	}

	@Override
	public Map<String, Map<String, WorldGroup>> loadWorldGroups() throws Exception
	{
		Map<String, Map<String, WorldGroup>> ret = new HashMap<>();

		for (Entry<String, YamlConfiguration> entry : groupConfigs.entrySet())
		{
			String world = entry.getKey().toLowerCase();
			if (! ret.containsKey(world))
				ret.put(world, new HashMap<String, WorldGroup>());

			YamlConfiguration config = entry.getValue();
			if (! config.isSet("groups"))
			{
				plugin.getLogHandler().debug("Found 0 groups to load from world {0}!", world);
				continue;
			}

			// Load groups
			Map<String, Object> values = config.getConfigurationSection("groups").getValues(false);
			for (Entry<String, Object> entry1 : values.entrySet())
			{
				String groupName = entry1.getKey();

				try
				{
					WorldGroup group = new WorldGroup(plugin, groupName, world, (MemorySection) entry1.getValue());
					ret.get(world).put(groupName.toLowerCase(), group);
					if (group.isDefaultGroup())
						plugin.getPermissionHandler().markDefault(world, group);
				}
				catch (Throwable ex)
				{
					plugin.getLogHandler().log(Level.SEVERE, Util.getUsefulStack(ex, "loading world group " + groupName));
				}
			}

			// Load parent groups
			for (WorldGroup group : ret.get(world).values())
				group.loadParentGroups();
		}

		return ret;
	}

	@Override
	public User loadUser(String world, OfflinePlayer player)
	{
		String key = player.getUniqueId().toString();
		YamlConfiguration config = userConfigs.get(world);
		if (! config.isSet("users." + key))
			return new User(plugin, player, world); // new User

		return new User(plugin, player, world, (MemorySection) config.get("users." + key));
	}

	@Override
	public User loadUser(String world, String key)
	{
		OfflinePlayer player = Util.matchOfflinePlayer(key);
		if (player != null)
			return loadUser(world, player);

		YamlConfiguration config = userConfigs.get(world);
		if (! config.isSet("users." + key))
			return null; // Couldn't find 'em

		return new User(plugin, null, world, (MemorySection) config.get("users." + key));
	}

	@Override
	public void reloadUser(User user)
	{
		String world =  plugin.getMirrorHandler().getUsersParent(user.getWorld());
		String uniqueId = user.getUniqueId();

		YamlConfiguration users = userConfigs.get(world);
		if (users.isSet("users." + uniqueId))
			user.loadFromDisk((MemorySection) users.get("users." + uniqueId));
	}

	@Override
	public Set<String> getUsers(String world)
	{
		YamlConfiguration config = userConfigs.get(world);
		return config.getConfigurationSection("users").getKeys(false);
	}

	@Override
	public void reload()
	{
		this.userConfigs = new HashMap<>();
		this.groupConfigs = new HashMap<>();
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
	public String toString()
	{
		return "YAML";
	}
}
