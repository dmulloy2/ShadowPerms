/**
 * (c) 2017 dmulloy2
 */
package net.dmulloy2.shadowperms.data.backend;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import net.dmulloy2.shadowperms.ShadowPerms;
import net.dmulloy2.shadowperms.data.Tuple;
import net.dmulloy2.shadowperms.types.ServerGroup;
import net.dmulloy2.shadowperms.types.User;
import net.dmulloy2.shadowperms.types.WorldGroup;
import net.dmulloy2.types.StringJoiner;
import net.dmulloy2.util.ListUtil;
import net.dmulloy2.util.Util;

import org.bukkit.OfflinePlayer;
import org.bukkit.World;

/**
 * @author dmulloy2
 */
// TODO Add support for group SQL?
public abstract class SQLBackend implements Backend
{
	protected Connection connection;

	protected boolean unifiedUsers;
	protected final ShadowPerms plugin;

	protected SQLBackend(ShadowPerms plugin)
	{
		this.plugin = plugin;
		this.unifiedUsers = plugin.getConfig().getBoolean("unifiedUsers", true);

		try
		{
			this.connection = connect();
		}
		catch (Throwable ex)
		{
			throw new RuntimeException("Failed to connect to SQL:", ex);
		}
	}

	abstract Connection connect() throws Exception;

	private String getUserTable(String world) throws SQLException
	{
		String table = unifiedUsers ? "SwornPerms_users" : "SwornPerms_" + world.toLowerCase() + "_users";
		ensureUserTable(table);
		return table;
	}

	@SuppressWarnings("unused")
	private String getGroupTable(String world)
	{
		return "SwornPerms_" + world.toLowerCase() + "_groups";
	}

	abstract void ensureUserTable(String tableName) throws SQLException;

	@Override
	public void saveUsers(String world) throws Exception
	{
		String sql = "";
		String table  = getUserTable(world);

		for (User user : plugin.getPermissionHandler().getUsers(world))
		{
			try
			{
				String id = user.getUniqueId();
				if (user.shouldBeSaved())
				{
					Tuple<String, String> options = toArrayStrings(user.getOptions());

					int i = 1;
					boolean idFirst = false;

					if (! rowExists(table, "identifier", id))
					{
						sql = "INSERT INTO " + table + " (identifier, lastKnownBy, groupName, subGroups, permissions, option_keys, option_values) " +
								" VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);";
						idFirst = true;
					}
					else
					{
						sql = String.format("UPDATE " + table + " SET lastKnownBy=?, groupName=?, subGroups=?, permissions=?,"
								+ "option_keys=?, option_values=? WHERE identifier=?;");
					}

					PreparedStatement statement = connection.prepareStatement(sql);

					if (idFirst) statement.setString(i++, id);
					statement.setString(i++, user.getLastKnownBy());
					statement.setString(i++, user.getGroupName());
					statement.setString(i++, toArrayString(user.getSubGroupNames()));
					statement.setString(i++, toArrayString(user.getPermissionNodes()));
					statement.setString(i++, options.getFirst());
					statement.setString(i++, options.getSecond());
					if (!idFirst) statement.setString(i++, id);

					statement.executeUpdate();
				}
				else
				{
					if (rowExists(table, "identifier", id))
					{
						sql = "DELETE FROM " + table + " WHERE identifier=?";
						PreparedStatement statement = connection.prepareStatement(sql);
						statement.setString(1, id);
						statement.executeUpdate();
					}
				}
			}
			catch (Throwable ex)
			{
				plugin.getLogHandler().log(Level.SEVERE, Util.getUsefulStack(ex, "saving user " + user.getName()));
			}
		}
	}

	@Override
	public void saveGroups(String world) throws Exception
	{
		
	}

	@Override
	public void saveServerGroups() throws Exception
	{
		
	}

	@Override
	public void loadWorld(World world) throws Exception
	{
		// We have nothing to do here
	}

	@Override
	public User loadUser(String world, OfflinePlayer player) throws Exception
	{
		String table = getUserTable(world);
		String key = player.getUniqueId().toString();
		if (! rowExists(table, "identifier", key))
		{
			return new User(plugin, player, world);
		}

		String sql = "SELECT * FROM " + table + " WHERE identifier=?;";
		PreparedStatement statement = connection.prepareStatement(sql);
		statement.setString(1, key);
		ResultSet results = statement.executeQuery();

		return new User(plugin, player, world, results);
	}

	@Override
	public User loadUser(String world, String key) throws Exception
	{
		OfflinePlayer player = Util.matchOfflinePlayer(key);
		if (player != null)
			return loadUser(world, player);

		String table = getUserTable(world);
		if (! rowExists(table, "identifier", key))
			return null;

		String sql = "SELECT * FROM " + table + " WHERE identifier=?;";
		PreparedStatement statement = connection.prepareStatement(sql);
		statement.setString(1, key);
		ResultSet results = statement.executeQuery();

		return new User(plugin, null, world, results);
	}

