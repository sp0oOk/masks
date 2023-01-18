package com.github.spook.masks.commands;

import com.github.spook.masks.Masks;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class CommandEquippedTest implements CommandExecutor {
  @Override
  public boolean onCommand(
      CommandSender commandSender, Command command, String s, String[] strings) {

    if (!(commandSender instanceof Player)) {
      commandSender.sendMessage("You must be a player to use this command!");
      return true;
    }

    Player player = (Player) commandSender;

    player.sendMessage(
        Arrays.toString(Masks.getInstance().getMaskManager().getEquippedMaskIds(player).toArray())
            + " are equipped (testCondition: "
            + Masks.getInstance().getMaskManager().hasMaskEquipped(player, "testMask").isPresent()
            + ")");

    return true;
  }
}
