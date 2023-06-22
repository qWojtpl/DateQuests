package pl.datequests.quests;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import pl.datequests.DateQuests;

import javax.annotation.Nullable;

@Getter
@Setter
public class Quest {

    private String owner;
    private QuestSchema questSchema;
    private QuestState questState = QuestState.NOT_COMPLETED;
    @Nullable
    private String event;
    private int progress = 0;
    private int requiredProgress = 1;
    private String dateTag;
    private int tagID;

    public void randomizeEvent() {
        event = questSchema.getRandomEvent(owner);
        progress = 0;
        loadRequiredProgress();
    }

    public void loadRequiredProgress() {
        if(event == null) {
            return;
        }
        String[] split = event.split(" ");
        if(split.length != 3) {
            DateQuests.getInstance().getLogger().severe(event + " is not a correct event!");
            requiredProgress = 1;
            return;
        }
        try {
            requiredProgress = Integer.parseInt(split[1]);
        } catch(NumberFormatException e) {
            DateQuests.getInstance().getLogger().severe(event + " is not a correct event!");
            requiredProgress = 1;
        }
    }

    public Material getEventMaterial() {
        if(event == null) {
            return Material.BEDROCK;
        }
        String[] split = event.split(" ");
        if(split.length != 3) {
            return Material.BEDROCK;
        }
        Material m = Material.getMaterial(split[2].toUpperCase());
        if(m == null) {
            if(split[0].equalsIgnoreCase("kill")) {
                m = Material.getMaterial(split[2].toUpperCase() + "_SPAWN_EGG");
                if(m == null) {
                    m = Material.BEDROCK;
                }
            } else {
                m = Material.BEDROCK;
            }
        }
        return m;
    }

    public void save() {
        DateQuests.getInstance().getDataHandler().saveQuest(this);
    }

}
