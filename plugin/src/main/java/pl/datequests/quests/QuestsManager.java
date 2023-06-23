package pl.datequests.quests;

import lombok.Getter;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import pl.datequests.DateQuests;
import pl.datequests.gui.SortType;
import pl.datequests.util.PlayerUtil;

import javax.annotation.Nullable;
import java.util.*;

@Getter
public class QuestsManager {

    private final DateQuests plugin = DateQuests.getInstance();
    private final List<QuestSchema> questSchemas = new ArrayList<>();
    private final HashMap<String, List<Quest>> quests = new HashMap<>();
    private int dateCheckTask = -1;

    public void addQuestSchema(QuestSchema schema) {
        questSchemas.add(schema);
    }

    public void addProgress(Quest quest, int progress) {
        if(!quest.getQuestState().equals(QuestState.NOT_COMPLETED)) {
            return;
        }
        boolean completed = quest.setProgress(quest.getProgress() + progress);
        Player p = PlayerUtil.getPlayer(quest.getOwner());
        if(p != null) {
            if(completed) {
                p.playSound(p, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);
                PlayerUtil.sendActionBarMessage(p, "§aYou completed quest " + quest.getQuestSchema().getSchemaName());
            } else {
                PlayerUtil.sendActionBarMessage(p, "§aYou scored in quest " + quest.getQuestSchema().getSchemaName());
            }
        }
        quest.saveProgress();
    }

    public void assignQuest(String player, Quest quest) {
        List<Quest> playerQuests = getPlayersQuests(player);
        playerQuests.add(quest);
        quests.put(player, playerQuests);
    }

    @Nullable
    public QuestSchema getQuestSchema(String name) {
        for(QuestSchema schema : questSchemas) {
            if(schema.getSchemaName().equals(name)) {
                return schema;
            }
        }
        return null;
    }

    public boolean isPlayerCanTakeQuest(String player, QuestSchema schema) {
        for(Quest quest : getPlayersQuestsBySchema(player, schema)) {
            if(quest.getTagID() >= schema.getTagID()) {
                return false;
            }
        }
        return true;
    }

    public boolean isQuestForEvent(Quest quest, String event, String subject) {
        if(quest.getEvent() == null) {
            return false;
        }
        String questEvent = quest.getEvent();
        String[] split = questEvent.split(" ");
        if(split.length != 3) {
            return false;
        }
        return split[0].equalsIgnoreCase(event) && split[2].equalsIgnoreCase(subject);
    }

    public List<Quest> getPlayersQuests(String player) {
        if(quests.containsKey(player)) {
            return quests.get(player);
        }
        return new ArrayList<>();
    }

    public List<Quest> getPlayersQuestsBySchema(String player, QuestSchema schema) {
        List<Quest> returnList = new ArrayList<>();
        for(Quest quest : getPlayersQuests(player)) {
            if(quest.getQuestSchema().equals(schema)) {
                returnList.add(quest);
            }
        }
        return returnList;
    }

    public List<Quest> getSortedQuests(List<Quest> questList, SortType sortType) {
        if(sortType.equals(SortType.NEWEST)) {
            return reverseList(questList);
        } else if(sortType.equals(SortType.OLDEST)) {
            return new ArrayList<>(questList);
        } else if(sortType.equals(SortType.COMPLETED)) {
            List<Quest> playerQuests = reverseList(questList);
            List<Quest> sortedQuests = new ArrayList<>();
            for(Quest quest : playerQuests) {
                if(quest.getQuestState().equals(QuestState.COMPLETED)) {
                    sortedQuests.add(quest);
                }
            }
            for(Quest quest : playerQuests) {
                if(!quest.getQuestState().equals(QuestState.COMPLETED)) {
                    sortedQuests.add(quest);
                }
            }
            return sortedQuests;
        } else {
            List<Quest> playerQuests = reverseList(questList);
            List<Quest> sortedQuests = new ArrayList<>();
            for(Quest quest : playerQuests) {
                if(quest.getQuestState().equals(QuestState.NOT_COMPLETED)) {
                    sortedQuests.add(quest);
                }
            }
            for(Quest quest : playerQuests) {
                if(!quest.getQuestState().equals(QuestState.NOT_COMPLETED)) {
                    sortedQuests.add(quest);
                }
            }
            return sortedQuests;
        }
    }

    public List<Quest> reverseList(List<Quest> list) {
        List<Quest> returnList = new ArrayList<>();
        for(int i = list.size() - 1; i >= 0; i--) {
            returnList.add(list.get(i));
        }
        return returnList;
    }

    public List<Quest> getPlayersActiveQuests(String player) {
        List<Quest> playerQuests = getPlayersQuests(player);
        List<Quest> activeQuests = new ArrayList<>();
        for(Quest q : playerQuests) {
            if(q.getQuestState().equals(QuestState.NOT_COMPLETED)) {
                activeQuests.add(q);
            }
        }
        return activeQuests;
    }

    @Nullable
    public QuestSchema getSchemaFromIndex(int index) {
        if(questSchemas.size() - 1 > index) {
            return null;
        }
        return questSchemas.get(index);
    }

    public void registerDateCheckTask() {
        if(dateCheckTask != -1) {
            plugin.getServer().getScheduler().cancelTask(dateCheckTask);
        }
        dateCheckTask = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            String newTag = plugin.getDateManager().getFormattedDate("%Y/%M/%D");
            for(QuestSchema schema : questSchemas) {
                if(newTag.equals(schema.getDateTag())) {
                    continue;
                }
                boolean changedTag = false;
                if(QuestInterval.DAY.equals(schema.getQuestInterval())) {
                    schema.setDateTag(newTag);
                    changedTag = true;
                } else if(QuestInterval.MONTH.equals(schema.getQuestInterval())) {
                    if(newTag.equals(plugin.getDateManager().getFormattedDate("%Y/%M/1"))) {
                        schema.setDateTag(newTag);
                        changedTag = true;
                    }
                } else {
                    if(plugin.getDateManager().getDayName().equalsIgnoreCase(schema.getQuestInterval().name())) {
                        schema.setDateTag(newTag);
                        changedTag = true;
                    }
                }
                if(changedTag) {
                    schema.setTagID(schema.getTagID() + 1);
                    DateQuests.getInstance().getDataHandler().saveSchemaTags(schema);
                }
            }
        }, 0L, 20L);
    }

}
