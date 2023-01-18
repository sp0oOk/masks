package com.github.spook.masks.objects;

import com.github.spook.masks.Masks;
import com.github.spook.masks.managers.MaskManager;
import lombok.Builder;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.function.BiConsumer;

/**
 * Abstract class for masks
 *
 * <p>NOTE: was going to do consumers for events, but stupid idea... see {@link
 * MaskManager#hasMaskEquipped(Player, String)} to see how to check if a player has a mask equipped
 * use in events like {@link org.bukkit.event.player.PlayerInteractEvent}
 */
@Builder(builderMethodName = "create")
@Getter
public class AbstractMask {

  private final String
      internalName; // The internal name of the mask. (used to identify the mask apart from the
  // display name)
  private final String displayName; // The display name of the mask.
  private final String textureURL; // The texture URL of the mask.
  private final List<String> lore; // The lore of the mask.
  private BiConsumer<MaskManager, Player> onEquip; // The on-equip event of the mask.
  private BiConsumer<MaskManager, Player> onUnequip; // The on-unequip event of the mask.

  /** Called to register the mask. */
  public void register() {
    Masks.getInstance().getMaskManager().addMask(this);
  }
}
