package com.github.spook.masks.managers;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.github.spook.masks.Masks;
import com.github.spook.masks.enums.EnchantName;
import com.github.spook.masks.enums.ManagerKillResult;
import com.github.spook.masks.enums.RomanNumeral;
import com.github.spook.masks.objects.*;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import de.tr7zw.nbtapi.NBTItem;
import net.minecraft.server.v1_8_R3.Container;
import net.minecraft.server.v1_8_R3.EntityHuman;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

import static com.comphenix.protocol.PacketType.Play.Server.*;

public class MaskManager extends Manager {

  private JavaPlugin plugin;
  private List<AbstractMask> masks;
  private HashBiMap<UUID, List<String>> playerMasks; // Cannot remember why I used a HashBiMap here

  private final ImmutableList<String> LORE_ADDON =
      ImmutableList.of(
          "",
          "§7§oAttach this mask to any helmet",
          "§7§oto give it a visual override!",
          "",
          "§7To attach, place this mask on a helmet.",
          "§7To un-attach, right-click helmet while attached.");

  private final String DEFAULT_TEXTURE_URL =
      "eyJ0aW1lc3RhbXAiOjE1NjIwODg5MjU2NDUsInByb2ZpbGVJZCI6IjdjODk1YWQwMTFkMDQzNTA5YWU1ZjJiYjFjZjZjOGVhIiwicHJvZmlsZU5hbWUiOiJCYXNpY0JBRSIsInRleHR1cmVzIjp7IlNLSU4iOnsidXJsIjoiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS83M2UxYWQ0ZmY0ZDhlMjM5YmJlYTUyNGIyNGM0ZDRkNWQ4NjIzZTUyZDdkOGVlNzllNmE1ZjhjN2FkZWRiOWEzIn19fQ==";

  /** Override onEnable method. */
  @Override
  public void onEnable(JavaPlugin plugin) {
    this.masks = Lists.newArrayList(); // Initialize masks list
    this.playerMasks = HashBiMap.create(); // Initialize player masks map
    registerDefaultMasks(); // Register default masks
    PacketAdapter adapter =
        new PacketAdapter(
            plugin,
            ListenerPriority.LOWEST,
            ENTITY_EQUIPMENT,
            WINDOW_ITEMS,
            SET_SLOT) { // Create packet adapter

          @Override
          public void onPacketSending(PacketEvent event) {
            final Player player = event.getPlayer();
            final ItemStack itemMask;

            if (player == null || !player.isOnline() || player.isDead()) { // If player is null or offline or dead
              return;
            }

            if (event.getPacketType() == ENTITY_EQUIPMENT) { // If packet is entity equipment

              final int entityId = event.getPacket().getIntegers().read(0);
              final ItemStack item;
              final Entity entity;

              if (player.getGameMode() == GameMode.CREATIVE && player.getEntityId() == entityId) {
                return;
              }

              if ((item = event.getPacket().getItemModifier().read(0)) != null
                  && ((itemMask = getMaskFromItem(item)) != null)
                  && (entity =
                          ProtocolLibrary.getProtocolManager()
                              .getEntityFromID(player.getWorld(), entityId))
                      != null
                  && entity instanceof EntityHuman) {
                event.getPacket().getItemModifier().write(0, itemMask);
              }

            } else if (event.getPacketType() == WINDOW_ITEMS) { // If packet is window items
              final int slot = event.getPacket().getIntegers().read(0);
              final ItemStack[] items = event.getPacket().getItemArrayModifier().read(0);

              if (slot != ((CraftPlayer) player).getHandle().activeContainer.windowId) {
                return;
              }

              if (items != null
                  && items.length >= 4
                  && !(player.getGameMode() == GameMode.CREATIVE)
                  && (itemMask = getMaskFromItem(items[5])) != null) {
                items[5] = itemMask;
                event.getPacket().getItemArrayModifier().write(0, items);
              }

            } else if (event.getPacketType() == SET_SLOT) { // If packet is set slot
              final Container container = ((CraftPlayer) player).getHandle().defaultContainer;

              if (container == null
                  || container.windowId != event.getPacket().getIntegers().read(0)
                  || player.getGameMode() == GameMode.CREATIVE) {
                return;
              }

              if (event.getPacket().getIntegers().read(1) == 5
                  && (itemMask = getMaskFromItem(event.getPacket().getItemModifier().read(0)))
                      != null) {
                event.getPacket().getItemModifier().write(0, itemMask);
              }
            }
          }
        };
    ProtocolLibrary.getProtocolManager().addPacketListener(adapter); // Register packet adapter
  }

