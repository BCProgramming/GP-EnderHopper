package com.BASeCamp.GPEnderHoppers;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;

import nl.rutgerkok.betterenderchest.WorldGroup;
import nl.rutgerkok.betterenderchest.io.Consumer;

public class ConsumerWraper {
    //This class wraps the Consumer, avoiding any class issues when not using BEC
    boolean dir;
    Thread caller = Thread.currentThread();
    public ConsumerWraper(String invName, WorldGroup wg, final HopperHolder hold, boolean direction) {
        dir = direction;
        GPEnderHopper.becPlugin.getChestCache().getInventory(invName, wg, new Consumer<Inventory>() {
            @Override
            public void consume(final Inventory i) {
                if(i == null) return;
                if(caller.equals(Thread.currentThread())) {
                    hold.completeMove(i, dir);
                } else {
                    Bukkit.getScheduler().scheduleSyncDelayedTask(GPEnderHopper.self, new Runnable() {
                        public void run() {
                            hold.completeMove(i, dir);
                        }
                    });
                }
            }
        });
    }
}
