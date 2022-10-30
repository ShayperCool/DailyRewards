package cz.revivalo.dailyrewards.rewardmanager;

import cz.revivalo.dailyrewards.DailyRewards;
import cz.revivalo.dailyrewards.lang.Lang;
import cz.revivalo.dailyrewards.playerconfig.PlayerData;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.logging.Logger;

public class DailyRewardByProgression {

    private Map<String, RewardPerDay> rewardsPerDay;
    private boolean isEnabled = false;
    private boolean isTest = false;
    private Logger logger;

    public DailyRewardByProgression(DailyRewards plugin){
        LoadConfig(plugin);
    }

    public void reload(){
        LoadConfig(DailyRewards.getPlugin(DailyRewards.class));
    }

    private void LoadConfig(DailyRewards plugin) {
        rewardsPerDay = new HashMap<>();
        logger = plugin.getLogger();

        FileConfiguration cfg = DailyRewards.getPlugin(DailyRewards.class).getConfig();

        isEnabled = cfg.getBoolean("config.daily-progression-enabled");
        isTest = cfg.getBoolean("config.daily-progression-test");

        if(isEnabled){
            ConfigurationSection progressionSection = cfg.getConfigurationSection("config.daily-progression");
            if(progressionSection != null){
                for (String key : progressionSection.getKeys(false)) {
                    ConfigurationSection dayRewardSection = progressionSection.getConfigurationSection(key);
                    RewardPerDay reward = new RewardPerDay();

                    reward.rewards = dayRewardSection.getStringList("rewards");
                    reward.lore = dayRewardSection.getStringList("lore");
                    reward.title = dayRewardSection.getString("title");
                    reward.subtitle = dayRewardSection.getString("subtitle");

                    if (!rewardsPerDay.containsKey(key)){
                        rewardsPerDay.put(key, reward);
                    }
                    else{
                        logger.warning("Reward for " + key + " was already added");
                    }
                }
            }
        }
    }

    public boolean getIsEnabled(){
        return isEnabled;
    }

    public boolean getIsTest(){
        return isTest;
    }

    public boolean isLimitNotReached(Player player){
        FileConfiguration data = PlayerData.getConfig(player);
        if (!data.isInt("rewards.dailyProgression")){
            data.set("rewards.dailyProgression", 1);
        }

        int rewardDay = data.getInt("rewards.dailyProgression");

        String rewardKey = "day" + rewardDay;

        return rewardsPerDay.containsKey(rewardKey);
    }

    public void reward(Player player){
        FileConfiguration data = PlayerData.getConfig(player);
        if (!data.isInt("rewards.dailyProgression")){
            data.set("rewards.dailyProgression", 1);
        }

        int rewardDay = data.getInt("rewards.dailyProgression");

        String rewardKey = "day" + rewardDay;

        logger.info(player.getName() + "will get reward for " + rewardKey);

        if (rewardsPerDay.containsKey(rewardKey)){
            data.set("rewards.dailyProgression", rewardDay + 1);
            RewardPerDay reward = rewardsPerDay.get(rewardKey);
            GiveRewards(player, reward);
            PrintLore(player, reward);
            ShowRewardTitle(player, reward);
        }else{
            logger.warning("here is no reward for: " + rewardKey);
        }
    }

    private static void ShowRewardTitle(Player player, RewardPerDay reward) {
        player.playSound(player.getLocation(), Sound.valueOf(Lang.DAILYSOUND.content(player).toUpperCase(Locale.ENGLISH)), 1F, 1F);
        player.sendTitle(PlaceholderAPI.setPlaceholders(player, Lang.applyColor(reward.title)), PlaceholderAPI.setPlaceholders(player, Lang.applyColor(reward.subtitle)), 15, 35, 15);
    }

    private static void GiveRewards(Player player, RewardPerDay reward) {
        ConsoleCommandSender console = Bukkit.getConsoleSender();
        List<String> rewards = reward.rewards;
        if (rewards.size() != 0) {
            for (String rewardStr : rewards) {
                String str = PlaceholderAPI.setPlaceholders(player, rewardStr);
                Bukkit.dispatchCommand(console, str.replace("%random%", String.valueOf(new Random().nextInt(200) + 100)).replace("%player%", player.getName()));
            }
        } else {
            player.sendMessage(Lang.REWARDDONTSET.content(player));
        }
    }

    private static void PrintLore(Player player, RewardPerDay reward){
        for (String lore : reward.lore) {
            player.sendMessage(PlaceholderAPI.setPlaceholders(player, Lang.applyColor(lore)));
        }
    }

    private class RewardPerDay{
        public List<String> rewards;
        public List<String> lore;
        public String title;
        public String subtitle;
    }

}
