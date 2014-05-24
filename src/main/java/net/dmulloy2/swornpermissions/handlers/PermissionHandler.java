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
		// Attempt to match the player
		Player player = Util.matchPlayer(name);
		if (player != null)
			return getUser(player);

		return getUser(getDefaultWorld(), name);
	}

	public final User getUser(World world, String name)
	{
		if (world == null)
			world = getDefaultWorld();

		return getUser(world.getName(), name);
	}

	public final User getUser(Player player)
	{
		return getUser(player.getWorld(), player);
	}

	public final User getUser(World world, Player player)
	{
		if (! isValidPlayer(player))
			return null;

		if (world == null)
			world = getDefaultWorld();

		return getUser(world.getName(), player.getUniqueId().toString());
	}

	// Root get user method... All getUser calls should be directed here
	public final User getUser(String world, String name)
	{
		if (world == null)
			world = getDefaultWorld().getName();

		world = plugin.getMirrorHandler().getUsersParent(world);

		// Attempt to grab from online users
		for (User user : users.get(world))
		{
			if (user.getUniqueId().equals(name) || user.getName().equalsIgnoreCase(name))
				return user;
		}

		// Attempt to load the user
		User user = plugin.getDataHandler().loadUser(world, name);
		if (user == null)
			return null;

		// Only track online users
		if (user.isOnline())
			users.get(world.toLowerCase()).add(user);

		return user;
	}

	public final List<User> getUsers(World world)
	{
		return getUsers(world.getName());
	}

	public final List<User> getUsers(String world)
	{
		return users.get(world.toLowerCase());
	}

	public final List<User> getAllUsers(World world)
	{
		return getAllUsers(world.getName());
	}

	public final List<User> getAllUsers(String world)
	{
		world = world.toLowerCase();

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

	public final boolean areUsersDifferent(World oldWorld, World newWorld)
	{
		return areUsersDifferent(oldWorld.getName(), newWorld.getName());
	}

	public final boolean areUsersDifferent(String oldWorld, String newWorld)
	{
		oldWorld = plugin.getMirrorHandler().getUsersParent(oldWorld);
		newWorld = plugin.getMirrorHandler().getUsersParent(newWorld);

		return ! oldWorld.equalsIgnoreCase(newWorld);
	}

	public final User moveWorld(Player player, World oldWorld, World newWorld)
	{
		return moveWorld(player, oldWorld.getName(), newWorld.getName());
	}

	public final User moveWorld(Player player, String oldWorld, String newWorld)
	{
		oldWorld = plugin.getMirrorHandler().getUsersParent(oldWorld);
		newWorld = plugin.getMirrorHandler().getUsersParent(newWorld);

		User oldUser = getUser(oldWorld, player.getName());

		if (oldWorld.equalsIgnoreCase(newWorld))
		{
			// Nothing changed, return the old user
			return oldUser;
		}

		// Get the new user, update, and return
		User newUser = getUser(newWorld, player.getName());
		users.get(newUser).add(newUser);
		return newUser;
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
		world = plugin.getMirrorHandler().getUsersParent(world);
		world = world.toLowerCase();

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

		world = plugin.getMirrorHandler().getUsersParent(world);

		return worldGroups.get(world).get(name);
	}

	public Collection<WorldGroup> getGroups(World world)
	{
		return getGroups(world.getName());
	}

	public Collection<WorldGroup> getGroups(String world)
	{
		return worldGroups.get(world.toLowerCase()).values();
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

	// ---- Group Creation

	public ServerGroup createServerGroup(String name)
	{
		ServerGroup group = new ServerGroup(plugin, name);
		serverGroups.put(name.toLowerCase(), group);
		return group;
	}

	public WorldGroup createWorldGroup(String name, World world)
	{
		String worldName = plugin.getMirrorHandler().getGroupsParent(world);

		WorldGroup group = new WorldGroup(plugin, name, worldName);
		worldGroups.get(worldName).put(name.toLowerCase(), group);
		return group;
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
		world = plugin.getMirrorHandler().getUsersParent(world);
		world = world.toLowerCase();

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
			for (User user : new ArrayList<User>(entry.getValue()))
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
			for (String groupName : values.keySet())
			{
				try
				{
					MemorySection section = (MemorySection) values.get(groupName);

					if (! groupName.startsWith("s:"))
						groupName = "s:" + groupName;

					ServerGroup group = new ServerGroup(plugin, groupName, section);
					serverGroups.put(groupName.toLowerCase(), group);
				}
				catch (Throwable ex)
				{
					plugin.getLogHandler().log(Level.SEVERE, Util.getUsefulStack(ex, "loading server group " + groupName));
				}
			}
		}

		for (String world : data.getGroupConfigs().keySet())
		{
			world = world.toLowerCase();

			if (! worldGroups.containsKey(world))
			{
				worldGroups.put(world, new HashMap<String, WorldGroup>());
			}

			fc = data.getGroupConfigs().get(world);
			if (! fc.isSet("groups"))
			{
				plugin.getLogHandler().debug("Found 0 groups to load from world {0}!", world);
				continue;
			}

			// Load groups
			Map<String, Object> values = fc.getConfigurationSection("groups").getValues(false);
			for (String groupName : values.keySet())
			{
				try
				{
					MemorySection section = (MemorySection) values.get(groupName);
					WorldGroup group = new WorldGroup(plugin, groupName, world, section);
					worldGroups.get(world).put(groupName.toLowerCase(), group);

					if (group.isDefaultGroup())
						defaultGroups.put(world, group);
				}
				catch (Throwable ex)
				{
					plugin.getLogHandler().log(Level.SEVERE, Util.getUsefulStack(ex, "loading world group " + groupName));
				}
			}

			// Update groups
			for (WorldGroup group : worldGroups.get(world).values())
			{
				group.loadParentGroups();
				group.update();
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
		if (! plugin.getMirrorHandler().areUsersMirrored(world))
		{
			if (! users.containsKey(world.getName().toLowerCase()))
				users.put(world.getName().toLowerCase(), new ArrayList<User>());
		}
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