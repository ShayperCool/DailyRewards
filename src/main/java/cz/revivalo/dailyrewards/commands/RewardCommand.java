package cz.revivalo.dailyrewards.commands;

import com.tchristofferson.configupdater.ConfigUpdater;
import cz.revivalo.dailyrewards.DailyRewards;
import cz.revivalo.dailyrewards.guimanager.GuiManager;
import cz.revivalo.dailyrewards.lang.Lang;
import cz.revivalo.dailyrewards.rewardmanager.DailyRewardByProgression;
import cz.revivalo.dailyrewards.rewardmanager.RewardManager;
import cz.revivalo.dailyrewards.rewardmanager.RewardTypesConstants;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

public class RewardCommand implements CommandExecutor {
    private final DailyRewards plugin;
    private final RewardManager rewardManager;
    private final GuiManager guiManager;
    private final DailyRewardByProgression dailyRewardByProgression;
    public RewardCommand(DailyRewards plugin) {
        this.plugin = plugin;
        rewardManager = plugin.getRewardManager();
        guiManager = plugin.getGuiManager();
        dailyRewardByProgression = plugin.getDailyRewardByProgression();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        //final boolean fromPlayer = sender instanceof Player;
        if (!(sender instanceof Player)){
            sender.sendMessage("[DailyRewards] Only in-game command!");
            return true;
        } else {
            Player player = (Player) sender;
            if (cmd.getName().equalsIgnoreCase("reward")){
                switch (args.length){
                    case 0:
                        rewardManager.claim(player, RewardTypesConstants.DAILY, true);
                        break;
                    case 1:
                        switch (args[0]){
                            case RewardTypesConstants.WEEKLY:
                                rewardManager.claim(player, RewardTypesConstants.WEEKLY, true);
                                break;
                            case RewardTypesConstants.MONTHLY:
                                rewardManager.claim(player, RewardTypesConstants.MONTHLY, true);
                                break;
                            case "reload":
                                if (!player.hasPermission("dailyreward.manage")){
                                    player.sendMessage(Lang.PERMISSIONMSG.content(player));
                                } else {
                                    this.plugin.reloadConfig();
                                    File configFile = new File(Bukkit.getServer().getPluginManager().getPlugin("DailyRewards").getDataFolder(), "config.yml");

                                    try {
                                        ConfigUpdater.update(this.plugin, "config.yml", configFile, Collections.emptyList());
                                    } catch (IOException ex) {
                                        ex.printStackTrace();
                                    }

                                    Lang.reload();
                                    dailyRewardByProgression.reload();
                                    player.sendMessage(Lang.RELOADMSG.content(player));
                                }
                                break;
                        }
                        break;
                    case 3:
                        switch (args[0]){
                            case "reset":
                                if (!player.hasPermission("dailyreward.manage")){
                                    player.sendMessage(Lang.PERMISSIONMSG.content(player));
                                } else {
                                    if (rewardManager.reset(Bukkit.getOfflinePlayer(args[1]), args[2])) {
                                        player.sendMessage(Lang.REWARDRESET.content(player).replace("%type%", args[2]).replace("%player%", args[1]));
                                    } else {
                                        player.sendMessage(Lang.UNAVAILABLEPLAYER.content(player).replace("%player%", args[1]));
                                    }
                                }
                                break;
                        }
                }
            } else if (cmd.getName().equalsIgnoreCase("rewards")){
                player.openInventory(guiManager.openRewardsMenu(player));
            }
        }
        return true;
    }
}
