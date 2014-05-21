/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.swornpermissions.conversion;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;

import net.dmulloy2.swornpermissions.SwornPermissions;
import net.dmulloy2.swornpermissions.permissions.ServerGroup;
import net.dmulloy2.swornpermissions.permissions.User;
import net.dmulloy2.swornpermissions.permissions.WorldGroup;
import net.dmulloy2.swornpermissions.types.UUIDFetcher;
import net.dmulloy2.swornpermissions.util.Util;

import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

/**
 * @author dmulloy2
 */

@SuppressWarnings("deprecation") // Self-deprecation
public class GroupManagerConverter
{
	private final File file;
	private final Map<String, UUID> uuidCache;

	private final SwornPermissions plugin;
	public GroupManagerConverter(File file, SwornPermissions plugin)
	{
		this.file = file;
		this.plugin = plugin;
		this.uuidCache = Maps.newHashMap();
	}

	public final void convert()
	{
		long start = System.currentTimeMillis();
		plugin.getLogHandler().log("Converting from GroupManager!");

		plugin.getLogHandler().log("Converting global groups!");

		File globalGroups = new File(file, "globalgroups.yml");

		try
		{
			List<ServerGroup> serverGroups = loadServerGroupsFromFile(globalGroups);

			File saveTo = new File(plugin.getDataFolder(), "serverGroups.yml");
			if (! saveTo.exists())
				saveTo.createNewFile();

			YamlConfiguration fc = YamlConfiguration.loadConfiguration(saveTo);
			for (ServerGroup group : serverGroups)
			{
				fc.set("groups." + group.getSaveName(), group.serialize());
			}

			fc.save(saveTo);
		}
		catch (Throwable ex)
		{
			plugin.getLogHandler().log(Level.WARNING, Util.getUsefulStack(ex, "converting global groups"));
		}

		File worldsDir = new File(plugin.getDataFolder(), "worlds");
		if (! worldsDir.exists())
			worldsDir.mkdirs();

		File worlds = new File(file, "worlds");
		if (worlds.exists())
		{
			if (worlds.isDirectory())
			{
				for (File world : worlds.listFiles())
				{
					if (world.isDirectory())
					{
						String worldName = world.getName();
						File dir = new File(worldsDir, worldName.toLowerCase());
						if (! dir.exists())
							dir.mkdirs();

						try
						{
							plugin.getLogHandler().log("Converting groups from world {0}!", worldName);

							File groupsFile = new File(world, "groups.yml");
							Map<String, WorldGroup> groups = loadGroupsFromFile(groupsFile, worldName);
							if (! groups.isEmpty())
							{
								plugin.getLogHandler().log("Converting {0} groups!", groups.size());
								
								File saveTo = new File(dir, "groups.yml");
								if (! saveTo.exists())
									saveTo.createNewFile();
	
								YamlConfiguration fc = YamlConfiguration.loadConfiguration(saveTo);
								for (Entry<String, WorldGroup> entry : groups.entrySet())
								{
									fc.set("groups." + entry.getKey(), entry.getValue().serialize());
								}
	
								fc.save(saveTo);
							}
						}
						catch (Throwable ex)
						{
							plugin.getLogHandler().log(Level.WARNING, Util.getUsefulStack(ex, "converting groups from " + worldName));
						}

						try
						{
							plugin.getLogHandler().log("Converting users from world {0}!", worldName);

							File usersFile = new File(world, "users.yml");
							Map<UUID, User> users = loadUsersFromFile(usersFile);
							if (! users.isEmpty())
							{
								plugin.getLogHandler().log("Converting {0} users!", users.size());
	
								File saveTo = new File(dir, "users.yml");
								if (! saveTo.exists())
									saveTo.createNewFile();
	
								YamlConfiguration fc = YamlConfiguration.loadConfiguration(saveTo);
								for (Entry<UUID, User> entry : users.entrySet())
								{
									User user = entry.getValue();
									fc.set("users." + entry.getKey().toString(), user.serialize());
								}
	
								fc.save(saveTo);
							}
						}
						catch (Throwable ex)
						{
							plugin.getLogHandler().log(Level.WARNING, Util.getUsefulStack(ex, "converting users from " + worldName));
						}
					}
				}
			}
		}

		// Clear cache
		uuidCache.clear();

		plugin.getLogHandler().log("Successfully converted from GroupManager! Took {0} ms!", System.currentTimeMillis() - start);
	}

