package com.example.doublejump;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.util.Vector;

public class DoubleJumpListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        // Allow flight by default for double jump detection
        player.setAllowFlight(true);
    }

    @EventHandler
    public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();

        // Skip for creative/spectator mode players
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
            return;
        }

        // Cannot double jump while sneaking
        if (player.isSneaking()) {
            event.setCancelled(true);
            player.setFlying(false);
            player.setAllowFlight(true);
            return;
        }

        // Perform double jump
        event.setCancelled(true);
        player.setFlying(false);
        player.setAllowFlight(false);

        // Apply upward velocity for the double jump
        Vector velocity = player.getVelocity();
        velocity.setY(0.42);
        // Preserve horizontal momentum
        player.setVelocity(velocity);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        // Reset double jump when player lands on ground
        if (player.isOnGround() && !player.isFlying()) {
            player.setAllowFlight(true);
        }
    }
}