package net.bukkit.faris.kingkits.listeners.event;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.bukkit.faris.kingkits.KingKits;
import net.bukkit.faris.kingkits.listeners.commands.SetKit;
import net.bukkit.faris.kingkits.listeners.event.custom.PlayerKillEvent;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PlayerListener implements Listener {
	private final KingKits plugin;

	/** Create an instance PlayerListener **/
	public PlayerListener(KingKits instance) {
		this.plugin = instance;
	}

	/** Register custom kill event **/
	@EventHandler(priority = EventPriority.HIGHEST)
	public void registerKillEvent(PlayerDeathEvent event) {
		try {
			if (event.getEntity() != null) {
				if (event.getEntity().getKiller() != null) {
					if (event.getEntity().getName() != event.getEntity().getKiller().getName()) event.getEntity().getServer().getPluginManager().callEvent(new PlayerKillEvent(event.getEntity().getKiller(), event.getEntity()));
				}
			}
		} catch (Exception ex) {
		}
	}

	/** Lists all the available kits when a player joins **/
	@EventHandler
	public void listKitsOnJoin(final PlayerJoinEvent event) {
		try {
			if (this.getPlugin().configValues.listKitsOnJoin) {
				if (event.getPlayer() != null) {
					if (this.getPlugin().configValues.pvpWorlds.contains("All") || this.getPlugin().configValues.pvpWorlds.contains(event.getPlayer().getWorld().getName())) {
						final Player p = event.getPlayer();
						p.getServer().getScheduler().scheduleSyncDelayedTask(this.getPlugin(), new Runnable() {
							public void run() {
								List<String> kitList = new ArrayList<String>();
								if (getPlugin().getKitsConfig().contains("Kits")) kitList = getPlugin().getKitsConfig().getStringList("Kits");
								String kits = ChatColor.GREEN + "";
								if (kitList.isEmpty()) {
									kits += ChatColor.DARK_RED + "No kits made.";
								} else {
									for (int kitPos = 0; kitPos < kitList.size(); kitPos++) {
										String kit = kitList.get(kitPos);
										ChatColor col = ChatColor.GREEN;
										boolean ignoreKit = false;
										if (getPlugin().configValues.kitListPermissions) {
											if (!p.hasPermission("kingkits.kits." + kit.toLowerCase())) ignoreKit = true;
										}
										if (!ignoreKit) {
											if (kitPos == kitList.size() - 1) kits += col + kit;
											else kits += col + kit + ", ";
										} else {
											if (kitPos == kitList.size() - 1) kits = replaceLast(kits, ",", "");
										}
									}
								}
								if (kits == ChatColor.GREEN + "") {
									kits = ChatColor.RED + "No kits available";
								}
								p.sendMessage(ChatColor.GOLD + "PvP Kits: " + kits);
							}
						}, 30L);
					}
				}
			}
		} catch (Exception ex) {
		}
	}

	/** Lets players create a sign kit **/
	@EventHandler
	public void createKitSign(PlayerInteractEvent event) {
		try {
			if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
				if (event.getPlayer().getWorld() != null) {
					if (this.getPlugin().configValues.pvpWorlds.contains("All") || this.getPlugin().configValues.pvpWorlds.contains(event.getPlayer().getWorld().getName())) {
						Player p = event.getPlayer();
						BlockState block = event.getClickedBlock().getState();
						if ((block instanceof Sign)) {
							Sign sign = (Sign) block;
							String SignType = sign.getLine(0);
							if (SignType.equalsIgnoreCase("[Kit]")) {
								if (p.hasPermission(this.getPlugin().permissions.kitCreateSign)) {
									sign.setLine(0, ChatColor.BLACK + "[" + ChatColor.DARK_BLUE + "®Kit" + ChatColor.BLACK + "]");
									sign.update(true);
								} else {
									p.sendMessage(ChatColor.DARK_RED + "You do not have access to do this.");
									sign.setLine(0, ChatColor.BLACK + "[" + ChatColor.RED + "Kit" + ChatColor.BLACK + "]");
									sign.update(true);
								}
								event.setCancelled(true);
							}
						}
					}
				}
			}
		} catch (Exception ex) {
		}
	}

	/** Lets player's use sign kits **/
	@EventHandler
	public void changeKits(PlayerInteractEvent event) {
		try {
			if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
				if (event.getPlayer().getWorld() != null) {
					if (this.getPlugin().configValues.pvpWorlds.contains("All") || this.getPlugin().configValues.pvpWorlds.contains(event.getPlayer().getWorld().getName())) {
						Player p = event.getPlayer();
						BlockState block = event.getClickedBlock().getState();
						if ((block instanceof Sign)) {
							Sign sign = (Sign) block;
							String SignType = sign.getLine(0);
							if (SignType.equalsIgnoreCase(ChatColor.BLACK + "[" + ChatColor.DARK_BLUE + "®Kit" + ChatColor.BLACK + "]")) {
								if (p.hasPermission(this.getPlugin().permissions.kitUseSign)) {
									String line1 = sign.getLine(1);
									if (line1 != null) {
										if (!line1.equalsIgnoreCase("")) {
											List<String> kitList = getPlugin().getKitsConfig().getStringList("Kits");
											List<String> kitListLC = new ArrayList<String>();
											for (int pos1 = 0; pos1 < kitList.size(); pos1++) {
												kitListLC.add(kitList.get(pos1).toLowerCase());
											}
											if (kitListLC.contains(line1.toLowerCase())) {
												String kitName = kitList.get(kitListLC.indexOf(line1.toLowerCase()));
												try {
													SetKit.setKitPlayerKit(this.getPlugin(), p, kitName);
												} catch (Exception e) {
													p.sendMessage(ChatColor.RED + "Error while trying to set your kit. Try using /pvpkit if it doesn't work.");
												}
											} else {
												p.sendMessage(ChatColor.RED + "Unknown kit " + ChatColor.DARK_RED + line1 + ChatColor.RED + ".");
												sign.setLine(0, ChatColor.BLACK + "[" + ChatColor.RED + "Kit" + ChatColor.BLACK + "]");
												sign.update(true);
											}
										} else {
											p.sendMessage(ChatColor.RED + "Sign incorrectly set up.");
											sign.setLine(0, ChatColor.BLACK + "[" + ChatColor.RED + "Kit" + ChatColor.BLACK + "]");
											sign.update(true);
										}
									} else {
										p.sendMessage(ChatColor.RED + "Sign incorrectly set up.");
										sign.setLine(0, ChatColor.BLACK + "[" + ChatColor.RED + "Kit" + ChatColor.BLACK + "]");
										sign.update(true);
									}
								} else {
									p.sendMessage(ChatColor.RED + "You do not have permission to use this sign.");
								}
								event.setCancelled(true);
							}
						}
					}
				}
			}
		} catch (Exception ex) {
		}
	}

	/** Remove a player's kit when they die **/
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		try {
			Player p = event.getEntity();
			if (this.getPlugin().playerKits.containsKey(p.getName())) {
				this.getPlugin().playerKits.remove(p.getName());
				if (!this.getPlugin().configValues.dropItemsOnDeath) event.getDrops().clear();
			}
			if (this.getPlugin().usingKits.containsKey(p.getName())) {
				this.getPlugin().usingKits.remove(p.getName());
				if (!this.getPlugin().configValues.dropItemsOnDeath) event.getDrops().clear();
			}
			if (this.getPlugin().hasKit.containsKey(p.getName())) this.getPlugin().hasKit.remove(p.getName());
			p.getInventory().clear();
			p.getInventory().setArmorContents(null);
		} catch (Exception ex) {
		}
	}

	/** Remove a player's kit when they leave **/
	@EventHandler
	public void removeKitOnQuit(PlayerQuitEvent event) {
		try {
			Player p = event.getPlayer();
			if (this.getPlugin().configValues.removeItemsOnLeave) {
				if (this.getPlugin().playerKits.containsKey(p.getName()) || getPlugin().usingKits.containsKey(p.getName())) {
					p.getInventory().clear();
					p.getInventory().setArmorContents(null);
				}
			}
			if (this.getPlugin().playerKits.containsKey(p.getName())) this.getPlugin().playerKits.remove(p.getName());
			if (this.getPlugin().usingKits.containsKey(p.getName())) this.getPlugin().usingKits.remove(p.getName());
			if (this.getPlugin().hasKit.containsKey(p.getName())) this.getPlugin().hasKit.remove(p.getName());
		} catch (Exception ex) {
		}
	}

	/** Remove a player's kit when they get kicked **/
	@EventHandler
	public void removeKitOnKick(PlayerKickEvent event) {
		try {
			Player p = event.getPlayer();
			if (event.getPlayer().getWorld() != null) {
				if (this.getPlugin().configValues.pvpWorlds.contains("All") || this.getPlugin().configValues.pvpWorlds.contains(event.getPlayer().getWorld().getName())) {
					if (this.getPlugin().configValues.removeItemsOnLeave) {
						if (this.getPlugin().playerKits.containsKey(p.getName()) || getPlugin().usingKits.containsKey(p.getName())) {
							p.getInventory().clear();
							p.getInventory().setArmorContents(null);
						}
					}
				}
			}
			if (this.getPlugin().playerKits.containsKey(p.getName())) this.getPlugin().playerKits.remove(p.getName());
			if (this.getPlugin().usingKits.containsKey(p.getName())) this.getPlugin().usingKits.remove(p.getName());
			if (this.getPlugin().hasKit.containsKey(p.getName())) this.getPlugin().hasKit.remove(p.getName());
		} catch (Exception ex) {
		}
	}

	/** Bans item dropping **/
	@EventHandler
	public void banDropItem(PlayerDropItemEvent event) {
		try {
			if (event.getItemDrop() != null) {
				if (event.getPlayer().getWorld() != null) {
					if (!this.getPlugin().configValues.dropItems) {
						if (this.getPlugin().configValues.pvpWorlds.contains("All") || this.getPlugin().configValues.pvpWorlds.contains(event.getPlayer().getWorld().getName())) {
							if (this.getPlugin().configValues.opBypass) {
								if (!event.getPlayer().isOp()) {
									if (this.getPlugin().playerKits.containsKey(event.getPlayer().getName())) {
										event.getPlayer().sendMessage(ChatColor.RED + "You cannot drop items when using a kit.");
										event.setCancelled(true);
									}
								}
							} else {
								if (this.getPlugin().usingKits.containsKey(event.getPlayer().getName())) {
									event.getPlayer().sendMessage(ChatColor.RED + "You cannot drop items when using a kit.");
									event.setCancelled(true);
								}
							}
						}
					}
				}
			}
		} catch (Exception ex) {
		}
	}

	/** Adds score to a player every time they kill another player **/
	@EventHandler
	public void addScore(PlayerDeathEvent event) {
		try {
			if (event.getEntityType() == EntityType.PLAYER) {
				if (this.getPlugin().configValues.scores) {
					if (this.getPlugin().configValues.pvpWorlds.contains("All") || this.getPlugin().configValues.pvpWorlds.contains(event.getEntity().getWorld().getName())) {
						Player killer = event.getEntity().getKiller();
						if (killer != null) {
							try {
								if (!this.getPlugin().playerScores.containsKey(killer.getName())) this.getPlugin().playerScores.put(killer.getName(), 0);
								int currentScore = (int) this.getPlugin().playerScores.get(killer.getName());
								int newScore = currentScore + this.getPlugin().configValues.scoreIncrement;
								this.getPlugin().playerScores.remove(killer.getName());
								if (newScore > this.getPlugin().configValues.maxScore) newScore = this.getPlugin().configValues.maxScore;
								this.getPlugin().playerScores.put(killer.getName(), newScore);
							} catch (Exception ex) {
							}
						}
					}
				}
			}
		} catch (Exception ex) {
		}
	}

	/** Makes players have a chat prefix with their score **/
	@EventHandler
	public void scoreChat(AsyncPlayerChatEvent event) {
		try {
			if (this.getPlugin().configValues.scores) {
				if (this.getPlugin().configValues.pvpWorlds.contains("All") || this.getPlugin().configValues.pvpWorlds.contains(event.getPlayer().getWorld().getName())) {
					Player p = event.getPlayer();
					if (!this.getPlugin().playerScores.containsKey(p.getName())) this.getPlugin().playerScores.put(p.getName(), 0);
					event.setFormat(this.getPlugin().replaceAllColours(this.getPlugin().configValues.scoreFormat).replaceAll("<score>", String.valueOf(this.getPlugin().playerScores.get(p.getName()))) + " " + event.getFormat());
				}
			}
		} catch (Exception ex) {
		}
	}

	/** Removes potion effects of a player when they leave **/
	@EventHandler
	public void leaveRemovePotionEffects(PlayerQuitEvent event) {
		try {
			if (this.getPlugin().configValues.removePotionEffectsOnLeave) {
				if (event.getPlayer().getWorld() != null) {
					if (this.getPlugin().configValues.pvpWorlds.contains("All") || this.getPlugin().configValues.pvpWorlds.contains(event.getPlayer().getWorld().getName())) {
						for (PotionEffect potionEffectOnPlayer : event.getPlayer().getActivePotionEffects()) {
							PotionEffectType potionEffectType = potionEffectOnPlayer.getType();
							event.getPlayer().removePotionEffect(potionEffectType);
						}
					}
				} else {
					for (PotionEffect potionEffectOnPlayer : event.getPlayer().getActivePotionEffects()) {
						PotionEffectType potionEffectType = potionEffectOnPlayer.getType();
						event.getPlayer().removePotionEffect(potionEffectType);
					}
				}
			}
		} catch (Exception ex) {
		}
	}

	/** Removes potion effects of a player when they get kicked **/
	@EventHandler
	public void kickRemovePotionEffects(PlayerKickEvent event) {
		try {
			if (this.getPlugin().configValues.removePotionEffectsOnLeave) {
				if (event.getPlayer().getWorld() != null) {
					if (this.getPlugin().configValues.pvpWorlds.contains("All") || this.getPlugin().configValues.pvpWorlds.contains(event.getPlayer().getWorld().getName())) {
						for (PotionEffect potionEffectOnPlayer : event.getPlayer().getActivePotionEffects()) {
							PotionEffectType potionEffectType = potionEffectOnPlayer.getType();
							event.getPlayer().removePotionEffect(potionEffectType);
						}
					}
				} else {
					for (PotionEffect potionEffectOnPlayer : event.getPlayer().getActivePotionEffects()) {
						PotionEffectType potionEffectType = potionEffectOnPlayer.getType();
						event.getPlayer().removePotionEffect(potionEffectType);
					}
				}
			}
		} catch (Exception ex) {
		}
	}

	/** Makes it so when you right click with a compass, you track the nearest player **/
	@EventHandler
	public void rightClickCompass(PlayerInteractEvent event) {
		try {
			if (this.getPlugin().configValues.rightClickCompass) {
				if (this.getPlugin().configValues.pvpWorlds.contains("All") || this.getPlugin().configValues.pvpWorlds.contains(event.getPlayer().getWorld().getName())) {
					if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
						if (event.getPlayer().getInventory().getItemInHand() != null) {
							if (event.getPlayer().getInventory().getItemInHand().getType() == Material.COMPASS) {
								if (event.getPlayer().hasPermission(this.getPlugin().permissions.rightClickCompass) || event.getPlayer().isOp()) {
									Player nearestPlayer = null;
									double distance = -1D;
									for (Player target : event.getPlayer().getServer().getOnlinePlayers()) {
										if (!target.getName().equalsIgnoreCase(event.getPlayer().getName())) {
											if (event.getPlayer().getLocation().getWorld().getName().equalsIgnoreCase(target.getLocation().getWorld().getName())) {
												if (distance == -1D) {
													distance = event.getPlayer().getLocation().distance(target.getLocation());
													nearestPlayer = target;
												} else {
													if (event.getPlayer().getLocation().distance(target.getLocation()) < distance) {
														distance = event.getPlayer().getLocation().distance(target.getLocation());
														nearestPlayer = target;
													}
												}
											}
										}
									}
									if (nearestPlayer != null) {
										event.getPlayer().setCompassTarget(nearestPlayer.getLocation());
										event.getPlayer().sendMessage(ChatColor.YELLOW + "Your compass is pointing at " + nearestPlayer.getName() + ".");
										if (this.getPlugin().compassTargets.containsKey(event.getPlayer())) this.getPlugin().compassTargets.remove(event.getPlayer());
										this.getPlugin().compassTargets.put(event.getPlayer().getPlayer(), nearestPlayer.getPlayer());
									} else {
										event.getPlayer().setCompassTarget(event.getPlayer().getWorld().getSpawnLocation());
										event.getPlayer().sendMessage(ChatColor.YELLOW + "Your compass is pointing at spawn.");
									}
								}
							}
						}
					}
				}
			}
		} catch (Exception ex) {
		}
	}

	/** Makes compass trackers track the new location of their target **/
	@EventHandler
	public void compassTrackMove(PlayerMoveEvent event) {
		try {
			if (this.getPlugin().configValues.rightClickCompass) {
				if (this.getPlugin().compassTargets.containsValue(event.getPlayer())) {
					Player tracker = null;
					for (Map.Entry<Player, Player> e : this.getPlugin().compassTargets.entrySet()) {
						Player key = e.getKey();
						Player value = e.getValue();
						if (key != null) {
							if (value != null) {
								if (key.isOnline()) {
									if (value.isOnline()) {
										if (event.getPlayer().getName().equalsIgnoreCase(value.getName())) tracker = key.getPlayer();
									}
								}
							}
						}
					}
					if (tracker != null) tracker.setCompassTarget(event.getPlayer().getLocation());
				}
			}
		} catch (Exception ex) {
		}
	}

	/** Makes compass trackers lose their target when they get leave or their target leaves **/
	@EventHandler
	public void compassTrackerLeave(PlayerQuitEvent event) {
		try {
			if (this.getPlugin().configValues.rightClickCompass) {
				if (this.getPlugin().compassTargets.containsValue(event.getPlayer())) {
					Player tracker = null;
					for (Map.Entry<Player, Player> e : this.getPlugin().compassTargets.entrySet()) {
						Player key = e.getKey();
						Player value = e.getValue();
						if (key != null) {
							if (value != null) {
								if (key.isOnline()) {
									if (value.isOnline()) {
										if (event.getPlayer().getName().equalsIgnoreCase(value.getName())) tracker = key.getPlayer();
									}
								}
							}
						}
					}
					if (tracker != null) this.getPlugin().compassTargets.remove(tracker);
				}
				if (this.getPlugin().compassTargets.containsKey(event.getPlayer())) this.getPlugin().compassTargets.remove(event.getPlayer());
			}
		} catch (Exception ex) {
		}
	}

	/** Makes compass trackers lose their target when they get kicked or their target gets kicked **/
	@EventHandler
	public void compassTrackerKick(PlayerKickEvent event) {
		try {
			if (this.getPlugin().configValues.rightClickCompass) {
				if (this.getPlugin().compassTargets.containsValue(event.getPlayer())) {
					Player tracker = null;
					for (Map.Entry<Player, Player> e : this.getPlugin().compassTargets.entrySet()) {
						Player key = e.getKey();
						Player value = e.getValue();
						if (key != null) {
							if (value != null) {
								if (key.isOnline()) {
									if (value.isOnline()) {
										if (event.getPlayer().getName().equalsIgnoreCase(value.getName())) tracker = key.getPlayer();
									}
								}
							}
						}
					}
					if (tracker != null) this.getPlugin().compassTargets.remove(tracker);
				}
				if (this.getPlugin().compassTargets.containsKey(event.getPlayer())) this.getPlugin().compassTargets.remove(event.getPlayer());
			}
		} catch (Exception ex) {
		}
	}

	/** Allows players to right click with soup and instantly drink it to be healed or fed **/
	@EventHandler
	public void quickSoup(PlayerInteractEvent event) {
		try {
			if (event.getItem() != null) {
				if (event.getAction() != null) {
					if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
						if (event.getItem().getType() == Material.MUSHROOM_SOUP) {
							if (this.getPlugin().configValues.quickSoup) {
								if (event.getPlayer().hasPermission(this.getPlugin().permissions.quickSoup) || (this.getPlugin().configValues.opBypass && event.getPlayer().isOp())) {
									if (this.getPlugin().configValues.pvpWorlds.contains("All") || this.getPlugin().configValues.pvpWorlds.contains(event.getPlayer().getWorld().getName())) {
										Player p = event.getPlayer();
										int soupAmount = p.getInventory().getItemInHand().getAmount();
										if (soupAmount == 1) {
											if (p.getHealth() < 20) {
												if (p.getHealth() + 5 > 20) p.setHealth(20);
												else p.setHealth(p.getHealth() + 5);
											} else if (p.getFoodLevel() < 20) {
												if (p.getFoodLevel() + 4 > 20) p.setFoodLevel(20);
												else p.setFoodLevel(p.getFoodLevel() + 4);
											} else {
												return;
											}
											p.getInventory().setItemInHand(new ItemStack(Material.BOWL, 1));
											event.setCancelled(true);
										} else if (soupAmount > 0) {
											if (p.getHealth() < 20) {
												if (p.getHealth() + 5 > 20) p.setHealth(20);
												else p.setHealth(p.getHealth() + 5);
											} else if (p.getFoodLevel() < 20) {
												if (p.getFoodLevel() + 4 > 20) p.setFoodLevel(20);
												else p.setFoodLevel(p.getFoodLevel() + 4);
											} else {
												return;
											}
											int newAmount = soupAmount - 1;
											ItemStack newItem = p.getInventory().getItemInHand();
											newItem.setAmount(newAmount);
											p.getInventory().setItemInHand(newItem);
											p.getInventory().addItem(new ItemStack(Material.BOWL));
											event.setCancelled(true);
										}
									}
								}
							}
						}
					}
				}
			}
		} catch (Exception ex) {
		}
	}

	/** Disables block breaking **/
	@EventHandler
	public void disableBlockBreaking(BlockBreakEvent event) {
		try {
			if (this.getPlugin().configValues.banBlockBreakingAndPlacing) {
				if (this.getPlugin().configValues.pvpWorlds.contains("All") || this.getPlugin().configValues.pvpWorlds.contains(event.getPlayer().getWorld().getName())) {
					if (this.getPlugin().configValues.opBypass) {
						if (!event.getPlayer().isOp()) event.setCancelled(true);
					} else {
						event.setCancelled(true);
					}
				}
			}
		} catch (Exception ex) {
		}
	}

	/** Disables block placing **/
	@EventHandler
	public void disableBlockPlacing(BlockPlaceEvent event) {
		try {
			if (this.getPlugin().configValues.banBlockBreakingAndPlacing) {
				if (this.getPlugin().configValues.pvpWorlds.contains("All") || this.getPlugin().configValues.pvpWorlds.contains(event.getPlayer().getWorld().getName())) {
					if (this.getPlugin().configValues.opBypass) {
						if (!event.getPlayer().isOp()) event.setCancelled(true);
					} else {
						event.setCancelled(true);
					}
				}
			}
		} catch (Exception ex) {
		}
	}

	/** Disables minecraft death messages **/
	@EventHandler
	public void disableDeathMessages(PlayerDeathEvent event) {
		try {
			if (event.getEntityType() == EntityType.PLAYER) {
				if (this.getPlugin().configValues.banBlockBreakingAndPlacing) {
					if (this.getPlugin().configValues.disableDeathMessages) event.setDeathMessage("");
				}
			}
		} catch (Exception ex) {
		}
	}

	/** Locks hunger bars so players can't lose hunger **/
	@EventHandler
	public void lockHunger(PlayerMoveEvent event) {
		try {
			if (this.getPlugin().configValues.lockHunger) {
				if (event.getPlayer().getWorld() != null) {
					if (event.getPlayer().getFoodLevel() < event.getPlayer().getMaxHealth()) {
						if (this.getPlugin().configValues.pvpWorlds.contains("All") || this.getPlugin().configValues.pvpWorlds.contains(event.getPlayer().getWorld().getName())) event.getPlayer().setFoodLevel(event.getPlayer().getMaxHealth());
					}
				} else {
					if (event.getPlayer().getFoodLevel() < event.getPlayer().getMaxHealth()) event.getPlayer().setFoodLevel(event.getPlayer().getMaxHealth());
				}
			}
		} catch (Exception ex) {
		}
	}

	/** Gives a player money when they kill another player **/
	@EventHandler
	public void moneyPerKill(PlayerKillEvent event) {
		try {
			Player killer = event.getPlayer();
			if (this.getPlugin().configValues.vaultValues.useEconomy && this.getPlugin().configValues.vaultValues.useMoneyPerKill) {
				if (killer.getWorld() != null) {
					if (this.getPlugin().configValues.pvpWorlds.contains("All") || this.getPlugin().configValues.pvpWorlds.contains(killer.getWorld().getName())) {
						net.milkbowl.vault.economy.Economy economy = (net.milkbowl.vault.economy.Economy) this.getPlugin().vault.getEconomy();
						if (!economy.hasAccount(killer.getName())) economy.createPlayerAccount(killer.getName());
						economy.depositPlayer(killer.getName(), this.getPlugin().configValues.vaultValues.moneyPerKill);
						killer.sendMessage(this.getPlugin().getMPKMessage(event.getDead(), this.getPlugin().configValues.vaultValues.moneyPerKill));
					}
				} else {
					net.milkbowl.vault.economy.Economy economy = (net.milkbowl.vault.economy.Economy) this.getPlugin().vault.getEconomy();
					if (!economy.hasAccount(killer.getName())) economy.createPlayerAccount(killer.getName());
					economy.depositPlayer(killer.getName(), this.getPlugin().configValues.vaultValues.moneyPerKill);
					killer.sendMessage(this.getPlugin().getMPKMessage(event.getDead(), this.getPlugin().configValues.vaultValues.moneyPerKill));
				}
			}
		} catch (Exception ex) {
		}
	}

	/** Takes money from a player when they die by another player **/
	@EventHandler
	public void moneyPerDeath(PlayerDeathEvent event) {
		try {
			if (event.getEntity().getKiller() != null) {
				if (this.getPlugin().configValues.vaultValues.useEconomy && this.getPlugin().configValues.vaultValues.useMoneyPerDeath) {
					if (!event.getEntity().getName().equalsIgnoreCase(event.getEntity().getKiller().getName())) {
						if (event.getEntity().getWorld() != null) {
							if (this.getPlugin().configValues.pvpWorlds.contains("All") || this.getPlugin().configValues.pvpWorlds.contains(event.getEntity().getKiller().getWorld().getName())) {
								net.milkbowl.vault.economy.Economy economy = (net.milkbowl.vault.economy.Economy) this.getPlugin().vault.getEconomy();
								economy.withdrawPlayer(event.getEntity().getName(), this.getPlugin().configValues.vaultValues.moneyPerDeath);
								event.getEntity().sendMessage(this.getPlugin().getMPDMessage(event.getEntity(), this.getPlugin().configValues.vaultValues.moneyPerDeath));
							}
						} else {
							net.milkbowl.vault.economy.Economy economy = (net.milkbowl.vault.economy.Economy) this.getPlugin().vault.getEconomy();
							economy.withdrawPlayer(event.getEntity().getName(), this.getPlugin().configValues.vaultValues.moneyPerDeath);
							event.getEntity().sendMessage(this.getPlugin().getMPDMessage(event.getEntity(), this.getPlugin().configValues.vaultValues.moneyPerDeath));
						}
					}
				}
			}
		} catch (Exception ex) {
		}
	}

	/** Removes the kit and clears a player's inventory when the player changes worlds **/
	@EventHandler
	public void removeKitOnWorldChange(PlayerChangedWorldEvent event) {
		try {
			if (!this.getPlugin().configValues.pvpWorlds.contains("All") && !this.getPlugin().configValues.pvpWorlds.contains(event.getPlayer().getLocation().getWorld().getName())) {
				Player p = event.getPlayer();
				if (this.getPlugin().playerKits.containsKey(p.getName())) this.getPlugin().playerKits.remove(p.getName());
				if (this.getPlugin().usingKits.containsKey(p.getName())) this.getPlugin().usingKits.remove(p.getName());
				if (this.getPlugin().hasKit.containsKey(p.getName())) this.getPlugin().hasKit.remove(p.getName());
				for (PotionEffect potionEffectOnPlayer : event.getPlayer().getActivePotionEffects())
					p.removePotionEffect(potionEffectOnPlayer.getType());
			}
		} catch (Exception ex) {
		}
	}

	/** Disables gamemode changes while using a kit **/
	@EventHandler
	public void disableGamemode(PlayerGameModeChangeEvent event) {
		try {
			if (this.getPlugin().configValues.disableGamemode) {
				if (event.getNewGameMode() != GameMode.SURVIVAL) {
					if (this.plugin.configValues.opBypass && event.getPlayer().isOp()) return;
					if (this.getPlugin().configValues.pvpWorlds.contains("All") || this.getPlugin().configValues.pvpWorlds.contains(event.getPlayer().getLocation().getWorld().getName())) {
						if (this.plugin.usingKits.containsKey(event.getPlayer().getName())) event.setCancelled(true);
					}
				}
			}
		} catch (Exception ex) {
		}
	}

	/** Update killstreaks and runs commands (if they exist in the configuration) when a player kills another player **/
	@EventHandler
	public void updateKillstreak(PlayerKillEvent event) {
		try {
			if (this.getPlugin().configValues.killstreaks) {
				if (this.getPlugin().configValues.pvpWorlds.contains("All") || this.getPlugin().configValues.pvpWorlds.contains(event.getPlayer().getWorld().getName())) {
					if (!this.getPlugin().playerKillstreaks.containsKey(event.getPlayer().getName())) this.getPlugin().playerKillstreaks.put(event.getPlayer().getName(), 0L);
					
					long currentKillstreak = this.getPlugin().playerKillstreaks.get(event.getPlayer().getName());
					if (currentKillstreak + 1L > Long.MAX_VALUE - 1) this.getPlugin().playerKillstreaks.put(event.getPlayer().getName(), 0L);
					else this.getPlugin().playerKillstreaks.put(event.getPlayer().getName(), this.getPlugin().playerKillstreaks.get(event.getPlayer().getName()) + 1L);

					currentKillstreak = this.getPlugin().playerKillstreaks.get(event.getPlayer().getName());
					if (this.getPlugin().getKillstreaksConfig().contains("Killstreak " + currentKillstreak)) {
						List<String> killstreakCommands = this.getPlugin().getKillstreaksConfig().getStringList("Killstreak " + currentKillstreak);
						for (String killstreakCommand : killstreakCommands)
							event.getPlayer().getServer().dispatchCommand(event.getPlayer().getServer().getConsoleSender(), killstreakCommand.replaceAll("<player>", event.getPlayer().getName()).replaceAll("<killstreak>", "" + currentKillstreak));
					}
				}
			}
		} catch (Exception ex) {
		}
	}

	/** Update killstreaks when a player dies **/
	@EventHandler
	public void removeKillstreakOnDeath(PlayerDeathEvent event) {
		try {
			if (this.getPlugin().configValues.killstreaks) {
				if (event.getEntity() != null) {
					if (event.getEntity().getKiller() != null) this.getPlugin().playerKillstreaks.remove(event.getEntity().getName());
				}
			}
		} catch (Exception ex) {
		}
	}

	/** Update killstreaks when a player leaves **/
	@EventHandler
	public void removeKillstreakOnLeave(PlayerQuitEvent event) {
		try {
			if (this.getPlugin().configValues.killstreaks) {
				if (event.getPlayer() != null) this.getPlugin().playerKillstreaks.remove(event.getPlayer().getName());
			}
		} catch (Exception ex) {
		}
	}

	/** Update killstreaks when a player gets kicked **/
	@EventHandler
	public void removeKillstreakOnKick(PlayerKickEvent event) {
		try {
			if (this.getPlugin().configValues.killstreaks) {
				if (event.getPlayer() != null) this.getPlugin().playerKillstreaks.remove(event.getPlayer().getName());
			}
		} catch (Exception ex) {
		}
	}

	/** Gets the plugin instance **/
	private KingKits getPlugin() {
		return this.plugin;
	}

	/** Returns a list of lower case strings **/
	public static List<String> listToLowerCase(List<String> originalMap) {
		List<String> newMap = new ArrayList<String>();
		for (String s : originalMap)
			newMap.add(s.toLowerCase());
		return newMap;
	}

	/** Replaces the last occurrence of a string in a string **/
	private String replaceLast(String text, String original, String replacement) {
		String message = text;
		if (message.contains(original)) {
			StringBuilder stringBuilder = new StringBuilder(text);
			stringBuilder.replace(text.lastIndexOf(original), text.lastIndexOf(original) + 1, replacement);
			message = stringBuilder.toString();
		}
		return message;
	}

}
