package com.github.spook.masks.commands;

import com.github.spook.masks.Masks;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CommandTest implements CommandExecutor {

  @Override
  public boolean onCommand(
      CommandSender commandSender, Command command, String s, String[] strings) {

    if (!(commandSender instanceof Player)) {
      commandSender.sendMessage("You must be a player to use this command!");
      return true;
    }

    Player player = (Player) commandSender;

    final ItemStack stack =
        Masks.getInstance()
            .getMaskManager()
            .addOrAppendMask(new ItemStack(Material.DIAMOND_HELMET, 1), "testMask");

    player.getInventory().addItem(stack);

    player.sendMessage("Added mask to item in hand!");

    return true;
  }
}
