/**
 * (c) 2017 dmulloy2
 */
package net.dmulloy2.swornpermissions.data.backend;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import net.dmulloy2.swornpermissions.SwornPermissions;

/**
 * @author dmulloy2
 */
public class SQLiteBackend extends SQLBackend
{
	private static final String DATABASE = "jdbc:sqlite:%s/permissions.db";

	public SQLiteBackend(SwornPermissions plugin)
	{
		super(plugin);
	}

	@Override
	Connection connect() throws Exception
	{
		Class.forName("org.sqlite.JDBC");

		File data = plugin.getDataFolder();
		Connection con = DriverManager.getConnection(String.format(DATABASE, data.getPath()));
		if (con == null)
			throw new IllegalStateException("Failed to connect to database!");

		plugin.getLogHandler().log("Established database connection to permissions.db");
		return con;
	}

	@Override
	void ensureUserTable(String tableName) throws SQLException
	{
		String sql = "CREATE TABLE IF NOT EXISTS " + tableName + "(" +
				"identifier VARCHAR, " + 
				"lastKnownBy VARCHAR, " +
				"groupName VARCHAR, " +
				"subGroups VARCHAR, " +
				"permissions VARCHAR, " +
				"timestamp_keys VARCHAR, " +
				"timestamp_values VARCHAR, " +
				"option_keys VARCHAR, " +
				"option_values VARCHAR);";

		Statement statement = connection.createStatement();
		statement.executeUpdate(sql);
	}

	@Override
	public String toString()
	{
		return "SQLite";
	}
}