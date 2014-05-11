/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.swornpermissions.listeners;

import lombok.AllArgsConstructor;
import net.dmulloy2.swornpermissions.SwornPermissions;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldInitEvent;

/**
 * @author dmulloy2
 */

@AllArgsConstructor
public class WorldListener implements Listener
{
	private final SwornPermissions plugin;

	@EventHandler(priority = EventPriority.MONITOR)
	public void onWorldInit(WorldInitEvent event)
	{
		plugin.getDataHandler().loadWorld(event.getWorld());
		plugin.getPermissionHandler().registerWorld(event.getWorld());
	}
}