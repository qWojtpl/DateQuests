package pl.datequests.quests;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;
import pl.datequests.DateQuests;
import pl.datequests.util.RandomNumber;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Getter
@Setter
public class QuestSchema {

    private String schemaName;
    private QuestInterval questInterval;
    private ItemStack changeQuestItem;
    private ItemStack icon;
    private String dateTag;
    private int tagID;
    private final List<QuestGroup> questGroups = new ArrayList<>();
    private final List<ItemStack> rewardForEvery = new ArrayList<>();
    private final List<ItemStack> rewardForAll = new ArrayList<>();
    private final HashMap<String, Integer> lastPlayerGroup = new HashMap<>();

    public void setDateTag(String dateTag) {
        this.dateTag = dateTag;
    }

    public String getRandomEvent(String player) {
        if(questGroups.size() == 0) {
            DateQuests.getInstance().getLogger().severe("Quest groups is empty!");
            return "ERROR";
        }
        int randomGroup = RandomNumber.randomInt(0, questGroups.size() - 1);
        if(questGroups.size() > 1) {
            if(lastPlayerGroup.containsKey(player)) {
                while(randomGroup == lastPlayerGroup.get(player)) {
                    randomGroup = RandomNumber.randomInt(0, questGroups.size() - 1);
                }
            }
            lastPlayerGroup.put(player, randomGroup);
            DateQuests.getInstance().getDataHandler().saveLastPlayerGroup(player, this, randomGroup);
        }
        QuestGroup questGroup = questGroups.get(randomGroup);
        int randomTask = RandomNumber.randomInt(0, questGroup.getRanges().size() - 1);
        String range = questGroup.getRanges().get(randomTask);
        String[] split = range.split("-");
        if(split.length != 2) {
            DateQuests.getInstance().getLogger().severe("Range " + range + " is not correct!");
            return "ERROR";
        }
        int min;
        int max;
        try {
            min = Integer.parseInt(split[0]);
            max = Integer.parseInt(split[1]);
        } catch(NumberFormatException e) {
            DateQuests.getInstance().getLogger().severe("Range " + range + " is not correct!");
            return "ERROR";
        }
        return questGroup.getEvents().get(randomTask).replace("%random%", RandomNumber.randomInt(min, max) + "");
    }

}
