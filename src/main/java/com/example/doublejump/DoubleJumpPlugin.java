package com.example.doublejump;

import org.bukkit.plugin.java.JavaPlugin;

public class DoubleJumpPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new DoubleJumpListener(this), this);
        getLogger().info("DoubleJump plugin has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("DoubleJump plugin has been disabled!");
    }
}