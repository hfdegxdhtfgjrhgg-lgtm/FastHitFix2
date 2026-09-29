package com.fast.hitfix;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public class FastHitFix extends JavaPlugin {
    
    private HitFixManager hitFixManager;
    private ProtocolManager protocolManager;
    
    @Override
    public void onEnable() {
        saveDefaultConfig();
        
        if (getServer().getPluginManager().getPlugin("ProtocolLib") == null) {
            getLogger().severe("ProtocolLib not found! Disabling plugin...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        protocolManager = ProtocolLibrary.getProtocolManager();
        
        hitFixManager = new HitFixManager(this, protocolManager);
        
        getServer().getPluginManager().registerEvents(
            new HitFixListener(this, hitFixManager), this);
        
        hitFixManager.registerPacketListeners();
        
        getCommand("hitfix").setExecutor((sender, cmd, label, args) -> {
            sender.sendMessage(color("&6&m═══════════════════════════"));
            sender.sendMessage(color("&6FastHitFix &7v1.0.0"));
            sender.sendMessage(color("&7Fix Ghost Hits"));
            sender.sendMessage(color("&7Author: &fFast"));
            sender.sendMessage(color("&7Max Reach: &f" + getConfig().getDouble("combat.max-reach")));
            sender.sendMessage(color("&7Min Delay: &f" + getConfig().getInt("combat.min-hit-delay") + "ms"));
            sender.sendMessage(color("&6&m═══════════════════════════"));
            return true;
        });
        
        getCommand("hitfixreload").setExecutor((sender, cmd, label, args) -> {
            reloadConfig();
            hitFixManager.reload();
            sender.sendMessage(color(getConfig().getString("messages.prefix") + 
                getConfig().getString("messages.reloaded")));
            return true;
        });
        
        getLogger().info("FastHitFix enabled!");
    }
    
    @Override
    public void onDisable() {
        if (hitFixManager != null) {
            hitFixManager.unregisterPacketListeners();
        }
        getLogger().info("FastHitFix disabled");
    }
    
    public HitFixManager getHitFixManager() {
        return hitFixManager;
    }
    
    private String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
