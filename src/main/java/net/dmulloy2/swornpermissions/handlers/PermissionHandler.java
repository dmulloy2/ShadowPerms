/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.swornpermissions.handlers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;

import lombok.Getter;
import net.dmulloy2.swornpermissions.SwornPermissions;
import net.dmulloy2.swornpermissions.permissions.Group;
import net.dmulloy2.swornpermissions.permissions.ServerGroup;
import net.dmulloy2.swornpermissions.permissions.User;
import net.dmulloy2.swornpermissions.permissions.WorldGroup;
import net.dmulloy2.swornpermissions.types.Reloadable;
import net.dmulloy2.swornpermissions.util.Util;

import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.collect.Maps;

/**
 * @author dmulloy2
 */

@Getter
public class PermissionHandler implements Reloadable
{
	private Map<String, List<User>> users;
	private Map<String, Map<String, WorldGroup>> worldGroups;
	private Map<String, ServerGroup> serverGroups;
	private Map<String, Group> defaultGroups;

	private final SwornPermissions plugin;
	public PermissionHandler(SwornPermissions plugin)
	{
		this.plugin = plugin;
		this.users = Maps.newHashMap();
	}

	// ---- User Getters

	public final User getUser(String name)
	{
		return getUser(getDefaultWorld(), name);
	}

	public final User getUser(Player player)
	{
		return getUser(player.getWorld(), player);
	}

	public final User getUser(World world, String name)
	{
		if (world == null)
			world = getDefaultWorld();

		return getUser(world.getName(), name);
	}

	public final User getUser(World world, Player player)
	{
		if (! isValidPlayer(player))
			return null;

		if (world == null)
			world = getDefaultWorld();

		return getUser(world.getName(), player.getName());
	}

	// Root get user method... All getUser calls should be directed here
	public final User getUser(String world, String name)
	{
		if (world == null)
			world = getDefaultWorld().getName();

		world = plugin.getDataHandler().getUsersParent(world);

		// Attempt to grab from online users
		for (User user : users.get(world))
		{
			if (user.getName().equalsIgnoreCase(name) || user.getUniqueId().equals(name))
				return user;
		}

		User user = plugin.getDataHandler().loadUser(world, name);
		if (user == null)
			return null;

		// Only track online users
		if (user.isOnline())
			users.get(world).add(user);

		return user;
	}

	public final List<User> getUsers(World world)
	{
		return getUsers(world.getName());
	}

	public final List<User> getUsers(String world)
	{
		return users.get(world);
	}

	public final List<User> getAllUsers(World world)
	{
		return getAllUsers(world.getName());
	}

	public final List<User> getAllUsers(String world)
	{
		List<User> ret = new ArrayList<User>();

		ret.addAll(getUsers(world));
		ret.addAll(plugin.getDataHandler().loadAllUsers(world));

		return ret;
	}

	// ---- User utility methods

	public final void updateUsers()
	{
		for (List<User> list : users.values())
		{
			for (User user : list)
			{
				user.updatePermissions(true);
			}
		}
	}

	public final void removeUser(String name)
	{
		users.remove(name);
	}

	public final void moveUser(User user, World oldWorld, World newWorld)
	{
		users.get(oldWorld.getName()).remove(user);
		users.get(newWorld.getName()).add(user);
	}

	public final boolean isValidPlayer(Player player)
	{
		for (Player pl : plugin.getServer().getOnlinePlayers())
		{
			if (player.equals(pl))
				return true;
		}

		return false;
	}

	public final boolean isRegistered(String id)
	{
		return isRegistered(id, getDefaultWorld());
	}

	public final boolean isRegistered(String id, World world)
	{
		return isRegistered(id, world.getName());
	}

	public final boolean isRegistered(String id, String world)
	{
		world = plugin.getDataHandler().getUsersParent(world);

		for (User user : getUsers(world))
		{
			if (user.getName().equalsIgnoreCase(id) || user.getUniqueId().equalsIgnoreCase(id))
				return true;
		}

		return false;
	}

	// ---- Group Getters

	public final Group getGroup(String name)
	{
		return getGroup(getDefaultWorld(), name);
	}

	public final Group getGroup(World world, String name)
	{
		return getGroup(world.getName(), name);
	}

	// Root group getter method
	public final Group getGroup(String world, String name)
	{
		// Groups are stored lowercase
		name = name.toLowerCase();

		if (name.startsWith("s:"))
			return serverGroups.get(name);

		world = plugin.getDataHandler().getUsersParent(world);

		return worldGroups.get(world).get(name);
	}

	public Collection<WorldGroup> getGroups(World world)
	{
		return getGroups(world.getName());
	}

	public Collection<WorldGroup> getGroups(String world)
	{
		return worldGroups.get(world).values();
	}

	public Set<Group> getAllGroups()
	{
		Set<Group> ret = new HashSet<Group>();
		ret.addAll(serverGroups.values());
		for (Map<String, WorldGroup> groups : worldGroups.values())
		{
			ret.addAll(groups.values());
		}

		return ret;
	}

