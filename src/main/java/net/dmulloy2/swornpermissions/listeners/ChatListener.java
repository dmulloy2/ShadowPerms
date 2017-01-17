/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.swornpermissions.listeners;

import java.util.logging.Level;

import lombok.AllArgsConstructor;
import net.dmulloy2.swornpermissions.SwornPermissions;
import net.dmulloy2.util.Util;

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

		try
		{
			String format = plugin.getChatHandler().formatChat(player, msg);
			if (format != null)
				event.setFormat(format);
		}
		catch (Throwable ex)
		{
			plugin.getLogHandler().log(Level.SEVERE, Util.getUsefulStack(ex, "parsing " + player.getName() + "''s chat"));
		}
	}
}