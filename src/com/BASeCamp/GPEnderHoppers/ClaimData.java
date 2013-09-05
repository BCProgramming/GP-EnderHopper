package com.BASeCamp.GPEnderHoppers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Scanner;

import org.bukkit.plugin.Plugin;

/**
 * used by GPEnderHoppers to track specific EnderHopper settings for each claim.
 * 
 * @author BC_Programming
 * 
 * 
 */
public class ClaimData {
    public static String ClaimDataFolder;
    private static HashMap<Long, ClaimData> cachedData = new HashMap<Long, ClaimData>();
    
    public static ClaimData getClaimData(long claimID) {
        if(!cachedData.containsKey(claimID)) {
            cachedData.put(claimID, new ClaimData(claimID));
        }
        return cachedData.get(claimID);
    }
    
    public static void closeAll() {
        cachedData.clear(); //Should cause all to be not referenced and thus collected in the GC
    }
    
    public static void setFolder(Plugin p) {
        ClaimDataFolder = p.getDataFolder().getAbsolutePath()  + File.separator + "claimdata";
    }
    
    
    private boolean HopperPush = false;
    private boolean HopperPull = false;
    private long ClaimID; // id of the claim.

    private ClaimData(long ForClaim) {
        ClaimID = ForClaim;
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
        String sourcefile = ClaimDataFolder + File.separator + String.valueOf(ClaimID) + ".dat";
        File getf = new File(sourcefile);
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

            }

        }
    }

    private void save() {
        // persist to a file.
        // we will save to ClaimDataFolder, within a file <claimID>.dat
        String targetfile = ClaimDataFolder + File.separator + String.valueOf(ClaimID) + ".dat";
        // new File(targetfile).mkdirs();
        try {
            Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(targetfile)));
            writer.write(String.valueOf(HopperPush) + "\n");
            writer.write(String.valueOf(HopperPull) + "\n");
            writer.close();
        } catch (FileNotFoundException fnf) {

        } catch (IOException exx) {

        }

    }

}
