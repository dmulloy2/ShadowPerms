/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.shadowperms.data;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import net.dmulloy2.shadowperms.ShadowPerms;
import net.dmulloy2.shadowperms.data.backend.Backend;
import net.dmulloy2.shadowperms.data.backend.Backend.BackendType;
import net.dmulloy2.shadowperms.data.backend.MySQLBackend;
import net.dmulloy2.shadowperms.data.backend.SQLiteBackend;
import net.dmulloy2.shadowperms.data.backend.YAMLBackend;
import net.dmulloy2.shadowperms.types.ServerGroup;
import net.dmulloy2.shadowperms.types.User;
import net.dmulloy2.shadowperms.types.WorldGroup;
import net.dmulloy2.swornapi.types.Reloadable;
import net.dmulloy2.swornapi.util.Util;

import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import lombok.Getter;

/**
 * @author dmulloy2
 */

@Getter
public final class DataHandler implements Reloadable
{
	public static DateFormat BACKUP_FORMAT = new SimpleDateFormat("yy-MM-dd_kk-mm");
	private static String DATA_YML = "data.yml";

	private Backend userBackend;
	private Backend groupBackend;

	private List<String> loadedWorlds;

	private YamlConfiguration serverGroups;

	private ShadowPerms plugin;
	public DataHandler(ShadowPerms plugin)
	{
		this.plugin = plugin;

		String userBackend = plugin.getConfig().getString("database.userType", "YAML");
		// String groupBackend = plugin.getConfig().getString("database.groupType", "YAML");

		BackendType userType = BackendType.find(userBackend);
		BackendType groupType = BackendType.YAML; // BackendType.find(groupBackend);

		Tuple<BackendType, BackendType> stored = previousBackends();
		if (stored != null && stored.first() != userType)
		{
			// Conversion time, boys
			// TODO bring this back
			// plugin.getConversionHandler().switchUserBackend(plugin, stored.getFirst(), userType);
		}

		// TODO Convert group backend

		this.userBackend = newBackend(userType, plugin);

		if (userType != groupType)
		{
			this.groupBackend = newBackend(groupType, plugin);
		}
		else
		{
			// Share a backend if they're the same
			this.groupBackend = this.userBackend;
		}

		plugin.getLogHandler().log("Using {0} for users and {0} for groups", userBackend, groupBackend);

		storeBackends(userType, groupType);

		this.scheduleSaveTask();
		this.reload();
	}

	public static Backend newBackend(BackendType type, ShadowPerms plugin)
	{
		return switch (type)
		{
			case MY_SQL -> new MySQLBackend(plugin);
			case SQL_LITE -> new SQLiteBackend(plugin);
			default -> new YAMLBackend(plugin);
		};
	}

	private Tuple<BackendType, BackendType> previousBackends()
	{
		File file = new File(plugin.getDataFolder(), DATA_YML);
		if (! file.exists())
			return null;

		YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
		if (! config.isSet("users"))
			return null;

		return new Tuple<>(BackendType.valueOf(config.getString("users")), BackendType.valueOf(config.getString("groups")));
	}

