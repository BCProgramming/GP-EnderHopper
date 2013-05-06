package com.BASeCamp.GPEnderHoppers;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.LinkedList;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.events.ClaimModifiedEvent;

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
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.EnderChest;
import org.bukkit.util.Vector;


public class HopperHandler implements Listener,Runnable{
	private GPEnderHopper Owner = null;
	public GPEnderHopper getOwner(){return Owner;}
	public HopperHandler(GPEnderHopper pOwner){
		Owner=pOwner;
		//schedule a repeating task to complete, we will perform logic on all Ender chests.
		for(World w:Bukkit.getWorlds()){
			
			for(Chunk c:w.getLoadedChunks()){
				for(BlockState te:c.getTileEntities()){
					if(te instanceof Hopper){
						synchronized(iteratehoppers){
							iteratehoppers.add((Hopper) te);
						}
					}
				}
				
				
				
			}
			
		}
		Bukkit.getScheduler().scheduleSyncRepeatingTask(Owner, this, 20, Owner.cfg.EnderHopperDelayTicks);
	}
	public void run(){
		//System.out.println("HopperHandler...");
		//implementation of Runnable for RepeatingSync Task.
		//Iterate through all Hoppers...
		Claim cachedClaim=null;
		synchronized (this){
		for(Hopper iterateHopper:iteratehoppers){
			try {
			if(iterateHopper==null) continue;
			if(iterateHopper.getInventory()==null) continue;
			Inventory HopperInventory = iterateHopper.getInventory();
			
			if(iterateHopper.getBlock().isBlockPowered()) continue; //powered hoppers do not accept or send items.
			//if the input is a Ender Chest
			//if the hopper is in a claim
			//take an item from the Ender inventory of the claim owner and put it in the hopper if there is room.
			//if The hopper is pointing at a Ender chest
			//and if the hopper is in a claim
			//take an item from the Hopper and put it into the Ender inventory of the claim owner
			//if there is room.
			Block inputblock = getHopperInput(iterateHopper);
			Block outputblock = getHopperOutput(iterateHopper);
			//System.out.println("inputblock material:" + inputblock.getType().toString());
			//System.out.println("outputblock material:" + outputblock.getType().toString());
			if(inputblock!=null && inputblock.getType().equals(Material.ENDER_CHEST)){
				
				//is it inside a claim?
				cachedClaim = Owner.gp.dataStore.getClaimAt(inputblock.getLocation(), true, cachedClaim);
				if(cachedClaim!=null){
					//get the claim Owner...
					String pOwner = cachedClaim.ownerName;
					//continue if there is a white and the player is not allowed. 
					if(Owner.cfg.WhiteList.size()==0 || Owner.cfg.WhiteList.contains(pOwner)){
					Player gotPlayer = Bukkit.getPlayer(pOwner);
				    if(gotPlayer!=null){
				    	Inventory ih =  gotPlayer.getEnderChest();
				    	if(ih!=null){
					    	//get first item.
					    	ItemStack firstItem=null;
					    	if(ih.getSize()>0){
					    	for(ItemStack iterate:ih){
					    		if(iterate!=null){
					    			firstItem=new ItemStack(iterate.clone());
					    			firstItem.setAmount(1);
					    			break;
					    		}
					    	}
					    	if(firstItem!=null){
					    		//make sure there is room!
					    		HashMap<Integer, ItemStack> result = 
					    				iterateHopper.getInventory().addItem(firstItem);
					    		//if we were able to add it, remove it from the Ender chest contents.
					    		if(result.isEmpty())
					    			ih.removeItem(firstItem);
					    		
					    		
					    		
					    	} //firstItem!=null
					    	}
				    	}
				    } //gotplayer!=null
				} //whitelist check
				} //cachedClaim!=null
			}
    		if(outputblock!=null && outputblock.getType().equals(Material.ENDER_CHEST)){
				
				//is it inside a claim?
				cachedClaim = Owner.gp.dataStore.getClaimAt(outputblock.getLocation(),true,cachedClaim);
				if(cachedClaim!=null){
					//get the claim owner...
					String pOwner = cachedClaim.ownerName;
					if(Owner.cfg.WhiteList.size()==0 || Owner.cfg.WhiteList.contains(pOwner)){
						
					Player gotPlayer = Bukkit.getPlayer(pOwner);
					if(gotPlayer!=null){
						Inventory ih = gotPlayer.getEnderChest();
						if(ih!=null){
							ItemStack firstItem=null;
							//we want to try to add an item from the hopper.
							if(HopperInventory.getSize()>0){
								for(ItemStack hopperitem:HopperInventory){
									if(hopperitem!=null)
									{
										firstItem=hopperitem.clone();
										firstItem.setAmount(1);
										break;
									}
								}
								HashMap<Integer,ItemStack> result = 
										ih.addItem(firstItem);
								//if it went through, remove it from the hopper.
								if(result.isEmpty()){
									iterateHopper.getInventory().removeItem(firstItem);
								}
							}
						}
					}
				}
				}
			}
		}
		catch(ConcurrentModificationException cme){
				
		}
		catch(NullPointerException npe){
			
		}
		catch(Exception exx){
			exx.printStackTrace();
		}
		}
		} //synchronized
		
		
		
	}
	
