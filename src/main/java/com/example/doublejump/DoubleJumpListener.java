package com.example.doublejump;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;

public class DoubleJumpListener implements Listener {

    private final Map<Player, Boolean> canDoubleJump = new HashMap<>();

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        player.setAllowFlight(true);
        canDoubleJump.put(player, true);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        canDoubleJump.remove(event.getPlayer());
    }

    @EventHandler
    public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();

        // Skip for creative/spectator mode players
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
            return;
        }

        // Cancel the flight toggle
        event.setCancelled(true);
        player.setFlying(false);

        // Cannot double jump while sneaking
        if (player.isSneaking()) {
            player.setAllowFlight(true);
            return;
        }

        // Cannot double jump if already used
        if (!canDoubleJump.getOrDefault(player, false)) {
            player.setAllowFlight(true);
            return;
        }

        // Perform double jump
        canDoubleJump.put(player, false);
        player.setAllowFlight(false);

        // Apply upward velocity matching vanilla jump height
        Vector velocity = player.getVelocity().clone();
        velocity.setY(0.42);
        player.setVelocity(velocity);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        // Skip for creative/spectator mode players
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
            return;
        }

        // Reset double jump when player lands on ground
        if (player.isOnGround() && !player.isFlying()) {
            canDoubleJump.put(player, true);
            player.setAllowFlight(true);
            return;
        }

        // Keep allowFlight=true while in the air and can still double jump,
        // to ensure PlayerToggleFlightEvent fires reliably even when sprinting
        if (!player.isOnGround() && canDoubleJump.getOrDefault(player, false)) {
            player.setAllowFlight(true);
        }
    }
}