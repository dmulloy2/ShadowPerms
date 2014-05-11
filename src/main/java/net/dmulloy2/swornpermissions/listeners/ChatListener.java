/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.swornpermissions.listeners;

import lombok.AllArgsConstructor;
import net.dmulloy2.swornpermissions.SwornPermissions;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

/**
 * @author dmulloy2
 */

@AllArgsConstructor
public class ChatListener implements Listener
{
	private final SwornPermissions plugin;

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerChat(AsyncPlayerChatEvent event)
	{
		if (event.isCancelled())
			return;

		Player player = event.getPlayer();
		String msg = event.getMessage();

		event.setFormat(plugin.getChatHandler().parseChatMessage(player, msg));
	}
}