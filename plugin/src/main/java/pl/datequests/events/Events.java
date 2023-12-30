package pl.datequests.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import pl.datequests.DateQuests;
import pl.datequests.quests.Quest;
import pl.datequests.quests.QuestsManager;

public class Events implements Listener {

    private final DateQuests plugin = DateQuests.getInstance();
    private final QuestsManager questsManager = plugin.getQuestsManager();

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if(!plugin.getDataHandler().isLoadAllPlayers()) {
            plugin.getDataHandler().loadPlayer(event.getPlayer().getName());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBreak(BlockBreakEvent event) {
        if(event.isCancelled()) {
            return;
        }
        String player = event.getPlayer().getName();
        for(Quest q : questsManager.getPlayersActiveQuests(player)) {
            if(questsManager.isQuestForEvent(q, "break", event.getBlock().getType().name())) {
                questsManager.addProgress(q, 1);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onKill(EntityDeathEvent event) {
        if(event.getEntity().getKiller() == null) {
            return;
        }
        String player = event.getEntity().getKiller().getName();
        for(Quest q : questsManager.getPlayersActiveQuests(player)) {
            if(questsManager.isQuestForEvent(q, "kill", event.getEntity().getType().name())) {
                questsManager.addProgress(q, 1);
            }
        }
    }

}
