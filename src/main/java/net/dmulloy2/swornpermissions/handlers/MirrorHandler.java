/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.swornpermissions.handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.dmulloy2.swornpermissions.SwornPermissions;
import net.dmulloy2.swornpermissions.types.Reloadable;

import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

import com.google.common.collect.Maps;

/**
 * @author dmulloy2
 */

public class MirrorHandler implements Reloadable
{
	private Map<String, List<String>> userMirrors;
	private Map<String, List<String>> groupMirrors;

	private String onlyUserWorld;
	private String defaultUserWorld;

	private String onlyGroupWorld;
	private String defaultGroupWorld;

	private final SwornPermissions plugin;
	public MirrorHandler(SwornPermissions plugin)
	{
		this.plugin = plugin;
		this.reload();
	}

	// ---- User Mirrors

	public final boolean areUsersMirrored(World world)
	{
		return areUsersMirrored(world.getName());
	}

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
		if (onlyUserWorld != null)
			return onlyUserWorld;

		world = world.toLowerCase();

		for (String parent : userMirrors.keySet())
		{
			List<String> children = userMirrors.get(parent);
			if (children.contains("*") || children.contains(world))
				return parent;
		}

		return world;
	}

	public final boolean areUsersLinked(World world1, World world2)
	{
		return areUsersLinked(world1.getName(), world2.getName());
	}

	public final boolean areUsersLinked(String world1, String world2)
	{
		world1 = getUsersParent(world1);
		world2 = getUsersParent(world2);

		return world1.equals(world2);
	}

	public final boolean areUsersMirroredByDefault()
	{
		return defaultUserWorld != null;
	}

	public final String getDefaultUserWorld()
	{
		return defaultUserWorld;
	}

	public final void addUserMirror(String parent, String mirrored)
	{
		parent = parent.toLowerCase();
		mirrored = mirrored.toLowerCase();

		if (! userMirrors.containsKey(parent))
			userMirrors.put(parent, new ArrayList<String>());

		userMirrors.get(parent).add(mirrored);
	}

	// ---- Group Mirrors

	public final boolean areGroupsMirrored(World world)
	{
		return areGroupsMirrored(world.getName());
	}

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

		for (String parent : groupMirrors.keySet())
		{
			List<String> children = groupMirrors.get(parent);
			if (children.contains("*") || children.contains(world))
				return parent;
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

	public final boolean areGroupsMirroredByDefault()
	{
		return defaultGroupWorld != null;
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

	// ---- Loading

	@SuppressWarnings("unchecked")
	private final void loadUserMirrors()
	{
		FileConfiguration config = plugin.getConfig();
		if (config.isSet("userMirrors"))
		{
			Map<String, Object> values = config.getConfigurationSection("userMirrors").getValues(false);
			for (String parent : values.keySet())
			{
				parent = parent.toLowerCase();

				List<String> children = new ArrayList<String>();
				for (String child : (List<String>) values.get(parent))
					children.add(child.toLowerCase());

				// Default world
				if (children.contains("undefined_worlds"))
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
			for (String parent : values.keySet())
			{
				parent = parent.toLowerCase();

				List<String> children = new ArrayList<String>();
				for (String child : (List<String>) values.get(parent))
					children.add(child.toLowerCase());

				if (children.contains("undefined_worlds"))
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
		// (Re-) initialize maps
		this.userMirrors = Maps.newHashMap();
		this.groupMirrors = Maps.newHashMap();

		// Clear variables
		this.onlyUserWorld = null;
		this.onlyGroupWorld = null;

		this.defaultUserWorld = null;
		this.defaultGroupWorld = null;

		// (Re-) load
		this.loadUserMirrors();
		this.loadGroupMirrors();
	}
}