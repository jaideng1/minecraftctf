package me.ctf.cblocksurprise;


import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatColor;

public class Main extends JavaPlugin implements Listener{
	String version = "v1.0";
	
	ItemStack redChestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
	ItemStack redHelmet = new ItemStack(Material.LEATHER_HELMET);
	
	ItemStack blueChestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
	ItemStack blueHelmet = new ItemStack(Material.LEATHER_HELMET);
	
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
					damaged.sendMessage(ChatColor.RED + "You've been hit! Your health: " + damaged.getHealth());
					if (damaged.getHealth() < 1) {
						event.setCancelled(true);
						damaged.setHealth(10);
						damaged.sendMessage(ChatColor.RED + "You've been sent to JAIL!");
					}
				} else {
					event.setCancelled(true);
				}
			}
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
	
	Location redBase = new Location(_world,-478.5,68,-105.5);
	Location redFlag = new Location(_world,-471.5,69,-105.5);
	Location blueBase = new Location(_world,-478.5,68,-4.5);
	Location blueFlag = new Location(_world,-470.5,69,-4.5);
	Location deathCamp = new Location(_world,-465.5,69,4.5);
	
	//ArrayList<Location> spawnItemNodes = new ArrayList<Location>();
	
	void makeTeams() {
		int i = 0;
		for (Player p : Bukkit.getOnlinePlayers()) {
			
			if (i < teamSize * 2) {
				p.getInventory().clear();
				if (i % 2 == 0) {
					redTeam.add(p);
					p.teleport(redBase);
				} else {
					blueTeam.add(p);
					p.teleport(blueBase);
				}
				p.setGameMode(GameMode.SURVIVAL);
			}
			i++;
		}
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
					redTeam = new ArrayList<Player>();
					blueTeam = new ArrayList<Player>();
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
