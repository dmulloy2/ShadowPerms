/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.swornpermissions.handlers;

import java.util.List;
import java.util.Map.Entry;

import net.dmulloy2.swornpermissions.SwornPermissions;
import net.dmulloy2.swornpermissions.types.MyMaterial;
import net.dmulloy2.swornpermissions.types.Reloadable;
import net.dmulloy2.swornpermissions.util.FormatUtil;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/**
 * @author dmulloy2
 */

public class AntiItemHandler implements Listener, Reloadable
{
	private List<MyMaterial> blacklistRightClickItems;
	private List<MyMaterial> blacklistLeftClickItems;
	private List<MyMaterial> blacklistItems;

	private int maxEnchantmentLevel;
	private boolean blockGodItems;
	
	@SuppressWarnings("unused")
	private final SwornPermissions plugin;
	public AntiItemHandler(SwornPermissions plugin)
	{
		this.plugin = plugin;
		this.reload();
	}

	@EventHandler
	public void onPlayerClickEvent(PlayerInteractEvent e)
	{
		switch (e.getAction())
		{
			case RIGHT_CLICK_BLOCK:
			case RIGHT_CLICK_AIR:
				if (e.getItem() != null)
				{
					ItemStack item = e.getItem();

					if (blacklistRightClickItems.contains(new MyMaterial(item.getType())))
					{
						e.setCancelled(true);
					}

					if (blockGodItems && ! item.getEnchantments().isEmpty())
					{
						for (Entry<Enchantment, Integer> enchantment : item.getEnchantments().entrySet())
						{
							if (enchantment.getValue() > maxEnchantmentLevel)
							{
								item.removeEnchantment(enchantment.getKey());
								blockedGodItem(e.getPlayer(), enchantment.getKey(), enchantment.getValue(), item.getType(), true);
							}
							else if (enchantment.getValue() < 0)
							{
								item.removeEnchantment(enchantment.getKey());
								blockedGodItem(e.getPlayer(), enchantment.getKey(), enchantment.getValue(), item.getType(), false);
							}
						}
					}
				}

				if (e.getClickedBlock() != null)
				{
					if (blacklistItems.contains(new MyMaterial(e.getClickedBlock().getType(), e.getClickedBlock().getState().getData())))
					{
						e.getClickedBlock().setType(Material.AIR);
					}
				}

				break;
			case LEFT_CLICK_AIR:
			case LEFT_CLICK_BLOCK:
				if (e.hasItem())
				{
					if (blacklistLeftClickItems.contains(new MyMaterial(e.getItem().getType())))
					{
						e.setCancelled(true);
					}
				}

				break;
			default:
				break;
		}
	}

	@EventHandler
	public void onPlayerPlaceBlock(BlockPlaceEvent e)
	{
		if (e.getBlockPlaced() != null)
		{
			if (blacklistItems.contains(new MyMaterial(e.getBlockPlaced().getType(), e.getBlockPlaced().getState().getData())))
			{
				e.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onInventoryCloseEvent(InventoryCloseEvent e)
	{
		if (e.getPlayer() instanceof Player)
		{
			Player player = (Player) e.getPlayer();
			PlayerInventory i = player.getInventory();

			ItemStack helmet = i.getHelmet();
			ItemStack chest = i.getChestplate();
			ItemStack legs = i.getLeggings();
			ItemStack boots = i.getBoots();

			if (helmet != null && blacklistItems.contains(new MyMaterial(helmet.getType())))
			{
				blockedRegularItem(player, helmet.getType());
				i.setHelmet(null);
			}
			if (chest != null && blacklistItems.contains(new MyMaterial(chest.getType())))
			{
				blockedRegularItem(player, chest.getType());
				i.setChestplate(null);
			}
			if (legs != null && blacklistItems.contains(new MyMaterial(legs.getType())))
			{
				blockedRegularItem(player, legs.getType());
				i.setLeggings(null);
			}
			if (boots != null && blacklistItems.contains(new MyMaterial(boots.getType())))
			{
				blockedRegularItem(player, boots.getType());
				i.setBoots(null);
			}

			for (ItemStack item : i.getContents())
			{
				if (item != null && item.getType() != Material.AIR)
				{
					if (blacklistItems.contains(new MyMaterial(item.getType(), item.getData())))
					{
						blockedRegularItem(player, item.getType());
						i.remove(item);
						continue;
					}

					if (blockGodItems && ! item.getEnchantments().isEmpty())
					{
						for (Entry<Enchantment, Integer> enchantment : item.getEnchantments().entrySet())
						{
							if (enchantment.getValue() > maxEnchantmentLevel)
							{
								item.removeEnchantment(enchantment.getKey());
								blockedGodItem(player, enchantment.getKey(), enchantment.getValue(), item.getType(), true);
							}
							else if (enchantment.getValue() < 0)
							{
								item.removeEnchantment(enchantment.getKey());
								blockedGodItem(player, enchantment.getKey(), enchantment.getValue(), item.getType(), false);
							}
						}
					}
				}
			}

			for (ItemStack armor : i.getArmorContents())
			{
				if (armor != null && armor.getType() != Material.AIR)
				{
					if (blockGodItems && ! armor.getEnchantments().isEmpty())
					{
						for (Entry<Enchantment, Integer> enchantment : armor.getEnchantments().entrySet())
						{
							if (enchantment.getValue() > maxEnchantmentLevel)
							{
								armor.removeEnchantment(enchantment.getKey());
								blockedGodItem(player, enchantment.getKey(), enchantment.getValue(), armor.getType(), true);
							}
							else if (enchantment.getValue() < 0)
							{
								armor.removeEnchantment(enchantment.getKey());
								blockedGodItem(player, enchantment.getKey(), enchantment.getValue(), armor.getType(), false);
							}
						}
					}
				}
			}
		}
	}

	public void blockedGodItem(Player p, Enchantment ench, int level, Material mat, boolean high)
	{
		p.sendMessage(FormatUtil.format(
				"&4Enchantment &f{0} &4with level &f{1} &4has been removed from item with type &f{2} &4because its level was too &f{3}&4.",
				FormatUtil.getFriendlyName(ench.getName()), level, FormatUtil.getFriendlyName(mat), high ? "high" : "low"));
	}

	public void blockedRegularItem(Player player, Material mat)
	{
		player.sendMessage(FormatUtil.format("&4Item with type: &f{0} &4is not allowed and has been removed from your inventory.",
				FormatUtil.getFriendlyName(mat)));
	}

	@Override
	public void reload()
	{
		// TODO Auto-generated method stub
		
	}
}