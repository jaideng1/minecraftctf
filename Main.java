package me.ctf.cblocksurprise;


import java.util.ArrayList;
import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.md_5.bungee.api.ChatColor;

public class Main extends JavaPlugin implements Listener{
	String version = "v1.0";
	
	ItemStack redChestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
	ItemStack redHelmet = new ItemStack(Material.LEATHER_HELMET);
	
	ItemStack blueChestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
	ItemStack blueHelmet = new ItemStack(Material.LEATHER_HELMET);
	
	ItemStack redf = new ItemStack(Material.RED_BANNER);
	ItemStack bluef = new ItemStack(Material.BLUE_BANNER);
	
	Material red = redf.getType();
	Material blue = bluef.getType();
	
	@Override
	public void onEnable() {
		getLogger().info("CTF enabled, " + version);
		getLogger().info("CTF by CBlockSurprise");
		getLogger().info("Download the map at https://cblocksurprise.github.io");
		
		
		//Set colors of chestplates
		LeatherArmorMeta meta = (LeatherArmorMeta) redChestplate.getItemMeta();
		meta.setColor(Color.RED);
		redChestplate.setItemMeta(meta);
		
		meta = (LeatherArmorMeta) redHelmet.getItemMeta();
		meta.setColor(Color.RED);
		redHelmet.setItemMeta(meta);
		
		meta = (LeatherArmorMeta) blueChestplate.getItemMeta();
		meta.setColor(Color.BLUE);
		blueChestplate.setItemMeta(meta);
		
		meta = (LeatherArmorMeta) blueHelmet.getItemMeta();
		meta.setColor(Color.BLUE);
		blueHelmet.setItemMeta(meta);
		
		ItemMeta itemMeta = redf.getItemMeta();
		itemMeta.setDisplayName(ChatColor.RED + "Red Team's Flag");
		ArrayList<String> lore = new ArrayList<String>();
		lore.add("Get this to your team's base!");
		itemMeta.setLore(lore);
		redf.setItemMeta(itemMeta);
		
		itemMeta = bluef.getItemMeta();
		itemMeta.setDisplayName(ChatColor.BLUE + "Blue Team's Flag");
		lore = new ArrayList<String>();
		lore.add("Get this to your team's base!");
		itemMeta.setLore(lore);
		bluef.setItemMeta(itemMeta);
		
		getServer().getPluginManager().registerEvents(this, this);
	}
	
	
	@Override
	public void onDisable() {
		getLogger().info("CTF disabled.");
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		Player player = (Player) event.getWhoClicked();
		if(event.getSlotType() == SlotType.ARMOR && isPlaying && isCTFPlayer(player)) {
			event.setCancelled(true);  
			player.sendMessage(ChatColor.GREEN + "Keep your eyes on the prize!");
        }
	}
	
	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		
		if (event.getEntity() instanceof Player) { //Change
			Player damaged = (Player) event.getEntity();
			if (isPlaying && isCTFPlayer(damaged)) {
				if (getPlayerTeam(damaged) != "") {
					damaged.sendMessage(ChatColor.RED + "You've been hit! Your health: " + Math.floor(damaged.getHealth()));
					if (damaged.getHealth() < 1) {
						event.setCancelled(true);
						damaged.setHealth(20);
						damaged.teleport(deathCamp);
						damaged.sendMessage(ChatColor.RED + "You've been sent to JAIL!");
					}
				} else {
					event.setCancelled(true);
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerItemDamage(PlayerItemDamageEvent event) {
		if (isCTFPlayer(event.getPlayer())) {
			event.setCancelled(true);
		}
	}
	
	
	@EventHandler
	public void onEntityRegainHealth(EntityRegainHealthEvent event) {
		if (event.getEntity() instanceof Player) {
			Player p = (Player) event.getEntity();
			if (isPlaying && isCTFPlayer(p)) {
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onFoodLevelChange(FoodLevelChangeEvent event) {
		Player p = (Player) event.getEntity();
		if (isCTFPlayer(p) && isPlaying) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		Player player = event.getPlayer();
		
		if (isCTFPlayer(player)) {
			event.setCancelled(true);
			//player.getInventory().addItem(new ItemStack(event.getBlockPlaced().getType()));
		}
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		if (isCTFPlayer(event.getPlayer())) {
			if (event.getBlock().getType() == Material.RED_BANNER && getPlayerTeam(event.getPlayer()) == "BLUE") {
				event.getBlock().setType(Material.AIR);
				event.getPlayer().getInventory().addItem(redf);
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "title @a subtitle {\"text\":\"BLUE has taken the RED flag\",\"color\":\"blue\"}");
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "title @a title {\"text\":\"!!!\",\"color\":\"blue\"}");
			}
			if (event.getBlock().getType() == Material.BLUE_BANNER && getPlayerTeam(event.getPlayer()) == "RED") {
				event.getBlock().setType(Material.AIR);
				event.getPlayer().getInventory().addItem(bluef);
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "title @a subtitle {\"text\":\"RED has taken the BLUE flag\",\"color\":\"red\"}");
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "title @a title {\"text\":\"!!!\",\"color\":\"red\"}");
			}
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Location point1blue = new Location(_world, -456, 68, -2);
		Location point2blue = new Location(_world,-450, 78, -8);
		Location point1red = new Location(_world,-456, 68, -74);
		Location point2red = new Location(_world,-450,78,-81);
		if (isInRect(event.getPlayer(), point1red, point2red) && isCTFPlayer(event.getPlayer()) && getPlayerTeam(event.getPlayer()) == "RED") {
			event.getPlayer().teleport(redBase);
			if (!event.getPlayer().getInventory().getItemInMainHand().isSimilar(bluef)) {
				event.getPlayer().sendMessage(ChatColor.RED+"You can't enter this area! If you have the flag, have it in your main hand!");
				
			} else {
				reset();
				Bukkit.broadcastMessage(ChatColor.DARK_RED + "The RED team has won!");
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "title @a title {\"text\":\"RED WON\",\"color\":\"red\"}");
			}
			
		}
		if (isInRect(event.getPlayer(), point1blue, point2blue) && isCTFPlayer(event.getPlayer()) && getPlayerTeam(event.getPlayer()) == "BLUE") {
			event.getPlayer().teleport(blueBase);
			if (!event.getPlayer().getInventory().getItemInMainHand().isSimilar(redf)) {
				event.getPlayer().sendMessage(ChatColor.RED+"You can't enter this area! If you have the flag, have it in your main hand!");
			} else {
				reset();
				Bukkit.broadcastMessage(ChatColor.DARK_BLUE + "The BLUE team has won!");
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "title @a title {\"text\":\"BLUE WON\",\"color\":\"blue\"}");
			}
		}
	}
	
	Inventory null_shop = Bukkit.createInventory(null, 9, "Item Shop");
	
	@EventHandler
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
		if (event.getRightClicked() instanceof Villager) {
			if (isCTFPlayer(event.getPlayer())) {
				event.setCancelled(true);
				
				ItemStack invis = new ItemStack(Material.POTION,1);
				ItemMeta im = invis.getItemMeta();
				im.setDisplayName("Invisibility (0:30)");
				invis.setItemMeta(im);
				
				ItemStack strength = new ItemStack(Material.POTION,1);
				ItemMeta sm = strength.getItemMeta();
				im.setDisplayName("Strength (0:30)");
				strength.setItemMeta(sm);
				
				ItemStack ench = new ItemStack(Material.ENCHANTED_BOOK,1);
				EnchantmentStorageMeta em = (EnchantmentStorageMeta) ench.getItemMeta();
				em.addEnchant(Enchantment.DAMAGE_ALL, 1, false);
				em.addEnchant(Enchantment.THORNS, 1, false);
				em.addEnchant(Enchantment.MULTISHOT, 1, false);
				ench.setItemMeta(em);
				
				Inventory inv = Bukkit.createInventory(null, 9, "Item Shop");
				inv.setItem(0, new ItemStack(Material.STONE_SWORD, 1));
				inv.setItem(1, new ItemStack(Material.IRON_SWORD, 1));
				inv.setItem(2, new ItemStack(Material.BOW, 1));
				inv.setItem(3, new ItemStack(Material.ARROW, 8));
				inv.setItem(4, invis); // TODO change the potions + enchanted book
				inv.setItem(5, strength); //<- invis ^strength
				inv.setItem(6, new ItemStack(Material.WHITE_WOOL, 16));
				inv.setItem(7, new ItemStack(Material.ENDER_PEARL, 1));
				inv.setItem(8, ench);
				event.getPlayer().openInventory(inv);
			}
		}
	}
	
