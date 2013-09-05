package com.BASeCamp.GPEnderHoppers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Scanner;

/**
 * used by GPEnderHoppers to track specific EnderHopper settings for each claim.
 * 
 * @author BC_Programming
 * 
 * 
 */
public class ClaimData {

    private boolean HopperPush = false;
    private boolean HopperPull = false;

    public boolean getHopperPush() {
        return HopperPush;
    }

    public void setHopperPush(boolean value) {
        HopperPush = value;
    }

    public boolean getHopperPull() {
        return HopperPull;
    }

    public void setHopperPull(boolean value) {
        HopperPull = value;
    }

    private GPEnderHopper Owner;
    public static String ClaimDataFolder;
    private long ClaimID; // id of the claim.

    public GPEnderHopper getOwner() {
        return Owner;
    }

    /**
     * returns the ID of the claim this ClaimData is associated with.
     * 
     * @return ClaimID this Data is associated with.
     */
    public long getClaimID() {
        return ClaimID;
    }

    public ClaimData(GPEnderHopper pOwner, long ForClaim) {
        Owner = pOwner;
        ClaimDataFolder = Owner.getDataFolder().getPath()  + File.separator + "claimdata";
        ClaimID = ForClaim;
        Read();
    }

    public void Read() {
        String sourcefile = Owner.getDataFolder().getPath() + File.separator + String.valueOf(ClaimID) + ".dat";
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

    public void Save() {
        // persist to a file.
        // we will save to ClaimDataFolder, within a file <claimID>.dat
        String targetfile = Owner.getDataFolder().getPath() + File.separator + String.valueOf(ClaimID) + ".dat";
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
