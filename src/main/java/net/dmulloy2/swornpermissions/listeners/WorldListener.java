/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.swornpermissions.listeners;

import lombok.AllArgsConstructor;
import net.dmulloy2.swornpermissions.SwornPermissions;

import org.bukkit.World;
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
		World world = event.getWorld();
		plugin.getDataHandler().loadWorld(world);
		plugin.getPermissionHandler().registerWorld(world);
	}
}