/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.shadowperms.listeners;

import lombok.AllArgsConstructor;
import net.dmulloy2.shadowperms.ShadowPerms;
import net.dmulloy2.shadowperms.types.User;
import net.dmulloy2.swornapi.util.FormatUtil;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * @author dmulloy2
 */

@AllArgsConstructor
public class PlayerListener implements Listener
{
	private final ShadowPerms plugin;

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerJoin(PlayerJoinEvent event)
	{
		Player player = event.getPlayer();

		User user = plugin.getPermissionHandler().getUser(player);
		if (user == null)
		{
			player.sendMessage(plugin.getPrefix() + FormatUtil.format("Failed to get a user instance! Contact an administrator!"));
			return;
		}

		user.updateUniqueID(player);
		user.updatePermissions(player, true);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerChangedWorld(PlayerChangedWorldEvent event)
	{
		Player player = event.getPlayer();
		User user = plugin.getPermissionHandler().getUser(player);
		if (user == null)
		{
			player.sendMessage(plugin.getPrefix() + FormatUtil.format("Failed to get a user instance! Contact an administrator!"));
			return;
		}

		user = plugin.getPermissionHandler().moveWorld(player, event.getFrom(), player.getWorld());

		boolean force = plugin.getConfig().getBoolean("forceUpdate.worldChange", false);
		user.updatePermissions(player, force);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		Player player = event.getPlayer();
		User user = plugin.getPermissionHandler().getUser(player);
		if (user != null)
			user.logout();
	}
}