/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.swornpermissions.listeners;

import lombok.AllArgsConstructor;
import net.dmulloy2.swornpermissions.SwornPermissions;
import net.dmulloy2.swornpermissions.types.User;
import net.dmulloy2.util.FormatUtil;

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
	private final SwornPermissions plugin;

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
		user.updatePermissions(true);
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

		if (plugin.getPermissionHandler().areUsersDifferent(event.getFrom(), player.getWorld()))
			user = plugin.getPermissionHandler().moveWorld(player, event.getFrom(), player.getWorld());

		user.updatePermissions(false);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		Player player = event.getPlayer();
		User user = plugin.getPermissionHandler().getUser(player);
		if (user == null)
		{
			player.sendMessage(plugin.getPrefix() + FormatUtil.format("Failed to get a user instance! Contact an administrator!"));
			return;
		}

		user.removeAttachment();
	}
}