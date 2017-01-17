/**
 * (c) 2017 dmulloy2
 */
package net.dmulloy2.swornpermissions.data.backend;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import net.dmulloy2.swornpermissions.SwornPermissions;

import org.apache.commons.lang.Validate;

/**
 * @author dmulloy2
 */
public class MySQLBackend extends SQLBackend
{
	public MySQLBackend(SwornPermissions plugin)
	{
		super(plugin);
	}

	@Override
	Connection connect() throws Exception
	{
		String url = plugin.getConfig().getString("database.MySQL.url");
		Validate.notEmpty(url, "url cannot be null or empty!");

		String username = plugin.getConfig().getString("database.MySQL.username");
		Validate.notEmpty(username, "username cannot be null or empty!");

		String password = plugin.getConfig().getString("database.MySQL.password");
		Validate.notEmpty(password, "password cannot be null or empty!");

		String database = plugin.getConfig().getString("database.MySQL.database");
		Validate.notEmpty(database, "database cannot be null or empty!");

		Class.forName("com.mysql.jdbc.Driver");
		Connection con = DriverManager.getConnection("jdbc:mysql://" + url + "/" + database, username, password);
		Validate.notNull(con, "Failed to establish database connection!");

		plugin.getLogHandler().log("Established database connection to {0}", url);
		return con;
	}

	@Override
	void ensureUserTable(String tableName) throws SQLException
	{
		String sql = "CREATE TABLE IF NOT EXISTS " + tableName + "(" +
				"identifier VARCHAR(64), " + 
				"lastKnownBy VARCHAR(64), " +
				"groupName VARCHAR(64), " +
				"subGroups TEXT, " +
				"permissions TEXT, " +
				"timestamp_keys TEXT, " +
				"timestamp_values TEXT, " +
				"option_keys TEXT, " +
				"option_values TEXT);";

		// System.out.println(sql);
		Statement statement = connection.createStatement();
		statement.executeUpdate(sql);
	}

	@Override
	public String toString()
	{
		return "MySQL";
	}
}