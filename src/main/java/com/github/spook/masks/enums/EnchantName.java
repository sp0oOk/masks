package com.github.spook.masks.enums;

@SuppressWarnings("unused")
public enum EnchantName {
  DAMAGE_ALL("Sharpness"),
  DAMAGE_ARTHROPODS("Bane of Arthropods"),
  DAMAGE_UNDEAD("Smite"),
  DIG_SPEED("Efficiency"),
  DURABILITY("Unbreaking"),
  FIRE_ASPECT("Fire Aspect"),
  KNOCKBACK("Knockback"),
  LOOT_BONUS_BLOCKS("Fortune"),
  LOOT_BONUS_MOBS("Looting"),
  OXYGEN("Respiration"),
  PROTECTION_ENVIRONMENTAL("Protection"),
  PROTECTION_EXPLOSIONS("Blast Protection"),
  PROTECTION_FALL("Feather Falling"),
  PROTECTION_FIRE("Fire Protection"),
  PROTECTION_PROJECTILE("Projectile Protection"),
  SILK_TOUCH("Silk Touch"),
  THORNS("Thorns"),
  WATER_WORKER("Aqua Affinity");

  private final String type;

  EnchantName(String type) {
    this.type = type;
  }

  public String getType() {
    return type;
  }
}
