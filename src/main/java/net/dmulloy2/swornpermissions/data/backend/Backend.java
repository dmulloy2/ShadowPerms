/**
 * (c) 2017 dmulloy2
 */
package net.dmulloy2.swornpermissions.data.backend;

import java.util.Map;
import java.util.Set;

import net.dmulloy2.swornpermissions.types.ServerGroup;
import net.dmulloy2.swornpermissions.types.User;
import net.dmulloy2.swornpermissions.types.WorldGroup;
import net.dmulloy2.types.Reloadable;

import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

/**
 * @author dmulloy2
 */
public interface Backend extends Reloadable
{
	void saveUsers(String world) throws Exception;
	void saveGroups(String world) throws Exception;
	void saveServerGroups() throws Exception;

	void loadWorld(World world) throws Exception;

	User loadUser(String world, OfflinePlayer player) throws Exception;
	User loadUser(String world, String identifier) throws Exception;

	void reloadUser(User user);

	Map<String, Map<String, WorldGroup>> loadWorldGroups() throws Exception;
	Map<String, ServerGroup> loadServerGroups() throws Exception;

	Set<String> getUsers(String world);

	void backup(CommandSender sender);

	public static enum BackendType
	{
		MY_SQL("mysql", "sql"),
		SQL_LITE("sqlite"),
		YAML("yaml", "yml"),
		;

		String[] ids;
		BackendType(String... ids)
		{
			this.ids = ids;
		}

		public static BackendType find(String input)
		{
			input = input.toLowerCase();
			for (BackendType type : BackendType.values())
			{
				for (String id : type.ids)
				{
					if (id.equals(input))
						return type;
				}
			}

			return YAML;
		}
	}
}