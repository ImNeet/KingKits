package net.bukkit.faris.kingkits.listeners.commands;

import net.bukkit.faris.kingkits.KingKits;
import net.bukkit.faris.kingkits.listeners.PlayerCommand;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class RefillCommand extends PlayerCommand {

	public RefillCommand(KingKits instance) {
		super(instance);
	}

	@Override
	protected boolean onCommand(Player p, String command, String[] args) {
		if (command.equalsIgnoreCase("refill") || command.equalsIgnoreCase("soup")) {
			try {
				if (p.hasPermission(this.getPlugin().permissions.refillSoupSingle) || p.hasPermission(this.getPlugin().permissions.refillSoupAll)) {
					if (this.getPlugin().cmdValues.refillKits) {
						if (this.getPlugin().configValues.pvpWorlds.contains("All") || this.getPlugin().configValues.pvpWorlds.contains(p.getWorld().getName())) {
							if (this.getPlugin().configValues.quickSoupKitOnly) {
								if (!this.getPlugin().usingKits.containsKey(p.getName())) {
									p.sendMessage(ChatColor.RED + "You have not chosen a kit.");
									return true;
								}
							}
							if (args.length == 0) {
								if (p.hasPermission(this.getPlugin().permissions.refillSoupSingle)) {
									if (p.getInventory().getItemInHand() != null) {
										if (p.getInventory().getItemInHand().getType() == Material.BOWL) {
											ItemStack itemInHand = p.getInventory().getItemInHand();
											int amount = itemInHand.getAmount();
											if (amount <= 1) {
												p.getInventory().setItemInHand(new ItemStack(Material.MUSHROOM_SOUP, 1));
											} else {
												itemInHand.setAmount(amount - 1);
												p.getInventory().setItemInHand(itemInHand);
												p.getInventory().addItem(new ItemStack(Material.MUSHROOM_SOUP, 1));
											}
											if (this.getPlugin().configValues.vaultValues.useEconomy && this.getPlugin().configValues.vaultValues.useCostPerRefill) {
												try {
													net.milkbowl.vault.economy.Economy economy = (net.milkbowl.vault.economy.Economy) this.getPlugin().vault.getEconomy();
													if (economy.hasAccount(p.getName())) {
														double cost = this.getPlugin().configValues.vaultValues.costPerRefill;
														if (economy.getBalance(p.getName()) >= cost) {
															economy.withdrawPlayer(p.getName(), cost);
															p.sendMessage(this.getPlugin().getEconomyMessage(cost));
														} else {
															p.sendMessage(ChatColor.GREEN + "You do not have enough money to refill your bowl.");
															return true;
														}
													} else {
														p.sendMessage(ChatColor.GREEN + "You do not have enough money to refill your bowl.");
														return true;
													}
												} catch (Exception ex) {
												}
											}
										} else {
											p.sendMessage(ChatColor.RED + "You must have a bowl in your hand.");
										}
									} else {
										p.sendMessage(ChatColor.RED + "You must have a bowl in your hand.");
									}
								} else {
									this.sendNoAccess(p);
								}
							} else if (args.length == 1) {
								if (args[0].equalsIgnoreCase("all")) {
									if (p.hasPermission(this.getPlugin().permissions.refillSoupAll)) {
										int amountOfBowls = 0;
										ItemStack[] itemContents = p.getInventory().getContents();
										for (int i = 0; i < itemContents.length; i++) {
											try {
												ItemStack item = itemContents[i];
												if (item != null) {
													if (item.getType() == Material.BOWL) {
														itemContents[i] = new ItemStack(Material.MUSHROOM_SOUP, item.getAmount());
														amountOfBowls += item.getAmount();
													}
												}
											} catch (Exception ex) {
												continue;
											}
										}
										if (amountOfBowls == 0) {
											p.sendMessage(ChatColor.RED + "You have no bowls!");
											return true;
										}
										if (this.getPlugin().configValues.vaultValues.useEconomy && this.getPlugin().configValues.vaultValues.useCostPerRefill) {
											try {
												net.milkbowl.vault.economy.Economy economy = (net.milkbowl.vault.economy.Economy) this.getPlugin().vault.getEconomy();
												if (economy.hasAccount(p.getName())) {
													double cost = this.getPlugin().configValues.vaultValues.costPerRefill * amountOfBowls;
													if (economy.getBalance(p.getName()) >= cost) {
														economy.withdrawPlayer(p.getName(), cost);
														p.sendMessage(this.getPlugin().getEconomyMessage(cost));
													} else {
														p.sendMessage(ChatColor.GREEN + "You do not have enough money to refill all your bowls.");
														return true;
													}
												} else {
													p.sendMessage(ChatColor.GREEN + "You do not have enough money to refill all your bowls.");
													return true;
												}
											} catch (Exception ex) {
											}
										}
										p.getInventory().setContents(itemContents);
									} else {
										this.sendNoAccess(p);
									}
								} else {
									p.sendMessage(ChatColor.RED + "Usage: " + ChatColor.DARK_RED + "/" + command.toLowerCase() + " [<all>]");
								}
							} else {
								p.sendMessage(ChatColor.RED + "Usage: " + ChatColor.DARK_RED + "/" + command.toLowerCase() + " [<all>]");
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
			} catch (Exception ex) {
				p.sendMessage(ChatColor.RED + "An unexpected error occured.");
			}
			return true;
		}
		return false;
	}

}