	@EventHandler
	public void onInventoryClickShop(InventoryClickEvent event) {
		Player player = (Player) event.getWhoClicked(); 
		ItemStack clicked = event.getCurrentItem();
		
		Inventory inventory = event.getInventory();
		if (clicked == null) {return;}
		if (inventory == null) {return;}
		int two = null_shop.getSize();
		int one = inventory.getSize();
		
		if (two == one) {
			if (clicked.getType() == Material.STONE_SWORD) {
				event.setCancelled(true);
				player.closeInventory();
				player.getInventory().addItem(new ItemStack(Material.STONE_SWORD, 1));
			}
			if (clicked.getType() == Material.IRON_SWORD) {
				event.setCancelled(true);
				player.closeInventory();
				player.getInventory().addItem(new ItemStack(Material.IRON_SWORD, 1));
			}
			if (clicked.getType() == Material.BOW) {
				event.setCancelled(true);
				player.closeInventory();
				player.getInventory().addItem(new ItemStack(Material.BOW, 1));
			}
			if (clicked.getType() == Material.ARROW) {
				event.setCancelled(true);
				player.closeInventory();
				player.getInventory().addItem(new ItemStack(Material.ARROW, 8));
			}
			if (clicked.getType() == Material.POTION) {
				event.setCancelled(true);
				player.closeInventory();
				player.getInventory().addItem(new ItemStack(Material.POTION, 1));
			}
//			if (clicked.getType() == Material.POTION) { change for each potion
//				event.setCancelled(true);
//				player.closeInventory();
//				player.getInventory().addItem(new ItemStack(Material.POTION, 1));
//			}
			if (clicked.getType() == Material.WHITE_WOOL) {
				event.setCancelled(true);
				player.closeInventory();
				player.getInventory().addItem(new ItemStack(Material.WHITE_WOOL, 16));
			}
			if (clicked.getType() == Material.ENDER_PEARL) {
				event.setCancelled(true);
				player.closeInventory();
				player.getInventory().addItem(new ItemStack(Material.ENDER_PEARL, 1));
			}
			if (clicked.getType() == Material.ENCHANTED_BOOK) {
				ItemStack ench = new ItemStack(Material.ENCHANTED_BOOK,1);
				EnchantmentStorageMeta em = (EnchantmentStorageMeta) ench.getItemMeta();
				em.addEnchant(Enchantment.DAMAGE_ALL, 1, false);
				em.addEnchant(Enchantment.THORNS, 1, false);
				em.addEnchant(Enchantment.MULTISHOT, 1, false);
				ench.setItemMeta(em);
				event.setCancelled(true);
				player.closeInventory();
				player.getInventory().addItem(ench);
			}
		}
	}
	
