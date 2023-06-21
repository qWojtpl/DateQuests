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
    private ItemStack icon;
    private String dateTag;
    private int tagID;
    private final HashMap<Integer, QuestGroup> questGroups = new HashMap<>();
    private final HashMap<Integer, ItemStack> rewardForEvery = new HashMap<>();
    private final HashMap<Integer, ItemStack> rewardForAll = new HashMap<>();

    public void setDateTag(String dateTag) {
        this.dateTag = dateTag;
        tagID++;
    }

}
