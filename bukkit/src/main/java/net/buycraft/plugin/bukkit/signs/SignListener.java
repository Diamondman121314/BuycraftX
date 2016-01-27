package net.buycraft.plugin.bukkit.signs;

import lombok.RequiredArgsConstructor;
import net.buycraft.plugin.bukkit.BuycraftPlugin;
import net.buycraft.plugin.bukkit.tasks.SignUpdateApplication;
import net.buycraft.plugin.bukkit.tasks.SignUpdater;
import net.buycraft.plugin.bukkit.util.SerializedBlockLocation;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;

@RequiredArgsConstructor
public class SignListener implements Listener {
    private final BuycraftPlugin plugin;

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        boolean ourSign;
        try {
            ourSign = event.getLine(0).equalsIgnoreCase("[buycraft_rp]");
        } catch (IndexOutOfBoundsException e) {
            return;
        }

        if (!ourSign)
            return;

        int pos;
        try {
            pos = Integer.parseInt(event.getLine(1));
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            event.getPlayer().sendMessage(ChatColor.RED + "The second line must be a number.");
            return;
        }

        if (!event.getPlayer().hasPermission("buycraft.admin")) {
            event.getPlayer().sendMessage(ChatColor.RED + "You can't create Buycraft signs.");
            return;
        }

        plugin.getSignStorage().addSign(new PurchaseSignPosition(SerializedBlockLocation.fromBukkitLocation(
                event.getBlock().getLocation()), pos));
        event.getPlayer().sendMessage(ChatColor.GREEN + "Added new recent purchase sign!");

        for (int i = 0; i < 4; i++) {
            event.setLine(i, "");
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, new SignUpdater(plugin));
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getBlock().getType() == Material.WALL_SIGN || event.getBlock().getType() == Material.SIGN_POST) {
            if (plugin.getSignStorage().removeSign(event.getBlock().getLocation())) {
                event.getPlayer().sendMessage(ChatColor.RED + "Removed recent purchase sign!");
                Bukkit.getScheduler().runTaskAsynchronously(plugin, new SignUpdater(plugin));
            }
            return;
        }

        for (BlockFace face : SignUpdateApplication.FACES) {
            if (plugin.getSignStorage().removeSign(event.getBlock().getRelative(face).getLocation())) {
                event.getPlayer().sendMessage(ChatColor.RED + "Removed recent purchase sign!");
                Bukkit.getScheduler().runTaskAsynchronously(plugin, new SignUpdater(plugin));
                return;
            }
        }
    }
}