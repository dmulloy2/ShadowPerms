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
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;

import net.dmulloy2.swornpermissions.SwornPermissions;
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
public class PermissionsExConverter
{
	private final String worldName;
	private final YamlConfiguration fc;
	private final Map<String, UUID> uuidCache;

	private final SwornPermissions plugin;
	public PermissionsExConverter(File file, SwornPermissions plugin)
	{
		this.plugin = plugin;
		this.uuidCache = Maps.newHashMap();
		this.fc = YamlConfiguration.loadConfiguration(file);
		this.worldName = plugin.getServer().getWorlds().get(0).getName();
	}

	public final void convert()
	{
		long start = System.currentTimeMillis();
		plugin.getLogHandler().log("Converting from PermissionsEx!");

		File worlds = new File(plugin.getDataFolder(), "worlds");
		if (! worlds.exists())
			worlds.mkdirs();

		File dir = new File(worlds, worldName);
		if (! dir.exists())
			dir.mkdirs();
		
		try
		{
			plugin.getLogHandler().log("Converting groups!");

			Map<String, WorldGroup> groups = loadGroupsFromFile();
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
			plugin.getLogHandler().log(Level.SEVERE, Util.getUsefulStack(ex, "converting groups"));
		}

		try
		{
			plugin.getLogHandler().log("Converting users!");

			Map<UUID, User> users = loadUsersFromFile();
			if (! users.isEmpty())
			{
				plugin.getLogHandler().log("Converting {0} users!", users.size());

				File saveTo = new File(dir, "users.yml");
				if (! saveTo.exists())
					saveTo.createNewFile();
	
				YamlConfiguration fc = YamlConfiguration.loadConfiguration(saveTo);
				for (Entry<UUID, User> entry : users.entrySet())
				{
					fc.set("users." + entry.getKey().toString(), entry.getValue().serialize());
				}
	
				fc.save(saveTo);
			}
		}
		catch (Throwable ex)
		{
			plugin.getLogHandler().log(Level.SEVERE, Util.getUsefulStack(ex, "converting users"));
		}

		// Clear cache
		uuidCache.clear();

		plugin.getLogHandler().log("Successfully converted from PermissionsEx! Took {0} ms!", System.currentTimeMillis() - start);
	}

	private final Map<String, WorldGroup> loadGroupsFromFile()
	{
		Map<String, WorldGroup> ret = new HashMap<String, WorldGroup>();

		if (! fc.isSet("groups"))
			return ret;

		Map<String, Object> values = fc.getConfigurationSection("groups").getValues(false);
		for (Entry<String, Object> entry : values.entrySet())
		{
			String name = entry.getKey();
			WorldGroup group = new WorldGroup(plugin, name, worldName);

			MemorySection section = (MemorySection) entry.getValue();
			group.loadFromDisk(section);

			group.setParentGroups(new HashSet<String>(section.getStringList("inheritance")));

			String prefix = section.getString("prefix", "");
			if (! prefix.isEmpty())
				group.setPrefix(prefix);

			ret.put(name, group);
		}

		return ret;
	}

	private ExecutorService e;

	private final Map<UUID, User> loadUsersFromFile()
	{
		Map<UUID, User> uuidMap = new HashMap<UUID, User>();
		Map<String, User> nameMap = new HashMap<String, User>();

		if (! fc.isSet("users"))
			return uuidMap;

		Map<String, Object> values = fc.getConfigurationSection("users").getValues(false);
		for (Entry<String, Object> entry : values.entrySet())
		{
			try
			{
				String name = entry.getKey();
				User user = new User(plugin, name);
	
				MemorySection section = (MemorySection) entry.getValue();

				List<String> groups = section.getStringList("group");
				if (! groups.isEmpty())
				{
					String group = groups.get(0);
					user.setGroupName(group);

					List<String> subGroups = groups.subList(1, groups.size());
					if (! subGroups.isEmpty())
						user.setSubGroupNames(new HashSet<String>(subGroups));
				}

				Map<String, Object> options = new HashMap<String, Object>();
				if (section.isSet("options"))
					options.putAll(section.getConfigurationSection("options").getValues(false));

				String prefix = section.getString("prefix", "");
				if (! prefix.isEmpty())
					options.put("prefix", prefix);

				String suffix = section.getString("suffix", "");
				if (! suffix.isEmpty())
					options.put("suffix", suffix);

				user.setOptions(options);

				List<String> permissions = section.getStringList("permissions");
				user.setPermissionNodes(new HashSet<String>(permissions));
	
				if (uuidCache.containsKey(name))
				{
					UUID uuid = uuidCache.get(name);
					user.setUniqueId(uuid);
					user.setLastKnownBy(name);
					uuidMap.put(uuid, user);
				}
				else
				{
					nameMap.put(name, user);
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
	
			e = Executors.newFixedThreadPool(3);
			List<Future<Map<String, UUID>>> results = e.invokeAll(fetchers);

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
}