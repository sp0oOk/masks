package com.github.spook.masks.objects;

import com.github.spook.masks.enums.ManagerKillResult;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class Manager {

    protected abstract void onEnable(JavaPlugin plugin); // Called when the manager is enabled.
    protected abstract void onDisable(); // Called when the manager is disabled.
    protected abstract Result<ManagerKillResult, Exception> kill(); // Called when the manager is killed.

}
