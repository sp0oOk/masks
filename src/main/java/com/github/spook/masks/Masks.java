package com.github.spook.masks;

import com.github.spook.masks.commands.CommandEquippedTest;
import com.github.spook.masks.commands.CommandTest;
import com.github.spook.masks.enums.ManagerKillResult;
import com.github.spook.masks.listeners.MaskUpdatesListener;
import com.github.spook.masks.managers.MaskManager;
import com.github.spook.masks.objects.Result;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class Masks extends JavaPlugin {

  private static Masks instance; // singleton instance
  private MaskManager maskManager; // mask manager

  @Override
  public void onEnable() {
    instance = this;
    this.maskManager = new MaskManager();
    this.maskManager.onEnable(this);

    if (!getServer().getPluginManager().isPluginEnabled("ProtocolLib")) {
      getLogger().severe("ProtocolLib is not installed or enabled!");
      final Result<ManagerKillResult, Exception> result = this.maskManager.kill();
      if (result.isError()) {
        getLogger().severe("Failed to kill MaskManager, disabling plugin!");
        result.getError().printStackTrace();
        getServer().getPluginManager().disablePlugin(this);
        return;
      }
      getLogger()
          .severe(
              "MaskManager was disabled, the plugin will not function ("
                  + result.getValue().getMessage()
                  + ")");
      maskManager = null;
    }

    getCommand("test").setExecutor(new CommandTest());
    getCommand("equipped").setExecutor(new CommandEquippedTest());

    registerListeners(new MaskUpdatesListener());
  }

  @Override
  public void onDisable() {
    maskManager.onDisable();
    maskManager = null;
    instance = null;
  }

  /**
   * Bulk register listener classes.
   *
   * @param listeners The listeners to register.
   */
  private void registerListeners(Listener... listeners) {
    for (Listener listener : listeners) {
      getServer().getPluginManager().registerEvents(listener, this);
      getLogger().info("Registered listener - " + listener.getClass().getSimpleName());
    }
  }

  /** Friendly plugin instance getter. */
  public static Masks getInstance() {
    return instance;
  }

  /** Friendly mask manager getter. */
  public MaskManager getMaskManager() {
    return maskManager;
  }
}
