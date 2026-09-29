package com.fast.hitfix;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class HitFixListener implements Listener {
    
    private final FastHitFix plugin;
    private final HitFixManager hitFixManager;
    
    public HitFixListener(FastHitFix plugin, HitFixManager hitFixManager) {
        this.plugin = plugin;
        this.hitFixManager = hitFixManager;
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        if (!(event.getEntity() instanceof Player)) return;
        
        Player attacker = (Player) event.getDamager();
        Player victim = (Player) event.getEntity();
        
        if (attacker.equals(victim)) return;
        
        // Hit already processed by packet listener
        // Just verify damage
    }
    
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        hitFixManager.clearPlayer(event.getPlayer());
    }
}
