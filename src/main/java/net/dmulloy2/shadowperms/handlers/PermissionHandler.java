/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.shadowperms.handlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;

import net.dmulloy2.shadowperms.ShadowPerms;
import net.dmulloy2.shadowperms.data.DataHandler;
import net.dmulloy2.shadowperms.types.Group;
import net.dmulloy2.shadowperms.types.ServerGroup;
import net.dmulloy2.shadowperms.types.User;
import net.dmulloy2.shadowperms.types.WorldGroup;
import net.dmulloy2.types.IPermission;
import net.dmulloy2.types.Reloadable;
import net.dmulloy2.util.Util;

import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.scheduler.BukkitRunnable;

import lombok.Getter;

/**
 * @author dmulloy2
 */

@Getter
public class PermissionHandler extends net.dmulloy2.handlers.PermissionHandler implements Reloadable
{
	private ConcurrentMap<String, List<User>> users;
	private Map<String, Map<String, WorldGroup>> worldGroups;
	private Map<String, ServerGroup> serverGroups;
	private Map<String, Group> defaultGroups;

	private final ShadowPerms plugin;

	public PermissionHandler(ShadowPerms plugin)
	{
		super(plugin);
		this.plugin = plugin;
		this.users = new ConcurrentHashMap<>();
	}

	// ---- User Getters

	public final User getUser(Player player)
	{
		return getUser(plugin.getMirrorHandler().getUsersParent(player.getWorld()), player);
	}

	public final User getUser(String world, Player player)
	{
		try
		{
			// First, attempt to grab from online users
			String identifier = player.getUniqueId().toString();
			for (User user : users.get(world))
			{
				if (user.getUniqueId().equals(identifier))
					return user;
			}

			// Last, load the user
			User user = plugin.getDataHandler().loadUser(player);
			users.get(world).add(user);
			return user;
		}
		catch (Throwable ex)
		{
			plugin.getLogHandler().log(Level.SEVERE, Util.getUsefulStack(ex, "getting user for: " + player.getName()));
			return null;
		}
	}

	public final User getUser(String world, OfflinePlayer player)
	{
		return getUser(world, player.getUniqueId().toString());
	}

	public final User getUser(String world, String identifier)
	{
		try
		{
			if (world == null)
				world = getDefaultWorld().getName();

			world = plugin.getMirrorHandler().getUsersParent(world);

			// First, attempt to match the player
			Player player = Util.matchPlayer(identifier);
			if (player != null)
				return getUser(world, player);

			// Then, attempt to grab from online users
			for (User user : users.get(world))
			{
				if (user.matches(identifier))
					return user;
			}

			// Finally, attempt to load the user
			User user = plugin.getDataHandler().loadUser(world, identifier);
			if (user == null)
				return null;

			users.get(world).add(user);
			return user;
		}
		catch (Throwable ex)
		{
			plugin.getLogHandler().log(Level.SEVERE, Util.getUsefulStack(ex, "getting user: " + identifier));
			return null;
		}
	}

	public final List<User> getUsers()
	{
		List<User> ret = new ArrayList<>();

		for (List<User> list : users.values())
			ret.addAll(list);

		return ret;
	}

	public final List<User> getUsers(String world)
	{
		return users.get(world.toLowerCase());
	}

