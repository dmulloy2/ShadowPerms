/**
 * SwornPermissions - comprehensive permission, chat, and world management system
 * Copyright (C) 2014 dmulloy2
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package net.dmulloy2.swornpermissions;

import lombok.Getter;
import net.dmulloy2.swornpermissions.commands.CmdCreateGroup;
import net.dmulloy2.swornpermissions.commands.CmdHelp;
import net.dmulloy2.swornpermissions.commands.CmdNick;
import net.dmulloy2.swornpermissions.commands.CmdPrefix;
import net.dmulloy2.swornpermissions.commands.CmdPrefixReset;
import net.dmulloy2.swornpermissions.commands.CmdRealName;
import net.dmulloy2.swornpermissions.commands.CmdReload;
import net.dmulloy2.swornpermissions.commands.CmdSave;
import net.dmulloy2.swornpermissions.commands.CmdSuffix;
import net.dmulloy2.swornpermissions.commands.CmdSuffixReset;
import net.dmulloy2.swornpermissions.commands.CmdVersion;
import net.dmulloy2.swornpermissions.commands.group.CmdGroup;
import net.dmulloy2.swornpermissions.commands.group.CmdListGroups;
import net.dmulloy2.swornpermissions.commands.user.CmdUser;
import net.dmulloy2.swornpermissions.commands.wizard.CmdWizard;
import net.dmulloy2.swornpermissions.conversion.ConversionHandler;
import net.dmulloy2.swornpermissions.handlers.AntiItemHandler;
import net.dmulloy2.swornpermissions.handlers.ChatHandler;
import net.dmulloy2.swornpermissions.handlers.CommandHandler;
import net.dmulloy2.swornpermissions.handlers.DataHandler;
import net.dmulloy2.swornpermissions.handlers.LogHandler;
import net.dmulloy2.swornpermissions.handlers.MirrorHandler;
import net.dmulloy2.swornpermissions.handlers.PermissionHandler;
import net.dmulloy2.swornpermissions.handlers.WizardHandler;
import net.dmulloy2.swornpermissions.listeners.ChatListener;
import net.dmulloy2.swornpermissions.listeners.PlayerListener;
import net.dmulloy2.swornpermissions.listeners.ServerListener;
import net.dmulloy2.swornpermissions.listeners.WorldListener;
import net.dmulloy2.swornpermissions.vault.SwornChatVault;
import net.dmulloy2.swornpermissions.vault.SwornPermissionsVault;
import net.dmulloy2.types.Reloadable;
import net.dmulloy2.util.FormatUtil;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * @author dmulloy2
 */

public class SwornPermissions extends JavaPlugin implements Reloadable
{
	private @Getter PermissionHandler permissionHandler;
	private @Getter AntiItemHandler antiItemHandler;
	private @Getter CommandHandler commandHandler;
	private @Getter MirrorHandler mirrorHandler;
	private @Getter WizardHandler wizardHandler;
	private @Getter ChatHandler chatHandler;
	private @Getter DataHandler dataHandler;
	private @Getter LogHandler logHandler;

	private @Getter boolean disabling;
	private @Getter boolean updated;

	private @Getter String prefix = FormatUtil.format("&3[&eSwornPerms&3]&e ");

	@Override
	public void onLoad()
	{
		// Vault Integration
		PluginManager pm = getServer().getPluginManager();
		if (pm.getPlugin("Vault") != null)
		{
			SwornPermissionsVault perms = new SwornPermissionsVault(this);
			getServer().getServicesManager().register(Permission.class, perms, this, ServicePriority.Highest);

			SwornChatVault chat = new SwornChatVault(this, perms);
			getServer().getServicesManager().register(Chat.class, chat, this, ServicePriority.Highest);
		}
	}

	@Override
	public void onEnable()
	{
		long start = System.currentTimeMillis();

		disabling = false;

		// Register log handler
		logHandler = new LogHandler(this);

		// If the data folder doesn't exist,
		// attempt to convert from other systems
		if (! getDataFolder().exists())
		{
			new ConversionHandler(this).attemptConversion();
		}

		// Configuration
		saveDefaultConfig();
		reloadConfig();

		// Register other handlers
		antiItemHandler = new AntiItemHandler(this);
		commandHandler = new CommandHandler(this);
		mirrorHandler = new MirrorHandler(this);
		wizardHandler = new WizardHandler(this);
		dataHandler = new DataHandler(this);
		chatHandler = new ChatHandler(this);

		permissionHandler = new PermissionHandler(this);
		permissionHandler.load();

		// Register prefixed commands
		commandHandler.setCommandPrefix("swornperms");
		commandHandler.registerPrefixedCommand(new CmdCreateGroup(this));
		commandHandler.registerPrefixedCommand(new CmdGroup(this));
		commandHandler.registerPrefixedCommand(new CmdHelp(this));
		commandHandler.registerPrefixedCommand(new CmdListGroups(this));
		commandHandler.registerPrefixedCommand(new CmdReload(this));
		commandHandler.registerPrefixedCommand(new CmdSave(this));
		commandHandler.registerPrefixedCommand(new CmdUser(this));
		commandHandler.registerPrefixedCommand(new CmdVersion(this));
		commandHandler.registerPrefixedCommand(new CmdWizard(this));

		// Register non-prefixed commands
		commandHandler.registerCommand(new CmdNick(this));
		commandHandler.registerCommand(new CmdPrefix(this));
		commandHandler.registerCommand(new CmdPrefixReset(this));
		commandHandler.registerCommand(new CmdRealName(this));
		commandHandler.registerCommand(new CmdSuffix(this));
		commandHandler.registerCommand(new CmdSuffixReset(this));

		// Register listeners
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new ChatListener(this), this);
		pm.registerEvents(new PlayerListener(this), this);
		pm.registerEvents(new ServerListener(this), this);
		pm.registerEvents(new WorldListener(this), this);

		// Initial update
		new BukkitRunnable()
		{
			@Override
			public void run()
			{
				permissionHandler.updateGroups();
				permissionHandler.updateUsers();
				logHandler.log("Groups and users updated!");
				updated = true;
			}
		}.runTaskLater(this, 20L);

		logHandler.log("{0} has been enabled. Took {1} ms.", getDescription().getFullName(), System.currentTimeMillis() - start);
	}

	@Override
	public void onDisable()
	{
		long start = System.currentTimeMillis();

		disabling = true;
		updated = false;

		getServer().getServicesManager().unregisterAll(this);
		getServer().getScheduler().cancelTasks(this);

		dataHandler.save();

		logHandler.log("{0} has been disabled. Took {1} ms.", getDescription().getFullName(), System.currentTimeMillis() - start);
	}

	@Override
	public void reload()
	{
		reloadConfig();

		chatHandler.reload();
		dataHandler.reload();
		mirrorHandler.reload();
		permissionHandler.reload();
	}
}