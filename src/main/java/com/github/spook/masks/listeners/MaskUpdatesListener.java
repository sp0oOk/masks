package com.github.spook.masks.listeners;

import com.github.spook.masks.Masks;
import com.github.spook.masks.objects.AbstractMask;
import com.github.spook.masks.objects.Util;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;

import java.util.List;
import java.util.stream.Collectors;

public class MaskUpdatesListener implements Listener {

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onChange(PlayerGameModeChangeEvent event) {
    if (event.getNewGameMode() == GameMode.CREATIVE) {
      Bukkit.getScheduler()
          .runTaskLater(
              Masks.getInstance(),
              () -> event.getPlayer().updateInventory(),
              1L); // Update inventory
    }
  }

  @EventHandler
  public void onQuit(PlayerQuitEvent event) {
    final List<AbstractMask> masks =
        Masks.getInstance().getMaskManager().getEquippedMasks(event.getPlayer()).stream()
            .filter(m -> m.getOnUnequip() != null)
            .collect(Collectors.toList()); // Get all masks with onUnequip consumer and collect them

    if (!masks.isEmpty()) { // If there actually is any masks with onUnequip consumer
      masks.forEach(
          mask ->
              mask.getOnUnequip()
                  .accept(
                      Masks.getInstance().getMaskManager(),
                      event.getPlayer())); // Run onUnequip consumer
    }
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onJoin(PlayerJoinEvent event) {
    Masks.getInstance()
        .getMaskManager()
        .updateMasks(event.getPlayer()); // Update masks when player joins
  }

  @EventHandler
  public void onEquip(PlayerInteractEvent event) {
    if (event.hasItem() && event.getItem() != null && Util.isArmorPiece(event.getItem())) {
      Masks.getInstance()
          .getMaskManager()
          .scheduleMaskUpdate(
              event.getPlayer()); // Update masks when player interacts with armor piece
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onWorldChange(PlayerChangedWorldEvent event) {
    Masks.getInstance()
        .getMaskManager()
        .scheduleMaskUpdate(event.getPlayer()); // Update masks when player changes world
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onHit(EntityDamageEvent event) {
    if (event.getEntity() instanceof Player) {
      Masks.getInstance()
          .getMaskManager()
          .scheduleMaskUpdate((Player) event.getEntity()); // Update masks when player gets hit
    }
  }

  @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
  public void onClick(InventoryClickEvent event) {
    if ((event.isShiftClick()
            || event.getSlotType() == InventoryType.SlotType.ARMOR
            || event.getClick().isKeyboardClick())
        && event.getInventory().getType()
            == InventoryType
                .CRAFTING) { // Apparently this is the inventory type when changing armor? was
                             // InventoryType.PLAYER in 1.7
      Masks.getInstance()
          .getMaskManager()
          .scheduleMaskUpdate(
              (Player) event.getWhoClicked()); // Update masks when player clicks in inventory
    }
  }
}