	public boolean isInRect(Player player, Location loc1, Location loc2)
	{
	    double[] dim = new double[2];
	 
	    dim[0] = loc1.getX();
	    dim[1] = loc2.getX();
	    Arrays.sort(dim);
	    if(player.getLocation().getX() > dim[1] || player.getLocation().getX() < dim[0])
	        return false;
	 
	    dim[0] = loc1.getZ();
	    dim[1] = loc2.getZ();
	    Arrays.sort(dim);
	    if(player.getLocation().getZ() > dim[1] || player.getLocation().getZ() < dim[0])
	        return false;
	    
	    dim[0] = loc1.getY();
	    dim[1] = loc2.getY();
	    Arrays.sort(dim);
	    if(player.getLocation().getY() > dim[1] || player.getLocation().getY() < dim[0])
	        return false;
	 	 
	    return true;
	}
	
	public boolean isCTFPlayer(Player p) {
		for (Player p1 : redTeam) {
			if (p1.getName() == p.getName()) {
				return true;
			}
		}
		for (Player p2 : blueTeam) {
			if (p2.getName() == p.getName()) {
				return true;
			}
		}
		
		return false;
	}
	
	public String getPlayerTeam(Player p) {
		for (Player p1 : redTeam) {
			if (p1.getName() == p.getName()) {
				return "RED";
			}
		}
		for (Player p2 : blueTeam) {
			if (p2.getName() == p.getName()) {
				return "BLUE";
			}
		}
		
		return "";
	}
	
