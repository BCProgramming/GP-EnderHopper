package com.BASeCamp.GPEnderHoppers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Scanner;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import me.ryanhamshire.GriefPrevention.Claim;


/**
 * used by GPEnderHoppers to track specific EnderHopper settings for each claim.
 * @author BC_Programming
 * 
 * 
 */
public class ClaimData {

	private boolean HopperPush = false;
	private boolean HopperPull = false;
	
	
	public boolean getHopperPush(){ return HopperPush;}
	public void setHopperPush(boolean value){HopperPush = value;}
	public boolean getHopperPull() { return HopperPull;}
	public void setHopperPull(boolean value){HopperPull = value;}
	
	private GPEnderHopper Owner;
	public static String ClaimDataFolder = Configuration.dataLayerFolderPath + File.separator + "claimdata";
	private long ClaimID; //id of the claim.
	
	
	
	
	
	public GPEnderHopper getOwner(){ return Owner;}
	
	/**
	 * returns the ID of the claim this ClaimData is associated with.
	 * @return ClaimID this Data is associated with.
	 */
	public long getClaimID(){return ClaimID;}
	
	public ClaimData(GPEnderHopper pOwner,Claim ForClaim){
		Owner=pOwner;
		ClaimID = ForClaim.getID();
		Read(ForClaim);
	}
	
	public void Read(Claim readforclaim){
		
		FileConfiguration readsource = Owner.gp.getMetaHandler().getClaimMeta("EnderHopper", readforclaim);
		YamlConfiguration outConfig = new YamlConfiguration();
		
		HopperPush = readsource.getBoolean("EnderHopper.Push",true);
		HopperPull = readsource.getBoolean("EnderHopper.Pull",true);
		outConfig.set("EnderHopper.Push",HopperPush);
		outConfig.set("EnderHopper.Pull",HopperPull);
		Owner.gp.getMetaHandler().setClaimMeta("EnderHopper", readforclaim,outConfig);
		
		
		
	}

	public void Save(){
		if(Owner==null){
			System.out.println("Owner is null.");
			return;
		}
		if(Owner.ds==null){
			System.out.println("dataStore is null.");
			return;
		}
		Claim targetclaim = Owner.ds.getClaim(ClaimID);
		if(targetclaim==null) return;
		YamlConfiguration outConfig = new YamlConfiguration();
		outConfig.set("EnderHopper.Push", HopperPush);
		outConfig.set("EnderHopper.Pull",HopperPull);
		Owner.gp.getMetaHandler().setClaimMeta("EnderHopper", targetclaim,outConfig);
		
	}
	
}