	// ---- Defaults

	public final Group getDefaultGroup()
	{
		return getDefaultGroup(getDefaultWorld());
	}

	public final Group getDefaultGroup(World world)
	{
		return getDefaultGroup(world.getName());
	}

	public final Group getDefaultGroup(String world)
	{
		world = plugin.getDataHandler().getUsersParent(world);

		return defaultGroups.get(world);
	}

	public final World getDefaultWorld()
	{
		return plugin.getServer().getWorlds().get(0);
	}

	// ---- Permission Utility Methods

	public final Permission getPermission(String node)
	{
		return plugin.getServer().getPluginManager().getPermission(node);
	}

	public final Set<Permission> getRegisteredPermissions()
	{
		return plugin.getServer().getPluginManager().getPermissions();
	}

	public final boolean hasPermission(CommandSender sender, net.dmulloy2.swornpermissions.types.Permission permission)
	{
		return permission == null || hasPermission(sender, getPermissionString(permission));
	}

	public final boolean hasPermission(CommandSender sender, String permission)
	{
		if (sender instanceof Player)
		{
			Player player = (Player) sender;
			return player.hasPermission(permission);
		}

		return true;
	}

	private final String getPermissionString(net.dmulloy2.swornpermissions.types.Permission permission)
	{
		return plugin.getName() + "." + permission.getNode().toLowerCase();
	}

	// ---- Cleanup

	public final void cleanupUsers()
	{
		cleanupUsers(0L);
	}

	public final void cleanupUsers(long delay)
	{
		if (delay <= 0 || plugin.isDisabling())
		{
			cleanupUsers0();
			return;
		}

		new BukkitRunnable()
		{
			@Override
			public void run()
			{
				cleanupUsers0();
			}
		}.runTaskLater(plugin, delay);
	}

	// Remove offline users
	private final void cleanupUsers0()
	{
		for (Entry<String, List<User>> entry : new HashMap<String, List<User>>(users).entrySet())
		{
			for (User user : entry.getValue())
			{
				if (! user.isOnline())
					users.get(entry.getKey()).remove(user);
			}
		}
	}

	// ---- Loading

	public final void loadGroups()
	{
		DataHandler data = plugin.getDataHandler();

		// Load server groups first
		FileConfiguration fc = data.getServerGroups();
		if (fc.isSet("groups"))
		{
			Map<String, Object> values = fc.getConfigurationSection("groups").getValues(false);
			for (Entry<String, Object> entry : values.entrySet())
			{
				try
				{
					String groupName = entry.getKey();
					if (! groupName.startsWith("s:"))
						groupName = "s:" + groupName;

					MemorySection section = (MemorySection) entry.getValue();
					ServerGroup group = new ServerGroup(plugin, groupName, section);
					serverGroups.put(groupName.toLowerCase(), group);
				}
				catch (Exception e)
				{
					plugin.getLogHandler().log(Level.SEVERE, Util.getUsefulStack(e, "loading server group " + entry.getKey()));
				}
			}
		}

		for (Entry<String, FileConfiguration> entry : data.getGroupConfigs().entrySet())
		{
			String world = entry.getKey();
			if (! worldGroups.containsKey(world))
			{
				worldGroups.put(world, new HashMap<String, WorldGroup>());
			}

			fc = entry.getValue();
			if (! fc.isSet("groups"))
			{
				plugin.getLogHandler().debug("Found 0 groups to load from world {0}!", world);
				continue;
			}

			Map<String, Object> values = fc.getConfigurationSection("groups").getValues(false);
			for (Entry<String, Object> entry1 : values.entrySet())
			{
				try
				{
					String groupName = entry1.getKey();
					MemorySection section = (MemorySection) entry1.getValue();
					WorldGroup group = new WorldGroup(plugin, groupName, world, section);
					worldGroups.get(world).put(groupName.toLowerCase(), group);

					if (group.isDefaultGroup())
						defaultGroups.put(world.toLowerCase(), group);
				}
				catch (Exception e)
				{
					plugin.getLogHandler().log(Level.SEVERE, Util.getUsefulStack(e, "loading world group " + entry1.getKey()));
				}
			}
		}
	}

	public final void registerWorlds()
	{
		for (World world : plugin.getServer().getWorlds())
		{
			registerWorld(world);
		}
	}

	public final void registerWorld(World world)
	{
		if (! users.containsKey(world.getName()))
			users.put(world.getName(), new ArrayList<User>());
	}

	@Override
	public void reload()
	{
		// ---- Initialize maps
		this.worldGroups = Maps.newHashMap();
		this.serverGroups = Maps.newHashMap();
		this.defaultGroups = Maps.newHashMap();

		// ---- Register Worlds
		this.registerWorlds();

		// ---- Load Groups
		this.loadGroups();

		// ---- Update Users
		this.updateUsers();
	}
}