/**
 * (c) 2017 dmulloy2
 */
package net.dmulloy2.shadowperms.data.backend;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

import net.dmulloy2.shadowperms.ShadowPerms;
import net.dmulloy2.shadowperms.data.DataHandler;

import org.bukkit.command.CommandSender;
import org.bukkit.util.FileUtil;

/**
 * @author dmulloy2
 */
public class SQLiteBackend extends SQLBackend
{
	private static final String DATABASE = "jdbc:sqlite:%s";
	private final String file;

	public SQLiteBackend(ShadowPerms plugin)
	{
		super(plugin);
		this.file = plugin.getDataFolder().getPath() + "/permissions.db";
	}

	@Override
	Connection connect() throws Exception
	{
		Class.forName("org.sqlite.JDBC");

		Connection con = DriverManager.getConnection(String.format(DATABASE, file));
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

	// TODO It's possible (and also maybe safer?) to backup via commands

	@Override
	public void backup(CommandSender sender)
	{
		File database = new File(file);
		if (! database.exists())
			return;

		File backup = new File(file.substring(0, file.length() - 3) + "-" + DataHandler.BACKUP_FORMAT.format(new Date()) + ".db");
		if (backup.exists())
			backup.delete();

		FileUtil.copy(database, backup);
	}

	@Override
	public String toString()
	{
		return "SQLite";
	}
}