	@Override
	public void reloadUser(User user)
	{
		try
		{
			String world =  plugin.getMirrorHandler().getUsersParent(user.getWorld());
			String uniqueId = user.getUniqueId();

			String table = getUserTable(world);
			if (! rowExists(table, "identifier", uniqueId))
				return;

			String sql = "SELECT * FROM " + table + " WHERE identifier=?;";
			PreparedStatement statement = connection.prepareStatement(sql);
			statement.setString(1, uniqueId);
			ResultSet results = statement.executeQuery();
			user.loadFromSQL(results);
		}
		catch (SQLException ex)
		{
			throw new RuntimeException("Failed to reload user " + user.getName() + " from SQL:", ex);
		}
	}

	@Override
	public Map<String, Map<String, WorldGroup>> loadWorldGroups() throws Exception
	{
		return null;
	}

	@Override
	public Map<String, ServerGroup> loadServerGroups() throws Exception
	{
		return null;
	}

	@Override
	public Set<String> getUsers(String world)
	{
		try
		{
			String table = getUserTable(world);
			String sql = "SELECT identifier FROM " + table + ";";
			Statement statement = connection.createStatement();

			ResultSet results = statement.executeQuery(sql);
			Set<String> users = new HashSet<>();
			while (results.next())
				users.add(results.getString(1));

			return users;
		}
		catch (SQLException ex)
		{
			throw new RuntimeException("Failed to obtain users for " + world, ex);
		}
	}

	@Override
	public void reload()
	{
		// I suppose you /could/ change this, but why
		this.unifiedUsers = plugin.getConfig().getBoolean("unifiedUsers", true);
	}

	protected boolean columnExists(String table, String column)
	{
		try
		{
			String sql = "SELECT " + column + " FROM " + table + ";";
			Statement statement = connection.createStatement();
			return statement.executeQuery(sql).next();
		}
		catch (Throwable ex)
		{
			return false;
		}
	}

	protected boolean rowExists(String table, String idColumn, String id)
	{
		try
		{
			String sql = "SELECT * FROM " + table + " WHERE " + idColumn + "=?";
			PreparedStatement statement = connection.prepareStatement(sql);
			statement.setString(1, id);
			return statement.executeQuery().next();
		}
		catch (Throwable ex)
		{
			ex.printStackTrace(); // TODO debug
			return false;
		}
	}

	// ---- Serialization

	// Essentially what's happening here is Map -> Array -> String, then back again.
	// MySQL and SQLite don't have Map and Array types we could really use,
	// so instead we just turn it into a string and hope it doesn't overflow.

	public static final String DELIM = "&#";

	// Remove empty and delim elements
	private static List<String> sanitize(List<String> list)
	{
		Iterator<String> iter = list.iterator();
		while (iter.hasNext())
		{
			String elem = iter.next().trim();
			if (elem.isEmpty() || elem.equals(DELIM))
				iter.remove();
		}

		return list;
	}

	// Make sure strings don't contain the delimiter anywhere else
	public static String sanitize(String string)
	{
		return string.replace(DELIM, "");
	}

	public static Tuple<String, String> toArrayStrings(Map<?, ?> map)
	{
		if (map.isEmpty())
			return new Tuple<>("", "");

		StringJoiner joiner = new StringJoiner(DELIM);
		for (Object key : map.keySet())
			joiner.append(sanitize(key.toString()));
		String keyStr = joiner.toString();

		joiner.newString();
		for (Object value : map.values())
			joiner.append(sanitize(value.toString()));
		String valStr = joiner.toString();

		return new Tuple<>(keyStr, valStr);
	}

	// Possibly genericize this, but I'm unaware of use-cases at the moment
	public static String toArrayString(List<String> list)
	{
		list = sanitize(list);
		if (list.isEmpty())
			return "";

		StringJoiner joiner = new StringJoiner(DELIM);
		for (String elem : list)
			joiner.append(sanitize(elem));
		return joiner.toString();
	}

	public static List<String> fromArrayString(String string)
	{
		String[] elems = string.split(DELIM);
		return sanitize(ListUtil.toList(elems));
	}

	public static Map<String, String> fromArrayStrings(String keyStr, String valStr)
	{
		String[] keys = keyStr.split(DELIM);
		String[] values = valStr.split(DELIM);

		Map<String, String> map = new HashMap<>(keys.length);
		for (int i = 0; i < keys.length; i++)
			map.put(keys[i], values[i]);

		return map;
	}
}
