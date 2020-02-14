/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.shadowperms.handlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.dmulloy2.shadowperms.ShadowPerms;
import net.dmulloy2.types.Reloadable;

import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * @author dmulloy2
 */

public class MirrorHandler implements Reloadable
{
	private Map<String, List<String>> userMirrors;
	private Map<String, List<String>> groupMirrors;

	private boolean unifiedUsers;
	private String mainWorld;

	private String onlyUserWorld;
	private String defaultUserWorld;

	private String onlyGroupWorld;
	private String defaultGroupWorld;

	private final ShadowPerms plugin;
	public MirrorHandler(ShadowPerms plugin)
	{
		this.plugin = plugin;
		this.reload();
	}

	// ---- User Mirrors

	public final boolean areUsersMirrored(String world)
	{
		return ! world.equalsIgnoreCase(getUsersParent(world));
	}

	public final String getUsersParent(World world)
	{
		return getUsersParent(world.getName());
	}

	public final String getUsersParent(String world)
	{
		if (unifiedUsers) return mainWorld;
		if (onlyUserWorld != null) return onlyUserWorld;

		world = world.toLowerCase();

		for (Entry<String, List<String>> entry : userMirrors.entrySet())
		{
			List<String> children = entry.getValue();
			if (children.contains("*") || children.contains(world))
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
		if (unifiedUsers) return true;

		world1 = getUsersParent(world1);
		world2 = getUsersParent(world2);

		return world1.equals(world2);
	}

	public final String getDefaultUserWorld()
	{
		return unifiedUsers ? mainWorld : defaultUserWorld;
	}

	public final void addUserMirror(String parent, String mirrored)
	{
		parent = parent.toLowerCase();
		mirrored = mirrored.toLowerCase();

		if (! userMirrors.containsKey(parent))
			userMirrors.put(parent, new ArrayList<String>());

		userMirrors.get(parent).add(mirrored);
	}

	public final void addUserMirror(String mirrored)
	{
		addUserMirror(getDefaultUserWorld(), mirrored);
	}

	// ---- Group Mirrors

	public final boolean areGroupsMirrored(String world)
	{
		return ! world.equalsIgnoreCase(getGroupsParent(world));
	}

	public final String getGroupsParent(World world)
	{
		return getGroupsParent(world.getName());
	}

	public final String getGroupsParent(String world)
	{
		if (onlyGroupWorld != null)
			return onlyGroupWorld;

		world = world.toLowerCase();

		for (Entry<String, List<String>> entry : groupMirrors.entrySet())
		{
			List<String> children = entry.getValue();
			if (children.contains("*") || children.contains(world))
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
		world1 = getGroupsParent(world1);
		world2 = getGroupsParent(world2);

		return world1.equals(world2);
	}

	public final String getDefaultGroupWorld()
	{
		return defaultGroupWorld;
	}

	public final void addGroupMirror(String parent, String mirrored)
	{
		parent = parent.toLowerCase();
		mirrored = mirrored.toLowerCase();

		if (! groupMirrors.containsKey(parent))
			groupMirrors.put(parent, new ArrayList<String>());

		groupMirrors.get(parent).add(mirrored);
	}

	public final void addGroupMirror(String mirrored)
	{
		addGroupMirror(getDefaultGroupWorld(), mirrored);
	}

	// ---- Loading

	@SuppressWarnings("unchecked")
	private final void loadUserMirrors()
	{
		FileConfiguration config = plugin.getConfig();
		if (unifiedUsers = config.getBoolean("unifiedUsers", false))
		{
			World main = plugin.getServer().getWorlds().get(0);
			this.mainWorld = main.getName().toLowerCase();
			return;
		}

		if (config.isSet("userMirrors"))
		{
			Map<String, Object> values = config.getConfigurationSection("userMirrors").getValues(false);
			for (Entry<String, Object> entry : values.entrySet())
			{
				String parent = entry.getKey().toLowerCase();
				if (parent.equals("%main_world"))
				{
					World main = plugin.getServer().getWorlds().get(0);
					parent = main.getName().toLowerCase();
				}

				List<String> children = new ArrayList<String>();
				for (String child : (List<String>) entry.getValue())
					children.add(child.toLowerCase());

				// Default world
				if (children.contains("%undefined_worlds"))
				{
					defaultUserWorld = parent;
				}

				// Only world
				if (children.contains("*"))
				{
					onlyUserWorld = parent;
					defaultUserWorld = parent;
					userMirrors.clear();
					return;
				}

				userMirrors.put(parent.toLowerCase(), children);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private final void loadGroupMirrors()
	{
		FileConfiguration config = plugin.getConfig();

		if (config.isSet("groupMirrors"))
		{
			Map<String, Object> values = config.getConfigurationSection("groupMirrors").getValues(false);
			for (Entry<String, Object> entry : values.entrySet())
			{
				String parent = entry.getKey().toLowerCase();
				if (parent.equals("%main_world"))
				{
					World main = plugin.getServer().getWorlds().get(0);
					parent = main.getName().toLowerCase();
				}

				List<String> children = new ArrayList<String>();
				for (String child : (List<String>) entry.getValue())
					children.add(child.toLowerCase());

				if (children.contains("%undefined_worlds"))
				{
					defaultGroupWorld = parent;
				}

				if (children.contains("*"))
				{
					onlyGroupWorld = parent;
					defaultGroupWorld = parent;
					groupMirrors.clear();
					return;
				}

				groupMirrors.put(parent.toLowerCase(), children);
			}
		}
	}

	@Override
	public void reload()
	{
		// Initialize maps
		this.userMirrors = new HashMap<String, List<String>>();
		this.groupMirrors = new HashMap<String, List<String>>();

		// Clear variables
		this.onlyUserWorld = null;
		this.onlyGroupWorld = null;

		this.defaultUserWorld = null;
		this.defaultGroupWorld = null;

		// Load mirrors
		this.loadUserMirrors();
		this.loadGroupMirrors();
	}
}