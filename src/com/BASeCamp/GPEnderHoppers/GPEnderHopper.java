package com.BASeCamp.GPEnderHoppers;
import java.util.logging.Logger;

import me.ryanhamshire.GriefPrevention.GriefPrevention;
import nl.rutgerkok.betterenderchest.BetterEnderChest;

import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;


public class GPEnderHopper extends JavaPlugin {
	
	GriefPrevention gp = null;
	HopperHandler hh = null;
	HopperCommand hc = null;
	public BetterEnderChest becPlugin = null;
	public static Logger log = Logger.getLogger("Minecraft");
	public Configuration config;
	
	
	@Override
	public void onDisable(){
		if(hh!=null)
			hh.saveData();
	}
	
	@Override
	public void onEnable(){
		log.info("GPEnderHopper Loading...");
		try {
			gp = (GriefPrevention) Bukkit.getPluginManager().getPlugin("GriefPrevention");
		} catch(Exception e) {
			log.severe("Found a GriefPrevention plugin but it is not of the right class!");
		}
		if(gp == null) {
			log.severe("GPEnderHopper: Could not find GriefPrevention. Disabling!");
			getPluginLoader().disablePlugin(this);
			return;
		}
		try {
			becPlugin = (BetterEnderChest) Bukkit.getPluginManager().getPlugin("BetterEnderChest");
		} catch(Exception e) {
			log.severe("Found a GriefPrevention plugin but it is not of the right class!");
		}
		saveDefaultConfig();
		config = getConfig();
		
		//register for Hopper Events.
		hh = new HopperHandler(this);
		Bukkit.getPluginManager().registerEvents(hh, this);
		
		hc = new HopperCommand(this);
		getCommand("claimecpull").setExecutor(hc);
		getCommand("claimecpush").setExecutor(hc);
		
		
	}
	

}
