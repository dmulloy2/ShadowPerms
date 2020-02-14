/**
 * (c) 2015 dmulloy2
 */
package net.dmulloy2.shadowperms.vault;

import net.dmulloy2.shadowperms.ShadowPerms;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.plugin.ServicePriority;

/**
 * @author dmulloy2
 */

public class VaultHandler
{
	public static void setupIntegration(ShadowPerms plugin)
	{
		ShadowPermsVault perms = new ShadowPermsVault(plugin);
		plugin.getServer().getServicesManager().register(Permission.class, perms, plugin, ServicePriority.Highest);

		ShadowChatVault chat = new ShadowChatVault(plugin, perms);
		plugin.getServer().getServicesManager().register(Chat.class, chat, plugin, ServicePriority.Highest);
	}
}