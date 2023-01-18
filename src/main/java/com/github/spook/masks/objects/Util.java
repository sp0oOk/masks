package com.github.spook.masks.objects;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

public class Util {

  /**
   * Pluralizes a word.
   *
   * @param word The word to pluralize.
   * @param pluralChar The character to append to the word.
   * @param count The count of the word.
   * @return The pluralized word.
   */
  public static String pluralize(String word, char pluralChar, int count) {
    if (count == 1) {
      return word;
    }
    return word + pluralChar;
  }

  /**
   * Colorizes a string.
   *
   * @param string The string to colorize.
   * @return The colorized string.
   */
  public static String color(String string) {
    return ChatColor.translateAlternateColorCodes('&', string);
  }

  /**
   * Get formatted name of an item.
   *
   * @param stack The item to get the name of.
   * @return The formatted name of the item.
   */
  public static String getDisplayName(ItemStack stack) {
    return (stack.hasItemMeta() && stack.getItemMeta().hasDisplayName()
        ? stack.getItemMeta().getDisplayName()
        : ChatColor.AQUA
            + StringUtils.capitalize(stack.getType().name().toLowerCase().replace("_", " ")));
  }

  /**
   * Self-explanatory. Returns true if an item-stack is an armor piece.
   *
   * @param stack The item-stack to check.
   * @return True if the item-stack is an armor piece.
   */
  public static boolean isArmorPiece(ItemStack stack) {
    return stack.getType().name().endsWith("_HELMET")
        || stack.getType().name().endsWith("_CHESTPLATE")
        || stack.getType().name().endsWith("_LEGGINGS")
        || stack.getType().name().endsWith("_BOOTS");
  }
}
