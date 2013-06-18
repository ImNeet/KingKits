package net.bukkit.faris.kingkits.guis;

import net.bukkit.faris.kingkits.KingKits;
import net.bukkit.faris.kingkits.helpers.KitStack;
import net.bukkit.faris.kingkits.hooks.Plugin;
import net.bukkit.faris.kingkits.listeners.commands.SetKit;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class GuiMenu implements Listener {

	private KingKits thePlugin = null;
	private Player thePlayer = null;
	private String guiTitle = null;
	private KitStack[] guiKitStacks = null;
	private Inventory guiInventory = null;

	/** 
	 * Create a new gui menu instance.
	 * @param player - The player that is using the menu
	 * @param title - The title of the menu
	 * @param kitStacks - The kits in the menu
	 */
	public GuiMenu(Player player, String title, KitStack[] kitStacks) {
		this.thePlugin = Plugin.getPlugin();
		this.thePlayer = player;
		this.guiTitle = title;
		this.guiKitStacks = kitStacks;

		this.clickedItem = false;

		if (Plugin.isInitialised()) Bukkit.getPluginManager().registerEvents(this, Plugin.getPlugin());
		else Bukkit.getPluginManager().registerEvents(this, Bukkit.getPluginManager().getPlugin("KingKits"));
	}

	/** Opens the menu for the player **/
	public void openMenu() {
		try {
			this.closeMenu(false);

			int menuSize = 36;
			if (this.guiKitStacks.length > 32) menuSize = 45;
			Inventory menuInventory = this.thePlayer.getServer().createInventory(null, menuSize, this.guiTitle);
			for (int i = 0; i < this.guiKitStacks.length; i++) {
				try {
					ItemStack currentStack = this.guiKitStacks[i].getItemStack();
					if (currentStack != null) {
						if (currentStack.getType() != Material.AIR) {
							if (currentStack.getItemMeta() != null) {
								ItemMeta itemMeta = currentStack.getItemMeta();
								ChatColor kitColour = this.thePlayer.hasPermission("kingkits.kits." + this.guiKitStacks[i].getKitName().toLowerCase()) ? ChatColor.GREEN : ChatColor.DARK_RED;
								itemMeta.setDisplayName(ChatColor.RESET + "" + kitColour + this.guiKitStacks[i].getKitName());
								currentStack.setItemMeta(itemMeta);
							}
							menuInventory.addItem(currentStack);
						}
					}
				} catch (Exception ex) {
					continue;
				}
			}
			this.guiInventory = menuInventory;
			this.thePlayer.openInventory(menuInventory);
		} catch (Exception ex) {
		}
	}

	/** Closes the menu for the player and unregisters the event **/
	public void closeMenu(boolean unregisterEvents) {
		try {
			this.thePlayer.closeInventory();
			if (unregisterEvents) {
				InventoryClickEvent.getHandlerList().unregister(this);
			}
		} catch (Exception ex) {
		}
	}

	/** Returns the player that is opening the menu **/
	public Player getPlayer() {
		return this.thePlayer;
	}

	/** Sets the player that is opening the menu **/
	public GuiMenu setPlayer(Player player) {
		this.thePlayer = player;
		return this;
	}

	/** Returns the title of the menu **/
	public String getTitle() {
		return this.guiTitle;
	}

	/** Sets the title of the menu **/
	public GuiMenu setTitle(String title) {
		this.guiTitle = title;
		return this;
	}

	/** Returns the kit item stacks **/
	public KitStack[] getKitStacks() {
		return this.guiKitStacks;
	}

	/** Sets the kit item stacks **/
	public GuiMenu setKitStacks(KitStack[] kitStacks) {
		this.guiKitStacks = kitStacks;
		return this;
	}

	/** Returns if the player has clicked in the inventory **/
	private boolean clickedItem = false;

	/** Handles when a player clicks an item **/
	@EventHandler
	protected void onPlayerClickSlot(InventoryClickEvent event) {
		try {
			if (this.guiInventory != null && event.getInventory() != null && event.getWhoClicked() != null) {
				if (event.getWhoClicked() instanceof Player) {
					if (event.getSlot() >= 0) {
						if (event.getSlotType() == SlotType.CONTAINER) {
							if (event.getInventory().getTitle().equals(this.guiInventory.getTitle())) {
								event.setCurrentItem(null);
								event.setCancelled(true);
								if (!this.clickedItem) {
									if (this.guiKitStacks.length >= event.getSlot()) SetKit.setKitPlayerKit(this.thePlugin, (Player) event.getWhoClicked(), this.guiKitStacks[event.getSlot()].getKitName());
								}
								this.clickedItem = true;
								this.closeMenu(true);
							}
						}
					}
				}
			}
		} catch (Exception ex) {
			if (event.getInventory() != null && this.guiInventory != null) {
				if (event.getInventory().getTitle().equals(this.guiInventory.getTitle())) {
					event.setCurrentItem(null);
					event.setCancelled(true);
					this.closeMenu(true);
				}
			}
		}
	}

}
