package net.bukkit.faris.kingkits.listeners.commands;

import java.util.ArrayList;
import java.util.List;

import net.bukkit.faris.kingkits.KingKits;
import net.bukkit.faris.kingkits.guis.GuiMenu;
import net.bukkit.faris.kingkits.helpers.KitStack;
import net.bukkit.faris.kingkits.listeners.PlayerCommand;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class KitCommand extends PlayerCommand {

	public KitCommand(KingKits instance) {
		super(instance);
	}

	@Override
	protected boolean onCommand(Player p, String command, String[] args) {
		if (command.equalsIgnoreCase("pvpkit")) {
			if (p.hasPermission(this.getPlugin().permissions.kitUseCommand)) {
				if (this.getPlugin().cmdValues.pvpKits) {
					if (this.getPlugin().configValues.pvpWorlds.contains("All") || this.getPlugin().configValues.pvpWorlds.contains(p.getWorld().getName())) {
						if (args.length == 0) {
							if (!this.getPlugin().configValues.kitListMode.equalsIgnoreCase("Gui")) {
								List<String> kitList = new ArrayList<String>();
								if (this.getPlugin().getKitsConfig().contains("Kits")) {
									kitList = this.getPlugin().getKitsConfig().getStringList("Kits");
								}
								p.sendMessage(r("&aKits List (" + kitList.size() + "):"));
								if (!kitList.isEmpty()) {
									for (int kitPos = 0; kitPos < kitList.size(); kitPos++) {
										String kitName = kitList.get(kitPos).split(" ")[0];
										if (p.hasPermission("kingkits.kits." + kitName.toLowerCase())) {
											p.sendMessage(r("&6" + (kitPos + 1) + ". " + kitName));
										} else {
											if (this.getPlugin().configValues.cmdKitListPermissions) p.sendMessage(r("&4" + (kitPos + 1) + ". " + kitName));
										}
									}
								} else {
									p.sendMessage(r("&4There are no kits"));
								}
							} else {
								List<String> kitList = new ArrayList<String>();
								if (this.getPlugin().getKitsConfig().contains("Kits")) kitList = this.getPlugin().getKitsConfig().getStringList("Kits");
								KitStack[] kitStacks = new KitStack[kitList.size()];
								boolean modifiedConfig = false;
								for (int i = 0; i < kitList.size(); i++) {
									try {
										String kitName = kitList.get(i);
										try {
											if (kitName.contains(" ")) {
												kitName = kitName.split(" ")[0];
											}
										} catch (Exception ex) {
										}
										ItemStack itemStack = new ItemStack(Material.DIAMOND_SWORD, 1);
										if (this.getPlugin().getGuiItemsConfig().contains(kitName)) itemStack = new ItemStack(this.getPlugin().getGuiItemsConfig().getInt(kitName), 1);
										else {
											this.getPlugin().getGuiItemsConfig().set(kitName, itemStack.getType().getId());
											modifiedConfig = true;
										}
										kitStacks[i] = new KitStack(kitName, itemStack);
									} catch (Exception ex) {
										continue;
									}
								}
								if (modifiedConfig) {
									this.getPlugin().saveGuiItemsConfig();
									this.getPlugin().reloadGuiItemsConfig();
								}
								ChatColor menuColour = !kitList.isEmpty() ? ChatColor.DARK_BLUE : ChatColor.RED;
								new GuiMenu(p, menuColour + "PvP Kits", kitStacks).openMenu();
							}
						} else if (args.length == 1) {
							String kitName = args[0];
							List<String> kitList = this.getPlugin().getKitsConfig().getStringList("Kits");
							List<String> kitListLC = new ArrayList<String>();
							for (int pos0 = 0; pos0 < kitList.size(); pos0++) {
								kitListLC.add(kitList.get(pos0).toLowerCase());
							}
							if (kitListLC.contains(kitName.toLowerCase())) {
								kitName = kitList.get(kitListLC.indexOf(kitName.toLowerCase()));
							}
							try {
								SetKit.setKitPlayerKit(this.getPlugin(), p, kitName);
							} catch (Exception ex) {
								ex.printStackTrace();
							}
						} else {
							p.sendMessage(r("&cUsage: &4/" + command.toLowerCase() + " [<kit>]"));
						}
					} else {
						p.sendMessage(ChatColor.RED + "You cannot use this command in this world.");
					}
				} else {
					p.sendMessage(ChatColor.RED + "This command is disabled in the configuration.");
				}
			} else {
				this.sendNoAccess(p);
			}
			return true;
		}
		return false;
	}

}
