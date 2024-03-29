/**
 * (c) 2016 dmulloy2
 */
package net.dmulloy2.shadowperms.commands;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import net.dmulloy2.shadowperms.ShadowPerms;
import net.dmulloy2.shadowperms.data.backend.YAMLBackend;
import net.dmulloy2.shadowperms.types.Permission;
import net.dmulloy2.shadowperms.types.User;
import net.dmulloy2.swornapi.util.FormatUtil;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * @author dmulloy2
 */

public class CmdPrune extends ShadowPermsCommand
{
	public CmdPrune(ShadowPerms plugin)
	{
		super(plugin);
		this.name = "prune";
		this.description = "Prune users that shouldn''t have been saved";
		this.permission = Permission.CMD_PRUNE;
		this.usesPrefix = true;
	}

	@Override
	public void perform()
	{
		if (plugin.getDataHandler().getUserBackend() instanceof YAMLBackend)
		{
			plugin.getDataHandler().backup(sender);
			sendpMessage("Pruned &b{0} &eusers!", prune());
		}
		else
		{
			err("Pruning is not supported on non-YAML backends.");
			err("Luckily, pruning shouldn't be an issue.");
		}
	}

	private int prune()
	{
		messageAndLog("Pruning user files, this may take a while...");
		int pruned = 0;

		List<String> loadedWorlds = plugin.getDataHandler().getLoadedWorlds();
		for (String world : loadedWorlds)
		{
			if (! plugin.getMirrorHandler().areUsersMirrored(world))
			{
				try
				{
					int fromWorld = prune(world);
					pruned += fromWorld;

					if (fromWorld > 0)
						messageAndLog("Pruned &b{0} &eusers from &b{1}&e.", fromWorld, world);
				}
				catch (IOException ex)
				{
					messageAndLog("Failed to prune world {0}: {1}", world, ex);
				}
			}
		}

		return pruned;
	}

	// Basically by pruning we're omitting users that shouldn't have been saved
	// in the first place. This behavior should be fixed, so pruning exists for
	// legacy purposes.

	private int prune(String world) throws IOException
	{
		File worlds = new File(plugin.getDataFolder(), "worlds");
		if (! worlds.exists())
			return 0;

		File worldFile = new File(worlds, world);
		if (! worldFile.exists())
			return 0;

		File file = new File(worldFile, "users.yml");
		if (! file.exists())
			return 0;

		file.delete();
		file.createNewFile();

		YamlConfiguration config = new YamlConfiguration();
		int pruned = 0;

		Collection<User> users = plugin.getPermissionHandler().getAllUsers(world);
		for (User user : users)
		{
			if (user.shouldBeSaved())
			{
				config.createSection("users." + user.getSaveName(), user.serialize());
			}
			else
			{
				plugin.getLogHandler().log("Pruning user {0} from {1}.", user.getLastKnownBy(), world);
				pruned++;
			}
		}

		config.save(file);
		return pruned;
	}

	private void messageAndLog(String message, Object... args)
	{
		if (isPlayer())
			sendpMessage(message, args);

		plugin.getLogHandler().log(ChatColor.stripColor(FormatUtil.format(message, args)));
	}
}