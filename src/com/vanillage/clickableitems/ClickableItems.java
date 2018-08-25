package com.vanillage.clickableitems;

import java.util.Map;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.vanillage.bukkitutils.configuration.LocationConfiguration;

public class ClickableItems extends JavaPlugin implements Listener {
	private LocationConfiguration locationConfiguration;
	
	@Override
	public void onEnable() {
		getConfig().options().copyDefaults(true);
		saveDefaultConfig();
		saveConfig();
		locationConfiguration = new LocationConfiguration(getConfig().getConfigurationSection("clickable-items"), this);
		getServer().getPluginManager().registerEvents(this, this);
		getLogger().info(getDescription().getFullName() + " enabled");
	}
	
	@Override
	public void onDisable() {
		getLogger().info(getDescription().getFullName() + " disabled");
	}
	
	public void onReload() {
		reloadConfig();
		locationConfiguration = new LocationConfiguration(getConfig().getConfigurationSection("clickable-items"), this);
		getLogger().info(getDescription().getFullName() + " reloaded");
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("clickableitems")) {
			if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
				if (sender.hasPermission("clickableitems.clickableitems.reload")) {
					onReload();
					sender.sendMessage("ClickableItems config reloaded.");
				} else {
					String permissionMessage = (String) getDescription().getCommands().get("clickableitems").get("permission-message");
					
					if (permissionMessage != null) {
						permissionMessage.replace("<permission>", "clickableitems.clickableitems.reload");
						sender.sendMessage(permissionMessage);
					}
				}
				
				return true;
			}
		}
		
		return false;
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Action action = event.getAction();
		String actionName;
		Player player = event.getPlayer();
		Location playerLocation;
		
		if ((action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK || action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) && locationConfiguration.getBoolean(playerLocation = player.getLocation(), (actionName = event.getAction().name()) + ".enable-player-location")) {
			Location playerEyeLocation = player.getEyeLocation();
			double searchRange = locationConfiguration.getDouble(playerLocation, actionName + ".search-range");
			Item item = null;
			double distanceSquared = Double.POSITIVE_INFINITY;
			
			for (Entity entity : playerLocation.getWorld().getNearbyEntities(playerEyeLocation, searchRange, searchRange, searchRange)) {
				Location entityLocation;
				
				if (entity instanceof Item && locationConfiguration.getBoolean(entityLocation = entity.getLocation(playerLocation), actionName + ".enable-item-location")) {
					Location offsettedEntityLocation = entity.getLocation().add(0.0d, locationConfiguration.getDouble(entityLocation, actionName + ".offset"), 0.0d);
					double distanceSquaredTemp = offsettedEntityLocation.distanceSquared(playerEyeLocation);
					
					if (distanceSquaredTemp < distanceSquared) {
						double accuracy = locationConfiguration.getDouble(entityLocation, actionName + ".accuracy");
						accuracy = 1.0d / Math.sqrt(1.0d + 1.0d / (accuracy * accuracy * distanceSquaredTemp) - 1.0d / distanceSquaredTemp);
						
						if (offsettedEntityLocation.subtract(playerEyeLocation).toVector().normalize().dot(playerEyeLocation.getDirection().normalize()) > accuracy) {
							item = (Item) entity;
							distanceSquared = distanceSquaredTemp;
						}
					}
				}
			}
			
			if (item != null) {
				getServer().getPluginManager().callEvent(new PlayerClickItemEvent(event, item));
			}
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerClickItem(PlayerClickItemEvent event) {
		getLogger().info(event.getPlayer().getName() + " clicked " + event.getItem().getItemStack().toString());
		
		for (Map<?, ?> command : locationConfiguration.getMapList(event.getItem().getLocation(), ".commands")) {
			if (command != null) {
				//TODO: check all options from the Map<?, ?> command and replace all placeholders
				Object permission = command.get("permission");
				
				if (permission instanceof String && event.getPlayer().hasPermission((String) permission) || !(permission instanceof String)) {
					Object commandLine = command.get("command");
					
					if (commandLine instanceof String) {
						CommandSender commandSender = "player".equals(command.get("sender")) ? event.getPlayer() : getServer().getConsoleSender();
						
						getServer().dispatchCommand(commandSender, ((String) commandLine).replace("<player>", event.getPlayer().getName()).replace("<type>", event.getItem().getItemStack().getType().toString()));
					}
				}
			}
		}
	}
}
