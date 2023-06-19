package pl.datequests.quests;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

@Getter
@Setter
public class QuestSchema {

    private String schemaName;
    private QuestInterval questInterval;
    private ItemStack changeQuestItem;
    private final HashMap<Integer, QuestGroup> questGroups = new HashMap<>();
    private final HashMap<Integer, ItemStack> rewardForEvery = new HashMap<>();
    private final HashMap<Integer, ItemStack> rewardForAll = new HashMap<>();

}
