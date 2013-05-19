package com.BASeCamp.GPEnderHoppers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import me.ryanhamshire.GriefPrevention.DataStore;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class Configuration {
	private GPEnderHopper Owner;
	protected final static String dataLayerFolderPath = "plugins" + File.separator + "EnderHoppers";
	final static String configFilePath = dataLayerFolderPath + File.separator + "config.yml";
	
	public int EnderHopperDelayTicks = 20*3;
	public List<String> WhiteList;
	public Configuration(GPEnderHopper _Owner){
		FileConfiguration config = YamlConfiguration.loadConfiguration(new File(configFilePath));
		FileConfiguration outConfig = new YamlConfiguration();
		EnderHopperDelayTicks = config.getInt("EnderHoppers.DelayTicks",3*20);
		outConfig.set("EnderHoppers.DelayTicks", EnderHopperDelayTicks);
		System.out.println("EnderHopper Delay Ticks" + EnderHopperDelayTicks);

		try {
			outConfig.save(configFilePath);
			
		}
		catch(IOException ex){
			//hmmm...
		}
		
	}
}