	private final Map<UUID, User> loadUsersFromFile(File file)
	{
		Map<UUID, User> uuidMap = new HashMap<UUID, User>();
		Map<String, User> nameMap = new HashMap<String, User>();

		YamlConfiguration fc = YamlConfiguration.loadConfiguration(file);
		if (! fc.isSet("users"))
			return uuidMap;

		Map<String, Object> values = fc.getConfigurationSection("users").getValues(false);
		for (Entry<String, Object> entry : values.entrySet())
		{
			try
			{
				String key = entry.getKey();
				MemorySection section = (MemorySection) entry.getValue();

				// Newer versions of GM convert to UUID
				String name = key.length() < 36 ? key : section.getString("lastname");
				String id = key.length() == 36 ? key : null;
				User user = new User(plugin, name);

				user.loadFromDisk(section); // Our system is similar to GM's

				if (section.isSet("info"))
					user.setOptions(section.getConfigurationSection("info").getValues(false));

				if (uuidCache.containsKey(name))
				{
					UUID uuid = uuidCache.get(name);
					user.setUniqueId(uuid);
					user.setLastKnownBy(name);
					uuidMap.put(uuid, user);
				}
				else
				{
					if (id != null)
					{
						UUID uuid = UUID.fromString(id);
						user.setUniqueId(uuid);
						user.setLastKnownBy(name);
						uuidMap.put(uuid, user);
						uuidCache.put(name, uuid);
					}
					else
					{
						nameMap.put(name, user);
					}
				}
			}
			catch (Throwable ex)
			{
				plugin.getLogHandler().log(Level.WARNING, Util.getUsefulStack(ex, "converting " + entry.getKey()));
			}
		}

		if (nameMap.isEmpty())
			return uuidMap;

		// Attempt to lookup UUIDs for new system

		try
		{
			List<String> names = new ArrayList<String>(nameMap.keySet());
			ImmutableList.Builder<List<String>> builder = ImmutableList.builder();
			int namesCopied = 0;
			while (namesCopied < names.size())
			{
				builder.add(ImmutableList.copyOf(names.subList(namesCopied, Math.min(namesCopied + 100, names.size()))));
				namesCopied += 100;
			}

			List<UUIDFetcher> fetchers = new ArrayList<UUIDFetcher>();
			for (List<String> namesList : builder.build())
			{
				fetchers.add(new UUIDFetcher(namesList));
			}

			ExecutorService service = Executors.newFixedThreadPool(3);
			List<Future<Map<String, UUID>>> results = service.invokeAll(fetchers);

			for (Future<Map<String, UUID>> result : results)
			{
				Map<String, UUID> uuids = result.get();
				uuidCache.putAll(uuids);
				for (Entry<String, UUID> entry : uuids.entrySet())
				{
					try
					{
						User user = nameMap.get(entry.getKey());
						UUID uuid = entry.getValue();
						user.setUniqueId(uuid);
						user.setLastKnownBy(entry.getKey());
						uuidMap.put(uuid, user);
					}
					catch (Throwable ex)
					{
						plugin.getLogHandler().log(Level.WARNING, "Failed to fetch UUID for " + entry.getKey());
					}
				}
			}
		}
		catch (Throwable ex)
		{
			plugin.getLogHandler().log(Level.WARNING, Util.getUsefulStack(ex, "converting to UUID-based lookups"));
		}

		return uuidMap;
	}

	private final Map<String, WorldGroup> loadGroupsFromFile(File file, String world)
	{
		Map<String, WorldGroup> ret = new HashMap<String, WorldGroup>();

		YamlConfiguration fc = YamlConfiguration.loadConfiguration(file);
		if (! fc.isSet("groups"))
			return ret;

		Map<String, Object> values = fc.getConfigurationSection("groups").getValues(false);
		for (Entry<String, Object> entry : values.entrySet())
		{
			String name = entry.getKey();
			WorldGroup group = new WorldGroup(plugin, name, world);

			MemorySection section = (MemorySection) entry.getValue();

			group.loadFromDisk(section);

			if (section.isSet("info"))
				group.setOptions(section.getConfigurationSection("info").getValues(false));

			if (section.isSet("inheritance"))
			{
				Set<String> parents = new HashSet<String>();
				for (String parent : section.getStringList("inheritance"))
				{
					parents.add(parent.replaceAll("g:", "s:"));
				}

				group.setParentGroups(parents);
			}

			ret.put(name, group);
		}

		return ret;
	}

	private final List<ServerGroup> loadServerGroupsFromFile(File file)
	{
		List<ServerGroup> ret = new ArrayList<ServerGroup>();
		YamlConfiguration fc = YamlConfiguration.loadConfiguration(file);
		Map<String, Object> values = fc.getConfigurationSection("groups").getValues(false);
		for (Entry<String, Object> entry : values.entrySet())
		{
			String name = entry.getKey().substring(2);
			MemorySection section = (MemorySection) entry.getValue();
			ServerGroup group = new ServerGroup(plugin, name, section);
			ret.add(group);
		}

		return ret;
	}
}