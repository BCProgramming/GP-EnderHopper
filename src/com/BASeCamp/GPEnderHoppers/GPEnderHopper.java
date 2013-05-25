package com.BASeCamp.GPEnderHoppers;
import java.util.logging.Logger;

import me.ryanhamshire.GriefPrevention.GriefPrevention;
import nl.rutgerkok.betterenderchest.BetterEnderChest;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;


public class GPEnderHopper extends JavaPlugin {
	
	GriefPrevention gp = null;
	HopperHandler hh = null;
	HopperCommand hc = null;
	public Configuration cfg = null;
	public BetterEnderChest becPlugin = null;
	public static Logger log = Logger.getLogger("Minecraft");
	
	
	@Override
	public void onDisable(){
		if(hh!=null)
			hh.saveData();
	}
	
	@Override
	public void onEnable(){
		log.info("GPEnderHopper Loading...");
		for(Plugin p: Bukkit.getPluginManager().getPlugins()){
			if(p instanceof GriefPrevention){
				gp = (GriefPrevention)p;
				log.info("GPEnderHopper:GriefPrevention found!");
				break;
			}
			else if(p instanceof BetterEnderChest){
				log.info("GPEnderHopper:BetterEnderChest Found!");
				becPlugin = (BetterEnderChest) p;
			}
		}
		
		if(gp==null){
			log.info("GPEnderHopper:GriefPrevention not found!");
			return;
		}
		cfg = new Configuration(this);
		//register for Hopper Events.
		hh = new HopperHandler(this);
		Bukkit.getPluginManager().registerEvents(hh, this);
		
		hc = new HopperCommand(this);
		getCommand("claimecpull").setExecutor(hc);
		getCommand("claimecpush").setExecutor(hc);
		
		
	}
	

}
