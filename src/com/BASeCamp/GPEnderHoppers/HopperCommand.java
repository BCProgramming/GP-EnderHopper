package com.BASeCamp.GPEnderHoppers;

import java.util.HashSet;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.Messages;
import me.ryanhamshire.GriefPrevention.PlayerData;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Hopper;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HopperCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String name, String[] args) {
		Boolean type = null;
		if (command.getName().equalsIgnoreCase("claimecpull"))
			type = false;
		if (command.getName().equalsIgnoreCase("claimecpush"))
			type = true;
		if (type == null)
			return false; // This is not the command you're looking for.
		if (sender instanceof Player && args.length == 1
				&& args[0].equalsIgnoreCase("debug")) {

			Block b = ((Player) sender).getTargetBlock(new HashSet<Material>(),
					10);
			if (b.getType() != Material.HOPPER) {
				sender.sendMessage("That's not a hopper");
				return true;
			}
			if (!GPEnderHopper.hh.hasHopper((Hopper) b.getState())) {
				sender.sendMessage("That's not being tracked!");
				return true;
			}
			HopperHolder hold = HopperHolder.getHolder((Hopper) b.getState());
			hold.debug = sender.getName();
			return true;
		}
		long claimID = -1;
		if (!(sender instanceof Player) && args.length == 0) {// Console most
																// likely
			if (sender.hasPermission("EnderHoppers.Manipulate.byId")) {
				sender.sendMessage("You must select a claim by ID number");
			} else
				sender.sendMessage("You cannot use this command.");
			return true;
		}
		String change = null;
		// if it is a claim ID, use that...
		boolean isClaim = false;
		if (args.length > 0) {
			try {

				claimID = Long.parseLong(args[0]);
				if (null != GPEnderHopper.gp.dataStore.getClaim(claimID)) {
					// valid claim.
					isClaim = true;
				}
			} catch (Exception e) {
				isClaim = false;
			}
		}
		if (isClaim && sender.hasPermission("EnderHoppers.Manipulate.byId")) {
			try {
				claimID = Long.parseLong(args[0]);
			} catch (Exception e) {
				sender.sendMessage("Could not parse '" + args[0]
						+ "' as either an ID or as 'here'");
				return true;
			}
			if (args.length >= 2)
				change = args[1];
		} else if (sender instanceof Player) {
			Player p = (Player) sender;
			Claim inclaim = GPEnderHopper.gp.dataStore.getClaimAt(
					p.getLocation(), true, null);
			if (!inclaim.getOwnerName().equalsIgnoreCase(p.getName())) {
				PlayerData pd = GPEnderHopper.gp.dataStore.getPlayerData(p
						.getUniqueId());
				if (!pd.ignoreClaims) {
					p.sendMessage(ChatColor.RED + " You do not own this claim!");
					// if they have ignoreclaims perm from GP, show that...
					if (p.hasPermission("GriefPrevention.IgnoreClaims")) {
						p.sendMessage(ChatColor.YELLOW.toString()
								+ Messages.IgnoreClaimsAdvertisement);
					}
					return true;
				}
			}
			if (inclaim != null)
				claimID = inclaim.getID();
			if (args.length >= 1) {
				if (args[0].equalsIgnoreCase("here")) {
					if (args.length >= 2)
						change = args[1];
				} else
					change = args[0];
			}
		}
		if (claimID == -1) {
			sender.sendMessage("There is no claim here.");
			return true;
		}
		ClaimData data = ClaimData.getClaimData(claimID);
		if (change != null) {
			if (change.equalsIgnoreCase("on")
					|| change.equalsIgnoreCase("enable")
					| change.equalsIgnoreCase("true")) {
				if (type) {
					data.setHopperPush(true);
				} else
					data.setHopperPull(true);
			} else if (change.equalsIgnoreCase("off")
					|| change.equalsIgnoreCase("disable")
					| change.equalsIgnoreCase("false")) {
				if (type) {
					data.setHopperPush(false);
				} else
					data.setHopperPull(false);
			} else {
				sender.sendMessage("Could not understand " + change);
				return true;
			}
		}
		if (type) {
			sender.sendMessage("Hopper Enderchest Push: "
					+ (data.getHopperPush() ? ChatColor.GREEN + "ON"
							: ChatColor.RED + "OFF"));
		} else {
			sender.sendMessage("Hopper Enderchest Pull: "
					+ (data.getHopperPull() ? ChatColor.GREEN + "ON"
							: ChatColor.RED + "OFF"));
		}
		return true;
	}

}