  /** Override onDisable method */
  @Override
  public void onDisable() {
    this.masks.clear(); // Clear masks list
    this.playerMasks.clear(); // Clear player masks map
    ProtocolLibrary.getProtocolManager()
        .removePacketListeners(this.plugin); // Remove packet listeners
    this.plugin = null; // Set plugin to null
  }

  /**
   * Kill manager for some reason
   *
   * @return Result of kill method
   */
  @Override
  public Result<ManagerKillResult, Exception> kill() {
    try {
      ProtocolLibrary.getProtocolManager()
          .removePacketListeners(this.plugin); // Remove packet listeners
      return Result.success(ManagerKillResult.SUCCESS); // Return success
    } catch (Exception e) {
      return Result.error(ManagerKillResult.FAILED, e); // Return error
    }
  }

  /**
   * Get masked item-stack (one that will be overridden in the packet listener) this is the
   * item-stack that will be sent to the player
   *
   * @param item item-stack to get masked item-stack from
   * @return masked item-stack
   */
  private ItemStack getMaskFromItem(ItemStack item) {
    if (item == null
        || !item.getType().name().endsWith("_HELMET")) { // If item is null or not a helmet
      return null;
    }

    final NBTItem nbtItem = new NBTItem(item); // Create NBT item
    final List<String> maskIds = getMaskListFromItem(nbtItem); // Get mask ids from item

    if (maskIds.isEmpty()) { // If mask ids is empty
      return null; // Return null
    }

    return createTextureMask(
        maskIds.size() > 1 ? null : getMaskByName(maskIds.get(0)), item); // Return texture mask
  }