	private void storeBackends(BackendType users, BackendType groups)
	{
		File file = new File(plugin.getDataFolder(), DATA_YML);
		if (! file.exists())
		{
			try
			{
				file.createNewFile();
			} catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}

		YamlConfiguration config = new YamlConfiguration();
		config.set("users", users.name());
		config.set("groups", groups.name());

		try
		{
			config.save(file);
		} catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	// ---- I/O

	private void scheduleSaveTask()
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

	public void save()
	{
		long start = System.currentTimeMillis();
		plugin.getLogHandler().log("Saving users and groups...");

		saveUsers();
		saveGroups();

		plugin.getLogHandler().log("Saved! Took {0} ms!", System.currentTimeMillis() - start);
	}

	private void saveUsers()
	{
		for (String world : loadedWorlds)
		{
			if (! plugin.getMirrorHandler().areUsersMirrored(world))
			{
				try
				{
					userBackend.saveUsers(world);
				}
				catch (Throwable ex)
				{
					plugin.getLogHandler().log(Level.SEVERE, Util.getUsefulStack(ex, "saving users for world " + world));
				}
			}
		}

		plugin.getPermissionHandler().cleanupUsers(20L);
	}

	private void saveGroups()
	{
		for (String world : loadedWorlds)
		{
			if (! plugin.getMirrorHandler().areGroupsMirrored(world))
			{
				try
				{
					userBackend.saveGroups(world);
				}
				catch (Throwable ex)
				{
					plugin.getLogHandler().log(Level.SEVERE, Util.getUsefulStack(ex, "saving groups for world " + world));
				}
			}
		}

		saveServerGroups();
	}

	// ---- Loading
	
	public void loadWorld(World world)
	{
		loadWorld(world.getName());
	}

	public void loadWorld(String world)
	{
		world = world.toLowerCase();
		if (isWorldLoaded(world))
			return;

		try
		{
			userBackend.loadWorld(world);
			if (userBackend != groupBackend)
				groupBackend.loadWorld(world);

			loadedWorlds.add(world);
		}
		catch (Throwable ex)
		{
			plugin.getLogHandler().log(Level.SEVERE, Util.getUsefulStack(ex, "loading world: " + world));
		}
	}

	public boolean isWorldLoaded(String world)
	{
		return loadedWorlds.contains(world.toLowerCase());
	}

	public User loadUser(String world, String identifier)
	{
		try
		{
			world = plugin.getMirrorHandler().getUsersParent(world);
			return userBackend.loadUser(world, identifier);
		}
		catch (Throwable ex)
		{
			plugin.getLogHandler().log(Level.SEVERE, Util.getUsefulStack(ex, "loading user: " + identifier));
			return null;
		}
	}

	public User loadUser(Player player)
	{
		String world = player.getWorld().getName();
		world = plugin.getMirrorHandler().getUsersParent(world);

		try
		{
			return userBackend.loadUser(world, player);
		}
		catch (Throwable ex)
		{
			plugin.getLogHandler().log(Level.SEVERE, Util.getUsefulStack(ex, "loading user for {0} ", player.getName()));
			return new User(plugin, player, world);
		}
	}

	public void reloadUser(User user)
	{
		userBackend.reloadUser(user);
	}

	public List<User> loadAllUsers(String world)
	{
		world = plugin.getMirrorHandler().getUsersParent(world);

		List<User> ret = new ArrayList<User>();
		Set<String> keys = userBackend.getUsers(world);

		for (String key : keys)
		{
			if (plugin.getPermissionHandler().isRegistered(key, world))
				continue;

			User user = loadUser(world, key);
			if (user != null)
				ret.add(user);
		}

		return ret;
	}

	public Map<String, ServerGroup> loadServerGroups()
	{
		try
		{
			return groupBackend.loadServerGroups();
		}
		catch (Throwable ex)
		{
			plugin.getLogHandler().log(Level.SEVERE, Util.getUsefulStack(ex, "loading server groups"));
		}

		return new HashMap<>();
	}

	public Map<String, Map<String, WorldGroup>> loadWorldGroups()
	{
		try
		{
			return groupBackend.loadWorldGroups();
		}
		catch (Throwable ex)
		{
			plugin.getLogHandler().log(Level.SEVERE, Util.getUsefulStack(ex, "loading world groups"));
		}

		return new HashMap<>();
	}

	public void saveServerGroups()
	{
		try
		{
			groupBackend.saveServerGroups();
		}
		catch (Throwable ex)
		{
			plugin.getLogHandler().log(Level.SEVERE, Util.getUsefulStack(ex, "saving server groups"));
		}
	}

	public void backup(CommandSender sender)
	{
		save();
		
		plugin.getLogHandler().log("Backing up users and groups...");
		if (sender instanceof Player)
			sender.sendMessage(plugin.getPrefix() + "Backing up users and groups...");

		userBackend.backup(sender);
		if (groupBackend != userBackend)
			groupBackend.backup(sender);

		plugin.getLogHandler().log("Backup complete!");
		if (sender instanceof Player)
			sender.sendMessage(plugin.getPrefix() + "Backup complete!");
	}

	public List<String> listWorlds()
	{
		return groupBackend.listWorlds();
	}

	@Override
	public void reload()
	{
		userBackend.reload();
		if (userBackend != groupBackend)
			groupBackend.reload();

		// ---- Load Worlds
		this.loadedWorlds = new ArrayList<>();
		for (String world : listWorlds())
			loadWorld(world);

		// ---- Load Server Groups
		this.loadServerGroups();
	}
}