	@Deprecated
	public final void setUsers(String world, List<User> users)
	{
		this.users.put(world, users);
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

	public final Group getGroup(World world, String name)
	{
		return getGroup(world.getName(), name);
	}

	public final Group getGroup(String world, String name)
	{
		world = plugin.getMirrorHandler().getGroupsParent(world);
		return getGroupRaw(world, name);
	}

	// Root group getter method
	// Does not take mirroring into account
	public final Group getGroupRaw(String world, String name)
	{
		// Groups are stored lowercase
		name = name.toLowerCase();

		if (name.startsWith("s:"))
			return serverGroups.get(name);

		world = world.toLowerCase();

		if (!worldGroups.containsKey(world))
			return null;

		return worldGroups.get(world).get(name);
	}

	public List<WorldGroup> getGroups(World world)
	{
		return getGroups(world.getName());
	}

	public List<WorldGroup> getGroups(String world)
	{
		Map<String, WorldGroup> groups = worldGroups.get(world.toLowerCase());
		if (groups != null)
			return new ArrayList<>(groups.values());

		return new ArrayList<>();
	}

	public List<String> getGroupNames(String world)
	{
		Map<String, WorldGroup> groups = worldGroups.get(world.toLowerCase());
		if (groups != null)
			return new ArrayList<>(groups.keySet());

		return new ArrayList<>();
	}


	public List<Group> getAllGroups()
	{
		List<Group> ret = new ArrayList<>();

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

	public void addWorldGroup(String world, WorldGroup group)
	{
		world = plugin.getMirrorHandler().getGroupsParent(world);
		worldGroups.get(world).put(group.getName().toLowerCase(), group);

		if (group.isDefaultGroup())
			defaultGroups.put(world, group);
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
		world = plugin.getMirrorHandler().getGroupsParent(world);
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

	public final Set<Permission> getPermissions()
	{
		return plugin.getServer().getPluginManager().getPermissions();
	}

	// Remove offline users
	public final void cleanupUsers(long delay)
	{
		if (plugin.isDisabling() || delay <= 0)
		{
			try
			{
				long start = System.currentTimeMillis();
				plugin.getLogHandler().log("Cleaning up users...");

				for (List<User> list : users.values())
				{
					Iterator<User> iter = list.iterator();
					while (iter.hasNext())
					{
						if (! iter.next().isOnline())
							iter.remove();
					}
				}

				plugin.getLogHandler().log("Finished cleaning up users. Took {0} ms!", System.currentTimeMillis() - start);
			}
			catch (Throwable ex)
			{
				plugin.getLogHandler().log(Level.WARNING, Util.getUsefulStack(ex, "cleaning up users"));
			}
		}
		else
		{
			new BukkitRunnable()
			{
				@Override
				public void run()
				{
					cleanupUsers(0);
				}
			}.runTaskLater(plugin, delay);
		}
	}

	// ---- Loading

	public final void loadGroups()
	{
		DataHandler data = plugin.getDataHandler();
		this.serverGroups = data.loadServerGroups();
		this.worldGroups = data.loadWorldGroups();

		worldGroups.forEach((world, groupMap) -> groupMap.forEach((name, group) -> group.loadParentGroups()));
	}

	public final void markDefault(String world, WorldGroup group)
	{
		this.defaultGroups.put(world.toLowerCase(), group);
	}

	public final void registerWorlds()
	{
		for (World world : plugin.getServer().getWorlds())
			registerWorld(world);
	}

	public final void registerWorld(World world)
	{
		if (! users.containsKey(world.getName().toLowerCase()))
			users.put(world.getName().toLowerCase(), new ArrayList<User>());
	}

	public final void load()
	{
		// ---- Initialize maps
		this.worldGroups = new HashMap<>();
		this.serverGroups = new HashMap<>();
		this.defaultGroups = new HashMap<>();

		// ---- Register Worlds
		this.registerWorlds();

		// ---- Load Groups
		this.loadGroups();

		// ---- Update Users
		this.updateUsers();
	}

	public final void update()
	{
		// Update server groups
		for (ServerGroup group : serverGroups.values())
		{
			group.updatePermissions(true, false);
		}

		// Update world groups
		for (Map<String, WorldGroup> groups : worldGroups.values())
		{
			for (WorldGroup group : groups.values())
				group.updatePermissions(true, false);
		}

		// Update users
		updateUsers();
	}

	private final void updateUsers()
	{
		for (List<User> list : users.values())
		{
			for (User user : list)
				user.updatePermissions(true);
		}
	}

	@Override
	public void reload()
	{
		// ---- Re-initialize maps
		this.worldGroups = new HashMap<>();
		this.serverGroups = new HashMap<>();
		this.defaultGroups = new HashMap<>();

		// ---- Reload Groups
		this.loadGroups();

		// ---- Reload users
		this.reloadUsers();
	}

	private final void reloadUsers()
	{
		for (List<User> list : users.values())
		{
			for (User user : list)
				user.reload();
		}
	}
}