  /** Register all default masks. */
  private void registerDefaultMasks() {

    AbstractMask.create()
        .internalName("testMask")
        .displayName("&3&lTest Mask")
        .textureURL(
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmVlNTU0YWJlMTRmZGE5MmVmNWVjOTIxMjIyZmU2MGMyNjhhOGFiZGY0MTIwZDRmMjgzZTgwM2RlOGQzZmUwYiJ9fX0=")
        .lore(ImmutableList.of("&7This is a test mask", "&7It does nothing"))
        .build()
        .register();
  }

  /**
   * Get a {@link AbstractMask} by its internal name.
   *
   * @param name The internal name of the mask.
   * @return The mask, or null if not found.
   */
  private AbstractMask getMaskByName(String name) {
    return this.masks.stream()
        .filter(m -> m.getInternalName().equalsIgnoreCase(name))
        .findFirst()
        .orElse(null); // Return mask
  }

  /**
   * Get a list of {@link AbstractMask} from a list of mask internal names
   *
   * @param list List of mask internal names
   * @return List of {@link AbstractMask}
   */
  private List<AbstractMask> getMaskList(List<String> list) {
    List<AbstractMask> masks = Lists.newArrayList(); // Create masks list
    for (String s : list) {
      masks.add(getMaskByName(s)); // Add mask to list
    }
    return masks; // Return masks list
  }

  /**
   * Registers a mask to the manager and adds it to the masks list.
   *
   * @param mask The mask to register.
   */
  public void addMask(AbstractMask mask) {
    this.masks.add(mask); // Add mask to list
  }

  /**
   * Add or append a mask to an item
   *
   * @param helmet The helmet to add the mask to
   * @param maskIds The mask ids to add
   * @return The new helmet
   */
  public ItemStack addOrAppendMask(ItemStack helmet, String... maskIds) {
    final List<AbstractMask> masks = getMaskList(Arrays.asList(maskIds));

    if (masks.isEmpty()
        || masks.stream().anyMatch(Objects::isNull)
        || helmet == null
        || !helmet
            .getType()
            .name()
            .endsWith("_HELMET")) { // If masks is empty or helmet is null or not a helmet
      return helmet;
    }

    final NBTItem nbtHelmet = new NBTItem(helmet); // Create NBT item
    final List<String> alreadyAttachedMasks =
        getMaskListFromItem(nbtHelmet); // Get mask ids from item

    Arrays.asList(maskIds)
        .forEach(
            maskId -> {
              if (!alreadyAttachedMasks.contains(
                  maskId)) { // If mask id is not in already attached masks
                alreadyAttachedMasks.add(maskId); // Add mask id to already attached masks
              }
            });

    nbtHelmet.setString("mask", String.join(";", alreadyAttachedMasks)); // Set mask string

    return nbtHelmet.getItem(); // Return item
  }

  /**
   * Create a texture mask from a mask and a helmet
   *
   * @param mask The mask to use
   * @param base The helmet to use
   * @return The new helmet
   */
  public ItemStack createTextureMask(AbstractMask mask, ItemStack base) {
    final ItemStack returnType =
        getItem(mask == null ? DEFAULT_TEXTURE_URL : mask.getTextureURL()); // Get custom skull

    final ItemMeta meta = returnType.getItemMeta();

    meta.setDisplayName(Util.getDisplayName(base));

    final List<String> lore = Lists.newArrayList();

    if (base.hasItemMeta()) {
      if (base.getItemMeta().hasEnchants()) {
        base.getItemMeta()
            .getEnchants()
            .forEach(
                (enchant, level) ->
                    lore.add(
                        Util.color(
                            "&7"
                                + EnchantName.valueOf(enchant.getName()).getType()
                                + " "
                                + RomanNumeral.toRoman(level))));
      }

      if (base.getItemMeta().hasLore()) {
        base.getItemMeta().getLore().forEach(l -> meta.getLore().add(Util.color(l)));
      }
    }

    meta.setLore(lore);

    returnType.setItemMeta(meta);

    return returnType; // Return item
  }

  /**
   * Get a minecraft textured item specifically, this will return a custom skull with the texture of
   * the given url
   *
   * @param texture The texture (base64)
   * @return The item
   */
  private ItemStack getItem(String texture) {
    GameProfile profile = new GameProfile(UUID.randomUUID(), null);
    PropertyMap propertyMap = profile.getProperties();
    if (propertyMap == null) {
      throw new IllegalStateException("Profile doesn't contain a property map");
    }
    propertyMap.put("textures", new Property("textures", texture));
    ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
    ItemMeta headMeta = head.getItemMeta();
    Class<?> headMetaClass = headMeta.getClass();
    try {
      getField(headMetaClass, "profile", GameProfile.class, 0).set(headMeta, profile);
    } catch (IllegalArgumentException | IllegalAccessException e) {
      e.printStackTrace();
    }
    head.setItemMeta(headMeta);
    return head;
  }

  /**
   * Get a field from a class
   *
   * @param target The class to get the field from
   * @param name The name of the field
   * @param fieldType The type of the field
   * @param index The index of the field
   * @return The field
   * @param <T> The type of the field
   */
  private <T> Field getField(Class<?> target, String name, Class<T> fieldType, int index) {
    for (final Field field : target.getDeclaredFields()) {
      if ((name == null || field.getName().equals(name))
          && fieldType.isAssignableFrom(field.getType())
          && index-- <= 0) {
        field.setAccessible(true);
        return field;
      }
    }

    if (target.getSuperclass() != null) {
      return getField(target.getSuperclass(), name, fieldType, index);
    }

    throw new IllegalArgumentException("Cannot find field with type " + fieldType);
  }
  /**
   * Get a list of mask internal names from a helmet item
   *
   * @param item The helmet item
   * @return A list of mask internal names
   */
  private List<String> getMaskListFromItem(NBTItem item) {
    if (item.hasKey("mask")) { // If item has mask key
      return Arrays.asList(item.getString("mask").split(";")); // Return mask list
    }

    return Lists.newArrayList(); // Return empty list
  }

  /**
   * Same as {@link #updateMasks(Player)} but with a delay of 1 tick and so it will be scheduled.
   *
   * @param player The player to update the masks for
   */
  public void scheduleMaskUpdate(Player player) {
    Bukkit.getScheduler()
        .scheduleSyncDelayedTask(
            Masks.getInstance(), () -> updateMasks(player), 1L); // Schedule mask update
  }

  /**
   * Get all masks attached to a player
   *
   * @param player The player to get the masks from
   * @return A list of masks
   */
  public List<AbstractMask> getEquippedMasks(Player player) {
    return this.playerMasks.getOrDefault(player.getUniqueId(), Lists.newArrayList()).stream()
        .map(this::getMaskByName)
        .filter(Objects::nonNull)
        .collect(Collectors.toList()); // Return masks
  }

  /**
   * Get all masks attached to a player (ids)
   *
   * @param player The player to get the masks from
   * @return A list of mask ids
   */
  public List<String> getEquippedMaskIds(Player player) {
    return this.playerMasks.getOrDefault(player.getUniqueId(), Lists.newArrayList());
  }

  /**
   * Checks if the player has a mask equipped with the given internal name if the mask is not found,
   * optional will be empty
   *
   * @param player the player to check
   * @param mask the mask to check for
   * @return optional of the mask
   */
  public Optional<AbstractMask> hasMaskEquipped(Player player, String mask) {

    if (!this.playerMasks.containsKey(
        player.getUniqueId())) { // If player masks does not contain key
      return Optional.empty(); // Return empty optional
    }

    return playerMasks.containsKey(player.getUniqueId())
            && Objects.requireNonNull(playerMasks.get(player.getUniqueId())).contains(mask)
        ? Optional.ofNullable(getMaskByName(mask))
        : Optional.empty(); // Return optional
  }

  /**
   * Updates the player's masks
   *
   * @param player The player to update
   */
  public void updateMasks(Player player) {

    if (player.getInventory().getHelmet() == null) { // If helmet is null
      this.playerMasks.remove(player.getUniqueId()); // Remove player from player masks
      return; // Return
    }

    final NBTItem nbtItem = new NBTItem(player.getInventory().getHelmet()); // Create NBT item

    if (!nbtItem.hasKey("mask")) { // If item does not have mask key
      this.playerMasks.remove(player.getUniqueId()); // Remove player from player masks
      return; // Return
    }

    final List<String> maskIds =
        Arrays.asList(nbtItem.getString("mask").split(";")); // Get mask ids

    final List<AbstractMask> masks = getMaskList(maskIds); // Get mask list

    if (masks.isEmpty()) { // If mask list is empty
      this.playerMasks.remove(player.getUniqueId()); // Remove player masks
      return; // Return
    }

    if (!this.playerMasks.containsKey(
        player.getUniqueId())) { // If player masks does not contain key
      this.playerMasks.put(player.getUniqueId(), maskIds); // Put mask ids
    } else { // Else
      Objects.requireNonNull(this.playerMasks.get(player.getUniqueId())).stream() // Get mask ids
          .map(this::getMaskByName) // Map mask names to mask
          .filter(i -> i != null && i.getOnUnequip() != null) // Filter null and on unequip
          .forEach(
              i ->
                  i.getOnUnequip()
                      .accept(
                          this,
                          player)); // For each accept on unequip (basically un-equip all masks)

      masks.stream() // Get mask list
          .filter(
              i ->
                  !Objects.requireNonNull(this.playerMasks.get(player.getUniqueId()))
                      .contains(i.getInternalName())) // Filter mask list
          .filter(i -> i.getOnEquip() != null) // Filter on equip
          .forEach(
              i ->
                  i.getOnEquip()
                      .accept(
                          this, player)); // For each accept on equip (basically equip all masks)

      this.playerMasks.put(player.getUniqueId(), maskIds); // Replace mask ids

      player.sendMessage(
          "Masks - You have "
              + (maskIds.size()
                  + Util.pluralize("mask", 's', maskIds.size())
                  + " equipped")); // Send message
    }
  }
}
