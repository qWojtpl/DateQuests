package pl.datequests.citizens;

import lombok.Getter;
import lombok.Setter;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import pl.datequests.DateQuests;
import pl.datequests.quests.Quest;
import pl.datequests.quests.QuestState;
import pl.datequests.quests.QuestsManager;

@Getter
@Setter
public class CitizensController implements Listener {

    private final DateQuests plugin = DateQuests.getInstance();
    private final QuestsManager questsManager = plugin.getQuestsManager();
    private String npcName;

    @EventHandler
    public void onClick(NPCRightClickEvent event) {
        if(event.isCancelled()) {
            return;
        }
        if(!event.getNPC().getName().equals(npcName)) {
            return;
        }
        String player = event.getClicker().getName();
        Player p = event.getClicker();
        ItemStack playerItem = p.getInventory().getItemInMainHand();
        if(playerItem.getType().equals(Material.AIR)) {
            plugin.getCommands().openGUI(p);
            return;
        }
        boolean found = false;
        for(Quest q : questsManager.getPlayersActiveQuests(player)) {
            if(!questsManager.isQuestForEvent(q, "deliver", playerItem.getType().name())) {
                continue;
            }
            while(playerItem.getAmount() > 0 && q.getQuestState().equals(QuestState.NOT_COMPLETED)) {
                playerItem.setAmount(playerItem.getAmount() - 1);
                questsManager.addProgress(q, 1);
                found = true;
            }
            p.updateInventory();
        }
        if(!found) {
            plugin.getCommands().openGUI(p);
        }
    }

}
