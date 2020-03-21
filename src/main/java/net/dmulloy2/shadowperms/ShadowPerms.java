/**
 * ShadowPerms - comprehensive permission, chat, and world management system
 * Copyright (C) 2014 - 2015 dmulloy2
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
package net.dmulloy2.shadowperms;

import net.dmulloy2.SwornPlugin;
import net.dmulloy2.commands.CmdHelp;
import net.dmulloy2.handlers.CommandHandler;
import net.dmulloy2.handlers.LogHandler;
import net.dmulloy2.shadowperms.commands.CmdBackup;
import net.dmulloy2.shadowperms.commands.CmdCleanUp;
import net.dmulloy2.shadowperms.commands.CmdCreateGroup;
import net.dmulloy2.shadowperms.commands.CmdNick;
import net.dmulloy2.shadowperms.commands.CmdPrefix;
import net.dmulloy2.shadowperms.commands.CmdPrefixReset;
import net.dmulloy2.shadowperms.commands.CmdPrune;
import net.dmulloy2.shadowperms.commands.CmdRealName;
import net.dmulloy2.shadowperms.commands.CmdReload;
import net.dmulloy2.shadowperms.commands.CmdSave;
import net.dmulloy2.shadowperms.commands.CmdSuffix;
import net.dmulloy2.shadowperms.commands.CmdSuffixReset;
import net.dmulloy2.shadowperms.commands.CmdVersion;
import net.dmulloy2.shadowperms.commands.group.CmdGroup;
import net.dmulloy2.shadowperms.commands.group.CmdListGroups;
import net.dmulloy2.shadowperms.commands.user.CmdUser;
import net.dmulloy2.shadowperms.data.DataHandler;
import net.dmulloy2.shadowperms.handlers.AntiItemHandler;
import net.dmulloy2.shadowperms.handlers.ChatHandler;
import net.dmulloy2.shadowperms.handlers.MirrorHandler;
import net.dmulloy2.shadowperms.handlers.PermissionHandler;
import net.dmulloy2.shadowperms.listeners.ChatListener;
import net.dmulloy2.shadowperms.listeners.PlayerListener;
import net.dmulloy2.shadowperms.listeners.ServerListener;
import net.dmulloy2.shadowperms.listeners.WorldListener;
import net.dmulloy2.shadowperms.vault.VaultHandler;
import net.dmulloy2.types.Reloadable;
import net.dmulloy2.util.FormatUtil;

import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitRunnable;

import lombok.Getter;

/**
 * @author dmulloy2
 */

@Getter
public class ShadowPerms extends SwornPlugin implements Reloadable
{
	private PermissionHandler permissionHandler;
	private AntiItemHandler antiItemHandler;
	private MirrorHandler mirrorHandler;
	private ChatHandler chatHandler;
	private DataHandler dataHandler;

	private boolean disabling;
	private boolean updated;

	private String prefix = FormatUtil.format("&3[&eShadowPerms&3] » &e");

	@Override
	public void onLoad()
	{
		// Vault Integration
		PluginManager pm = getServer().getPluginManager();
		if (pm.getPlugin("Vault") != null)
		{
			try
			{
				VaultHandler.setupIntegration(this);
			} catch (Throwable ignored) { }
		}
	}

	@Override
	public void onEnable()
	{
		long start = System.currentTimeMillis();

		disabling = false;

		// Register log handler
		logHandler = new LogHandler(this);

		// Configuration
		saveDefaultConfig();
		reloadConfig();

		// Register other handlers
		antiItemHandler = new AntiItemHandler(this);
		commandHandler = new CommandHandler(this);
		mirrorHandler = new MirrorHandler(this);
		chatHandler = new ChatHandler(this);

		permissionHandler = new PermissionHandler(this);
		dataHandler = new DataHandler(this);

		permissionHandler.load();

		// Register prefixed commands
		commandHandler.setCommandPrefix("perm");
		commandHandler.registerPrefixedCommand(new CmdBackup(this));
		commandHandler.registerPrefixedCommand(new CmdCleanUp(this));
		commandHandler.registerPrefixedCommand(new CmdCreateGroup(this));
		commandHandler.registerPrefixedCommand(new CmdGroup(this));
		commandHandler.registerPrefixedCommand(new CmdHelp(this));
		commandHandler.registerPrefixedCommand(new CmdListGroups(this));
		commandHandler.registerPrefixedCommand(new CmdPrune(this));
		commandHandler.registerPrefixedCommand(new CmdReload(this));
		commandHandler.registerPrefixedCommand(new CmdSave(this));
		commandHandler.registerPrefixedCommand(new CmdUser(this));
		commandHandler.registerPrefixedCommand(new CmdVersion(this));

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
				permissionHandler.update();
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