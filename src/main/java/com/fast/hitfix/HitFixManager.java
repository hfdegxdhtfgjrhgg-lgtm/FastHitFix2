package com.fast.hitfix;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class HitFixManager {
    
    private final FastHitFix plugin;
    private final ProtocolManager protocolManager;
    
    private final Map<UUID, Long> lastHitTime = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> hitCounter = new ConcurrentHashMap<>();
    
    private final List<PacketAdapter> packetAdapters = new ArrayList<>();
    
    private boolean fixHitRegistration;
    private boolean preventGhostHits;
    private boolean fixKnockback;
    private boolean fixReach;
    
    private double maxReach;
    private int minHitDelay;
    private long resetTime;
    
    public HitFixManager(FastHitFix plugin, ProtocolManager protocolManager) {
        this.plugin = plugin;
        this.protocolManager = protocolManager;
        reload();
    }
    
    public void reload() {
        fixHitRegistration = plugin.getConfig().getBoolean("hitfix.fix-hit-registration", true);
        preventGhostHits = plugin.getConfig().getBoolean("hitfix.prevent-ghost-hits", true);
        fixKnockback = plugin.getConfig().getBoolean("hitfix.fix-knockback", true);
        fixReach = plugin.getConfig().getBoolean("hitfix.fix-reach", true);
        
        maxReach = plugin.getConfig().getDouble("combat.max-reach", 4.0);
        minHitDelay = plugin.getConfig().getInt("combat.min-hit-delay", 100);
        resetTime = plugin.getConfig().getLong("combat.reset-time", 500);
    }
    
    public void registerPacketListeners() {
        if (fixHitRegistration) {
            registerHitListener();
        }
        
        if (preventGhostHits) {
            registerGhostHitListener();
        }
    }
    
    private void registerHitListener() {
        PacketAdapter adapter = new PacketAdapter(plugin, 
                PacketType.Play.Client.USE_ENTITY) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                if (event.isCancelled()) return;
                
                Player player = event.getPlayer();
                if (player == null) return;
                if (player.hasPermission("fasthitfix.bypass")) return;
                
                handleHit(player);
            }
        };
        
        protocolManager.addPacketListener(adapter);
        packetAdapters.add(adapter);
    }
    
    private void registerGhostHitListener() {
        PacketAdapter adapter = new PacketAdapter(plugin,
                PacketType.Play.Server.ENTITY_VELOCITY) {
            @Override
            public void onPacketSending(PacketEvent event) {
                // Knockback fix
            }
        };
        
        protocolManager.addPacketListener(adapter);
        packetAdapters.add(adapter);
    }
    
    public void unregisterPacketListeners() {
        for (PacketAdapter adapter : packetAdapters) {
            protocolManager.removePacketListener(adapter);
        }
        packetAdapters.clear();
    }
    
    private void handleHit(Player player) {
        long now = System.currentTimeMillis();
        long lastHit = lastHitTime.getOrDefault(player.getUniqueId(), 0L);
        
        long delay = now - lastHit;
        
        if (delay < minHitDelay && lastHit > 0) {
            plugin.getLogger().warning(player.getName() + " hit too fast: " + delay + "ms");
        }
        
        lastHitTime.put(player.getUniqueId(), now);
        
        int counter = hitCounter.getOrDefault(player.getUniqueId(), 0) + 1;
        hitCounter.put(player.getUniqueId(), counter);
        
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            int current = hitCounter.getOrDefault(player.getUniqueId(), 0);
            if (current > 0) {
                hitCounter.put(player.getUniqueId(), current - 1);
            }
        }, resetTime / 50L);
    }
    
    public void clearPlayer(Player player) {
        lastHitTime.remove(player.getUniqueId());
        hitCounter.remove(player.getUniqueId());
    }
    
    public int getHitCount(Player player) {
        return hitCounter.getOrDefault(player.getUniqueId(), 0);
    }
}
