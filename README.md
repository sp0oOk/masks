
# 🎭 Masks 🎭

Template for a 1.8.8 Minecraft Masks plugin, intended to be used to create cosmetic masks that provide visual override when equipped alongside special abilities and effects.

# ✨ Example ✨

```java
AbstractMask.create()
        .internalName("testMask")
        .displayName("&3&lTest Mask")
        .textureURL(
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmVlNTU0YWJlMTRmZGE5MmVmNWVjOTIxMjIyZmU2MGMyNjhhOGFiZGY0MTIwZDRmMjgzZTgwM2RlOGQzZmUwYiJ9fX0=")
        .lore(ImmutableList.of("&7This is a test mask", "&7It does nothing"))
        .build()
        .register();
```

![Use Example](https://i.gyazo.com/104f8f8b152dce4e40d18ffcabde98be.gif)

# ❗ Note ❗

This is bare bones, and is only created/uploaded here to provide some insight on how plugins that already do this work. Moreover, all the pain of intercepting packets is already handled for you!