	World _world = Bukkit.getWorlds().get(0);
	
	Location redBase = new Location(_world,-460.5,68,-78.5);
	Location redFlag = new Location(_world,-453.5,69,-77.5);
	Location blueBase = new Location(_world,-460.5,68,-4.5);
	Location blueFlag = new Location(_world,-453.5,69,-4.5);
	Location deathCamp = new Location(_world,-451.5,74,6.5);
	
	//ArrayList<Location> spawnItemNodes = new ArrayList<Location>();
	
	void makeTeams() {
		blueFlag.getBlock().setType(Material.BLUE_BANNER);
		redFlag.getBlock().setType(Material.RED_BANNER);
		int i = 0;
		for (Player p : Bukkit.getOnlinePlayers()) {
			
			if (i < teamSize * 2) {
				p.getInventory().clear();
				if (i % 2 == 0) {
					redTeam.add(p);
					p.teleport(redBase);
					p.getInventory().setChestplate(redChestplate);
					p.getInventory().setHelmet(redHelmet);
					
				} else {
					blueTeam.add(p);
					p.teleport(blueBase);
					p.getInventory().setChestplate(blueChestplate);
					p.getInventory().setHelmet(blueHelmet);
				}
				p.setHealth(20);
				p.setFoodLevel(20);
				PotionEffect haste = new PotionEffect(PotionEffectType.FAST_DIGGING,75000,255);
				p.addPotionEffect(haste);
				p.setGameMode(GameMode.SURVIVAL);
			}
			i++;
		}
	}
	
	public void reset() {
		redTeam = new ArrayList<Player>();
		blueTeam = new ArrayList<Player>();
		isPlaying = false;
	}
	
	ArrayList<Player> redTeam = new ArrayList<Player>();
	ArrayList<Player> blueTeam = new ArrayList<Player>();
	
	boolean isPlaying = false;
	int teamSize = 5;
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			if (cmd.getName().equalsIgnoreCase("startgame") && player.isOp()) {
				if (!isPlaying) {
					Bukkit.broadcastMessage(ChatColor.GREEN + "Starting new Capture The Flag game!");
					player.performCommand("title @a title {\"text\":\"Starting Game!\",\"color\":\"green\"}");
					isPlaying = true;
					makeTeams();
				} else {
					player.sendMessage(ChatColor.RED + "There's already a game on!");
				}
				
				return true;
			}
			if (cmd.getName().equalsIgnoreCase("stopgame") && player.isOp()) {
				if (isPlaying) {
					Bukkit.broadcastMessage(ChatColor.RED + "Capture The Flag game has been stopped.");
					player.performCommand("title @a title {\"text\":\"Stopping Game\",\"color\":\"red\"}");
					reset();
				} else {
					player.sendMessage(ChatColor.RED + "There is not a game active right now.");
				}
				return true;
			}
			if (cmd.getName().equalsIgnoreCase("setteamsize") && player.isOp()) {
				teamSize = Integer.parseInt(args[0]);
				player.sendMessage(ChatColor.RED + "Set team size to: " + teamSize);
				return true;
			}
			
		}
		return false; 
	}
	
}
