package com.example.doublejump;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
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
    private final Map<Player, Double> lastVelocityY = new HashMap<>();
    private final DoubleJumpPlugin plugin;

    public DoubleJumpListener(DoubleJumpPlugin plugin) {
        this.plugin = plugin;

        // Fallback task: keep allowFlight = true for eligible players every tick.
        // This handles edge cases where PlayerMoveEvent may not fire
        // (e.g. player frozen or stopped mid-air for any reason).
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
                    continue;
                }
                if (!player.isOnGround() && !player.isFlying()
                        && canDoubleJump.getOrDefault(player, false)) {
                    player.setAllowFlight(true);
                }
            }
        }, 1L, 1L);
    }

    private void performDoubleJump(Player player) {
        canDoubleJump.put(player, false);
        player.setAllowFlight(false);

        // Apply upward velocity matching vanilla jump height
        Vector velocity = player.getVelocity().clone();
        velocity.setY(0.42);
        player.setVelocity(velocity);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        player.setAllowFlight(true);
        canDoubleJump.put(player, true);
        lastVelocityY.put(player, 0.0);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        canDoubleJump.remove(player);
        lastVelocityY.remove(player);
    }

    @EventHandler(priority = EventPriority.HIGH)
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
            // Still allow flight so the next jump press can trigger again
            player.setAllowFlight(true);
            return;
        }

        // PRIMARY trigger: perform double jump immediately
        performDoubleJump(player);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        // Skip for creative/spectator mode players
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
            return;
        }

        // Landing on ground: reset double jump state
        if (player.isOnGround() && !player.isFlying()) {
            canDoubleJump.put(player, true);
            player.setAllowFlight(true);
            lastVelocityY.put(player, player.getVelocity().getY());
            return;
        }

        // Player is in the air
        double currentVelocityY = player.getVelocity().getY();
        double previousVelocityY = lastVelocityY.getOrDefault(player, currentVelocityY);
        lastVelocityY.put(player, currentVelocityY);

        // BACKUP trigger: detect jump intent through velocity change.
        // When the player presses jump while falling (and PlayerToggleFlightEvent
        // didn't fire because allowFlight was reset by the server), the client
        // still sends a jump packet, and the server may apply a velocity change.
        // We detect this by checking if the player's Y velocity went from
        // negative (falling) to positive (rising), which indicates a jump attempt.
        if (canDoubleJump.getOrDefault(player, false)
                && !player.isFlying()
                && previousVelocityY < -0.1
                && currentVelocityY >= 0.0) {
            performDoubleJump(player);
            return;
        }

        // Keep allowFlight = true while player is in the air and eligible.
        // This runs synchronously during movement packet processing, which is
        // more reliable than the scheduled task because it fires BEFORE the
        // server processes the flight ability packet from the client.
        if (!player.isFlying() && canDoubleJump.getOrDefault(player, false)) {
            player.setAllowFlight(true);
        }
    }
}