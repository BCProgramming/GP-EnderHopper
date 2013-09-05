package com.BASeCamp.GPEnderHoppers;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.logging.Level;

import me.ryanhamshire.GriefPrevention.Claim;
import nl.rutgerkok.betterenderchest.BetterEnderChest;
import nl.rutgerkok.betterenderchest.BetterEnderChestPlugin.PublicChest;
import nl.rutgerkok.betterenderchest.WorldGroup;
import nl.rutgerkok.betterenderchest.chestprotection.ProtectionBridge;
import nl.rutgerkok.betterenderchest.io.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Hopper;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class HopperHandler implements Listener, Runnable {
    private GPEnderHopper Owner = null;
    private LinkedList<Hopper> iteratehoppers = new LinkedList<Hopper>();
    private Inventory ih = null;

    public GPEnderHopper getOwner() {
        return Owner;
    }

    public HopperHandler(GPEnderHopper pOwner) {
        Owner = pOwner;
        // schedule a repeating task to complete, we will perform logic on all
        // Ender chests.
        for (World w : Bukkit.getWorlds()) {
            for (Chunk c : w.getLoadedChunks()) {
                for (BlockState te : c.getTileEntities()) {
                    if (te instanceof Hopper) addHopper((Hopper)te);
                }

            }

        }
        // read in Data.
        // iterate through all claims.

        Bukkit.getScheduler().scheduleSyncRepeatingTask(Owner, this, 20, Owner.config.getInt("EnderHoppers.DelayTicks", 60));
    }
    
    public void addHopper(Hopper hop) {
        iteratehoppers.add(hop);
    }
    public void removeHopper(Hopper hop) {
        iteratehoppers.remove(hop);
    }

    // based on the BetterEnderChest Source code in BEC's Event Handler
    private String getInventoryName(String pPlayer, Block clickedBlock) {
        if (Owner.becPlugin == null)
            return pPlayer;
        Player grabPlayer = Bukkit.getPlayer(pPlayer);

        // Find out the inventory that should be opened
        ProtectionBridge protectionBridge = Owner.becPlugin.getProtectionBridges().getSelectedRegistration();
        if (!protectionBridge.isProtected(clickedBlock) && PublicChest.openOnOpeningUnprotectedChest) {
            // Unprotected Ender chest
            // Get public chest
            if (grabPlayer.hasPermission("betterenderchest.user.open.publicchest")) {
                return BetterEnderChest.PUBLIC_CHEST_NAME;
            } else {
                // Get player's name
                if (grabPlayer.hasPermission("betterenderchest.user.open.privatechest")) {
                    return grabPlayer.getName();
                }
            }
        } else {
            // Protected Ender Chest
            if (protectionBridge.canAccess(grabPlayer, clickedBlock)) {
                // player can access the chest
                if (grabPlayer.hasPermission("betterenderchest.user.open.privatechest")) {
                    // and has the correct permission node
                    // Get the owner's name
                    return protectionBridge.getOwnerName(clickedBlock);
                } else {
                    return null;
                }
            }
        }
        return null;
    }

    public void run() {
        // System.out.println("HopperHandler...");
        // implementation of Runnable for RepeatingSync Task.
        // Iterate through all Hoppers

        Claim cachedClaim = null;
        for (Hopper iterateHopper : iteratehoppers) {
            try {
                if (iterateHopper == null)
                    continue;
                if (iterateHopper.getInventory() == null)
                    continue;
                Inventory HopperInventory = iterateHopper.getInventory();

                if (iterateHopper.getBlock().isBlockPowered())
                    continue; // powered hoppers do not accept or send
                              // items.
                // if the input is a Ender Chest
                // if the hopper is in a claim
                // take an item from the Ender inventory of the claim
                // owner and put it in the hopper if there is room.
                // if The hopper is pointing at a Ender chest
                // and if the hopper is in a claim
                // take an item from the Hopper and put it into the
                // Ender inventory of the claim owner
                // if there is room.
                Block inputblock = getHopperInput(iterateHopper);
                Block outputblock = getHopperOutput(iterateHopper);
                // System.out.println("inputblock material:" +
                // inputblock.getType().toString());
                // System.out.println("outputblock material:" +
                // outputblock.getType().toString());
                if (inputblock != null && inputblock.getType().equals(Material.ENDER_CHEST)) {

                    // is it inside a claim?
                    cachedClaim = Owner.gp.dataStore.getClaimAt(inputblock.getLocation(), true, cachedClaim);

                    if (cachedClaim != null) {

                        // grab this claim's data.
                        ClaimData cd = ClaimData.getClaimData(cachedClaim.getID());
                        if (!cd.getHopperPush()) continue;
                        // get the claim Owner...
                        String pOwner = cachedClaim.ownerName;
                        // continue if there is a white and the player
                        // is not allowed.
                        Player gotPlayer = Bukkit.getPlayer(pOwner);

                        if (gotPlayer != null || Owner.becPlugin != null) {
                            // if bec is available, grab that players
                            // BEC Chest, instead of the default.
                            if (Owner.becPlugin != null) {
                                String InventoryName = getInventoryName(pOwner, inputblock);
                                WorldGroup wg = Owner.becPlugin.getWorldGroupManager().getGroupByWorld(inputblock.getLocation().getWorld());
                                Owner.becPlugin.getChestCache().getInventory(InventoryName, wg, new Consumer<Inventory>() {
                                    @Override
                                    public void consume(Inventory i) {
                                        ih = i;
                                    }
                                });
                            } else {
                                ih = gotPlayer.getEnderChest();

                            }

                            if (ih != null) {
                                // get first item.
                                // System.out.println("EnderChest has "
                                // + HopperInventory.getSize() +
                                // " Items");
                                ItemStack firstItem = null;
                                if (ih.getSize() > 0) {
                                    for (ItemStack iterate : ih) {
                                        if (iterate != null) {
                                            firstItem = new ItemStack(iterate.clone());
                                            firstItem.setAmount(1);
                                            break;
                                        }
                                    }
                                    if (firstItem != null) {
                                        // System.out.println("Moving item:" + firstItem.getType().name());
                                        // System.out.println("moved item "
                                        // +
                                        // firstItem.getType().toString()
                                        // +
                                        // "from Enderchest to Hopper");
                                        // make sure there is room!
                                        HashMap<Integer, ItemStack> result = iterateHopper.getInventory().addItem(firstItem);
                                        // if we were able to add it,
                                        // remove it from the Ender
                                        // chest contents.
                                        if (result.isEmpty())
                                            ih.removeItem(firstItem);

                                    } // firstItem!=null
                                }
                            }
                        } // gotplayer!=null

                    } // cachedClaim!=null
                }
                if (outputblock != null && outputblock.getType().equals(Material.ENDER_CHEST)) {
                    // System.out.println("output Enderchest");
                    // is it inside a claim?
                    cachedClaim = Owner.gp.dataStore.getClaimAt(outputblock.getLocation(), true, cachedClaim);
                    if (cachedClaim != null) {
                        ClaimData cd = ClaimData.getClaimData(cachedClaim.getID());
                        // System.out.println( "Pull: " +
                        // cd.getHopperPull());
                        if (!cd.getHopperPull())
                            continue;

                        // get the claim owner...
                        String pOwner = cachedClaim.ownerName;

                        Player gotPlayer = Bukkit.getPlayer(pOwner);
                        if (gotPlayer != null || Owner.becPlugin != null) {

                            if (Owner.becPlugin != null) {
                                String inventoryName = getInventoryName(pOwner, outputblock);
                                WorldGroup wg = Owner.becPlugin.getWorldGroupManager().getGroupByWorld(inputblock.getLocation().getWorld());
                                Owner.becPlugin.getChestCache().getInventory(inventoryName, wg, new Consumer<Inventory>() {
                                    @Override
                                    public void consume(Inventory i) {
                                        ih = i;
                                    }
                                });
                            } else {
                                ih = gotPlayer.getEnderChest();

                            }

                            if (ih != null) {
                                ItemStack firstItem = null;
                                // we want to try to add an item from
                                // the hopper.
                                // System.out.println("Hopper has " +
                                // HopperInventory.getSize() +
                                // " Items");
                                if (HopperInventory.getSize() > 0) {
                                    for (ItemStack hopperitem : HopperInventory) {
                                        if (hopperitem != null) {
                                            firstItem = hopperitem.clone();
                                            firstItem.setAmount(1);
                                            break;
                                        }
                                    }
                                    if (firstItem != null) {
                                        // System.out.println("Moving item:"
                                        // +
                                        // firstItem.getType().name());

                                        HashMap<Integer, ItemStack> result = ih.addItem(firstItem);
                                        // if it went through, remove it
                                        // from the hopper.
                                        if (result.isEmpty()) {
                                            iterateHopper.getInventory().removeItem(firstItem);
                                        }
                                    }
                                }
                            }
                        }

                    }
                }
            } catch (Exception exx) {
                Owner.log.log(Level.SEVERE, "Error in thread:", exx);
            }
        }

    }


    public static Block getHopperInput(Hopper source) {
        try {
            Location sourcespot = source.getBlock().getLocation();
            Location newspot = new Location(sourcespot.getWorld(), sourcespot.getBlockX(), sourcespot.getBlockY() + 1, sourcespot.getBlockZ());
            return sourcespot.getWorld().getBlockAt(newspot);
        } catch (Exception exx) {
            return null;
        }

    }

    // returns the block this hopper is pointing at.
    public static Block getHopperOutput(Hopper source) {
        byte rawdata = source.getRawData();
        // 0 000 is straight down
        // 1 001 is SOUTH (Z+)
        // 2 010 is NORTH (Z-)
        // 4 100 is WEST (X-)
        // 5 101 is EAST (X+)
        int dir = (rawdata & 1) * 2 - 1; // dir is 1 for 1,3,5,7 or -1 for
                                         // 0,2,4,8
        return source.getBlock().getRelative((rawdata & 4) == 0 ? 0 : dir, rawdata == 0 ? -1 : 0, (rawdata & 4) == 0 ? dir : 0);
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
