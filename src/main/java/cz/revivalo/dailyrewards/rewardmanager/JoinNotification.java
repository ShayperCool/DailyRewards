package cz.revivalo.dailyrewards.rewardmanager;

import cz.revivalo.dailyrewards.DailyRewards;
import cz.revivalo.dailyrewards.lang.Lang;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class JoinNotification implements Listener {
    private final DailyRewards plugin;
    private final Cooldowns cooldowns;

    private final DailyRewardByProgression dailyRewardByProgression;
    public JoinNotification(final DailyRewards plugin) {
        this.plugin = plugin;
        cooldowns = plugin.getCooldowns();
        dailyRewardByProgression = plugin.getDailyRewardByProgression();
    }

    @EventHandler (priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onJoin(final PlayerJoinEvent event){
        final Player player = event.getPlayer();
        if (!Lang.AUTOMATICALLYACTIVATE.getBoolean()) cooldowns.set(player);
        if (Lang.ENABLEJOINNOTIFICATION.getBoolean()) {
            boolean isDailyRewardAvailable = isDailyRewardAvailable(player);
            if (isDailyRewardAvailable) {
                new BukkitRunnable(){
                    @Override
                    public void run() {
                        for (String line : Lang.JOINNOTIFICATION.contentLore(player)){
                            TextComponent joinMsg = new TextComponent(line);
                            joinMsg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/reward"));
                            joinMsg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(Lang.JOINHOVERMESSAGE.content(player)).create()));
                            player.spigot().sendMessage(joinMsg);
                        }
                    }
                }.runTaskLater(plugin, Lang.JOINNOTIFICATIONDELAY.getInt() * 20L);
            }
        }
    }

    private boolean isDailyRewardAvailable(Player player){
        return (Long.parseLong(cooldowns.getCooldown(player, "daily", false)) < 0 || dailyRewardByProgression.getIsTest()) &&
                (player.hasPermission("dailyreward.daily") || player.hasPermission("dailyreward.daily.premium")) &&
                dailyRewardByProgression.isLimitNotReached(player);
    }
}
