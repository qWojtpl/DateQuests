package pl.datequests.quests;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import pl.datequests.DateQuests;

@Getter
@Setter
public class Quest {

    private String owner;
    private QuestSchema questSchema;
    private QuestState questState = QuestState.NOT_COMPLETED;
    private String event;
    private int progress = 0;
    private int requiredProgress = 1;
    private String dateTag;
    private int tagID;

    public void randomizeEvent() {
        setEvent(questSchema.getRandomEvent(owner));
        progress = 0;
    }

    public void setEvent(String event) {
        this.event = event;
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

    public void setTagID(int id) {
        this.tagID = id;
        if(this.questState.equals(QuestState.COMPLETED)) {
            return;
        }
        if(id < questSchema.getTagID()) {
            this.questState = QuestState.NOT_ACTIVE;
        }
    }

    /**
        Returns true if quest is completed
        @param progress Progress to add
        @return True or false
     */
    public boolean setProgress(int progress) {
        this.progress = progress;
        if(progress >= requiredProgress) {
            questState = QuestState.COMPLETED;
            return true;
        }
        return false;
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

    public void saveProgress() {
        DateQuests.getInstance().getDataHandler().saveQuestProgress(this);
    }

}
