package com.BASeCamp.GPEnderHoppers;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.Hopper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

public class HopperHandler implements Listener, Runnable {
    private GPEnderHopper Owner = null;
    private ArrayList<HopperHolder> toRun = new ArrayList<HopperHolder>();

    public GPEnderHopper getOwner() {
        return Owner;
    }

    public HopperHandler(GPEnderHopper pOwner) {
        Owner = pOwner;
        //Run this a tick later so that the dataStore is populated.
        Bukkit.getScheduler().scheduleSyncDelayedTask(pOwner, new Runnable() {
            @Override
            public void run() {
                for (World w : Bukkit.getWorlds()) {
                    for (Chunk c : w.getLoadedChunks()) {
                        for (BlockState te : c.getTileEntities()) {
                            if (te instanceof Hopper) addHopper((Hopper)te);
                        }
                    }
                }
            }
        });

        Bukkit.getScheduler().scheduleSyncRepeatingTask(Owner, this, 20, Owner.config.getInt("EnderHoppers.DelayTicks", 60));
    }
    
    public void addHopper(Hopper hop) {
        HopperHolder hh = HopperHolder.getHolder(hop);
        if(hh != null) toRun.add(hh);
    }
    public void removeHopper(Hopper hop) {
        HopperHolder hh = HopperHolder.getHolder(hop);
        if(hh == null) return;
        hh.destroy();
        toRun.remove(hh);
    }

    public void run() {
        for(HopperHolder hold: toRun) hold.handle();
    }
    
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent ev) {
        if (ev.getBlockPlaced().getType() == Material.HOPPER) addHopper((Hopper) ev.getBlock().getState());
        //TODO: Handle Ender Chest events
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent ev) {
        if (ev.getBlock().getType().equals(Material.HOPPER)) removeHopper((Hopper)ev.getBlock().getState());
        //TODO: Handle Ender Chest events
    }

    @EventHandler
    public void OnChunkUnload(ChunkUnloadEvent ev) {
        for (BlockState te : ev.getChunk().getTileEntities()) {
            if (te instanceof Hopper) addHopper((Hopper) te);
        }
    }

    @EventHandler
    public void OnChunkLoad(ChunkLoadEvent ev) {
        for (BlockState te : ev.getChunk().getTileEntities()) {
            if (te instanceof Hopper) addHopper((Hopper) te);
        }

    }

}
