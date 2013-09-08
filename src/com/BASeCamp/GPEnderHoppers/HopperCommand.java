package com.BASeCamp.GPEnderHoppers;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.Messages;
import me.ryanhamshire.GriefPrevention.PlayerData;
import me.ryanhamshire.GriefPrevention.TextMode;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HopperCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String name, String[] args) {
        // TODO Auto-generated method stub
        if (!(sender instanceof Player))
            return false;
        Player p = (Player) sender;
        Claim inclaim = GPEnderHopper.gp.dataStore.getClaimAt(p.getLocation(), true, null);

        if (name.equalsIgnoreCase("claimecpull") || (name.equalsIgnoreCase("claimecpush"))) {
            if (inclaim == null) {
                p.sendMessage(ChatColor.RED + " You must be inside a claim to change it!");
                return false;
            }
            boolean ignoreclaimperm = p.hasPermission("GriefPrevention.IgnoreClaims");
            PlayerData pd = GPEnderHopper.gp.dataStore.getPlayerData(p.getName());

            // make sure the claim is owned by the player, or they are set to
            // ignore claims.
            if (!(pd.ignoreClaims || inclaim.ownerName.equalsIgnoreCase(p.getName()))) {
                // nope!
                p.sendMessage(ChatColor.RED + " You do not own this claim!");
                // if they have ignoreclaims perm from GP, show that...
                if (ignoreclaimperm) {
                    GriefPrevention.sendMessage(p, TextMode.Info, Messages.IgnoreClaimsAdvertisement);

                }
                return false;

            }

            // checks out, toggle appropriate perm.
            // retrieve the claim Data.
            ClaimData cd = ClaimData.getClaimData(inclaim.getID());
            if (name.equalsIgnoreCase("claimecpull")) {
                cd.setHopperPull(!cd.getHopperPull());
                p.sendMessage("Hopper Enderchest Pull:" + (cd.getHopperPull() ? "On" : "Off"));
            } else if (name.equalsIgnoreCase("claimecpush")) {
                cd.setHopperPush(!cd.getHopperPush());
                p.sendMessage("Hopper Enderchest Push:" + (cd.getHopperPush() ? "On" : "Off"));
            }
            // save it.

            return true;

        }

        return false;
    }

}