	private LinkedList<Hopper> iteratehoppers = new LinkedList<Hopper>();
public static Block getHopperInput(Hopper source){
		try {
		Location sourcespot = source.getBlock().getLocation();
		Location newspot = new Location(sourcespot.getWorld(),sourcespot.getBlockX(),sourcespot.getBlockY()+1,sourcespot.getBlockZ());
		return sourcespot.getWorld().getBlockAt(newspot);
		}
		catch(Exception exx){ return null;}
		
	}
        //0 is straight down
		//1 is SOUTH (Z+)
		//2 is NORTH (Z-)
		//4 is WEST (X-)
		//5 is EAST (X+)
private static Vector[] offsets = new Vector[] {
	
	new Vector(0,-1,0),
	new Vector(0,0,1),
	new Vector(0,0,-1),
	new Vector(-1,0,0),
	new Vector(1,0,0)
};
	//returns the block this hopper is pointing at.
	public static Block getHopperOutput(Hopper source){
		Location sourcespot = source.getBlock().getLocation();
		byte rawdata = source.getRawData();
		//0 is straight down
		//1 is SOUTH (Z+)
		//2 is NORTH (Z-)
		//4 is WEST (X-)
		//5 is EAST (X+)
		
		Vector useoffset = offsets[rawdata];
		
		Location outputblock = new Location(sourcespot.getWorld(),
				sourcespot.getBlockX()+useoffset.getBlockX(),
				sourcespot.getBlockY()+useoffset.getBlockY(),
				sourcespot.getBlockZ()+useoffset.getBlockZ());
		
		return sourcespot.getWorld().getBlockAt(outputblock);
	}
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent ev){
		if(ev.getBlockPlaced().getType()==Material.HOPPER){
			synchronized (this){
			    iteratehoppers.add((Hopper)ev.getBlock().getState());
			}
		}
	}
	public void onBlockBreak(BlockBreakEvent ev){
		if(ev.getBlock().getType().equals(Material.HOPPER)){
			synchronized (this){
				iteratehoppers.remove(ev.getBlock().getState());
			}
		}
	}
	@EventHandler
	public void OnChunkUnload(ChunkUnloadEvent ev){
		for(BlockState te:ev.getChunk().getTileEntities()){
			if(te instanceof Hopper)
				synchronized(this){
					iteratehoppers.add((Hopper)te);
				}
		}
	}
	@EventHandler
	public void OnChunkLoad(ChunkLoadEvent ev){
	for(BlockState te :ev.getChunk().getTileEntities()){
		//we cannot find ender chests, unfortunately, but we can find hoppers.
		//look for hoppers.
		if(te instanceof Hopper){
			synchronized(this){
			    iteratehoppers.add((Hopper)te);
			}
		}
		
		
		
	}
	
	}
	

}

