package com.BASeCamp.GPEnderHoppers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Scanner;
import java.util.logging.Level;

import me.ryanhamshire.GriefPrevention.Claim;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

/**
 * used by GPEnderHoppers to track specific EnderHopper settings for each claim.
 * 
 * @author BC_Programming
 * 
 * 
 */
public class ClaimData {
    public static File claimFolder;
    private static HashMap<Long, ClaimData> cachedData = new HashMap<Long, ClaimData>();
    private static boolean useMeta = false;
    
    public static ClaimData getClaimData(long claimID) {
        if(!cachedData.containsKey(claimID)) {
            cachedData.put(claimID, new ClaimData(claimID));
        }
        return cachedData.get(claimID);
    }
    public static ClaimData getClaimData(Claim claim) {
        if(!cachedData.containsKey(claim.getID())) {
            cachedData.put(claim.getID(), new ClaimData(claim));
        }
        return cachedData.get(claim.getID());
    }
    
    public static void closeAll() {
        cachedData.clear(); //Should cause all to be not referenced and thus collected in the GC
    }
    
    public static void setFolder(Plugin p)  {
        claimFolder = new File(p.getDataFolder(), "claimdata");
        try {
            GPEnderHopper.gp.getClass().getMethod("getMetaHandler");
            GPEnderHopper.log.info("Using GP Meta");
            Bukkit.getScheduler().scheduleSyncDelayedTask(p, new Runnable() {
                @Override
                public void run() {
                    convertAll();
                }
            });
            useMeta = true;
        } catch(Exception e) {
            GPEnderHopper.log.info("Using files");
            useMeta = false;
            if(!claimFolder.exists()) claimFolder.mkdir();
        }
    }
    
    public static void convertAll() {//Flat file -> meta handler.
        if(!claimFolder.exists()) return;
        if(!claimFolder.isDirectory()) return;
        GPEnderHopper.log.info("Converting files.");
        for(File f: claimFolder.listFiles()) {
            try {
                if(f.isDirectory()) continue;
                if(!f.getName().endsWith(".dat")) continue;
                GPEnderHopper.log.info("Converting "+f.getName());
                long id = Long.parseLong(f.getName().split("\\.")[0]);
                useMeta = false;
                ClaimData data = getClaimData(id);
                data.read();
                useMeta = true;
                data.save();
                f.delete();
            } catch(Exception e) {
                GPEnderHopper.log.log(Level.SEVERE,"Could not convert file '"+f.getName()+"'!", e);
            }
        }
        claimFolder.delete();
        closeAll();
    }
    
    private boolean HopperPush = false;
    private boolean HopperPull = false;
    private long ClaimID; // id of the claim.
    private Claim claim = null; //Do not fill unless neccesary

    private ClaimData(long ForClaim) {
        ClaimID = ForClaim;
        read();
    }
    private ClaimData(Claim ForClaim) {
        ClaimID = ForClaim.getID();
        claim = ForClaim;
        read();
    }

    /**
     * returns the ID of the claim this ClaimData is associated with.
     * 
     * @return ClaimID this Data is associated with.
     */
    public long getClaimID() {
        return ClaimID;
    }
    
    public Claim getClaim() {
        if(claim == null) claim = GPEnderHopper.gp.dataStore.getClaim(ClaimID);
        return claim;
    }

    public boolean getHopperPush() {
        return HopperPush;
    }

    public void setHopperPush(boolean value) {
        HopperPush = value;
        save();
    }

    public boolean getHopperPull() {
        return HopperPull;
    }

    public void setHopperPull(boolean value) {
        HopperPull = value;
        save();
    }

    private void read() {
        if(useMeta) {
            FileConfiguration conf = GPEnderHopper.gp.getMetaHandler().getClaimMeta("EnderHopper", getClaim());
            HopperPush = conf.getBoolean("EnderHopper.Push", false);
            HopperPull = conf.getBoolean("EnderHopper.Pull", false);
            return;
        }
        File getf = new File(claimFolder, String.valueOf(ClaimID) + ".dat");
        if (getf.exists()) {
            try {
                Scanner s = new Scanner(getf);
                // read in the Push and Pull data.
                String readpush = s.next();
                String readpull = s.next();
                HopperPush = Boolean.parseBoolean(readpush);
                HopperPull = Boolean.parseBoolean(readpull);
                s.close();
            } catch (IOException ex) {
                GPEnderHopper.log.log(Level.SEVERE, "Exception in load()", ex);
            }

        }
    }

    private void save() {
        if(useMeta) {
            YamlConfiguration conf = new YamlConfiguration();
            conf.set("EnderHopper.Push", HopperPush);
            conf.set("EnderHopper.Pull", HopperPull);
            GPEnderHopper.gp.getMetaHandler().setClaimMeta("EnderHopper", getClaim(), conf);
            return;
        }
        // persist to a file.
        // we will save to ClaimDataFolder, within a file <claimID>.dat
        File getf = new File(claimFolder, String.valueOf(ClaimID) + ".dat");
        // new File(targetfile).mkdirs();
        try {
            Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(getf)));
            writer.write(String.valueOf(HopperPush) + "\n");
            writer.write(String.valueOf(HopperPull) + "\n");
            writer.close();
        } catch(Exception e) {
            GPEnderHopper.log.log(Level.SEVERE, "Exception in save()", e);
        }

    }

}
