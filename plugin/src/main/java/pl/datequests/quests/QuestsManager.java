package pl.datequests.quests;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import pl.beaverlib.util.DateManager;
import pl.beaverlib.util.PlayerUtil;
import pl.datequests.DateQuests;
import pl.datequests.data.MessagesManager;
import pl.datequests.gui.SortType;
import pl.datequests.util.RandomNumber;

import javax.annotation.Nullable;
import java.text.MessageFormat;
import java.util.*;

@Getter
public class QuestsManager {

    private final DateQuests plugin = DateQuests.getInstance();
    private final MessagesManager messages = plugin.getMessagesManager();
    private final List<QuestSchema> questSchemas = new ArrayList<>();
    private final HashMap<String, List<Quest>> quests = new HashMap<>();
    private final HashMap<String, List<ItemStack>> rewards = new HashMap<>();
    private final HashMap<String, List<Integer>> specialRewardReceived = new HashMap<>();
    @Setter
    private List<ItemStack> specialReward = new ArrayList<>();
    @Setter
    private RewardType specialRewardType;
    @Setter
    private double specialRewardPercentage = 90;
    private int dateCheckTask = -1;

    public void addQuestSchema(QuestSchema schema) {
        questSchemas.add(schema);
    }

    public void addProgress(Quest quest, int progress) {
        if(!quest.getQuestState().equals(QuestState.NOT_COMPLETED)) {
            return;
        }
        boolean completed = quest.setProgress(quest.getProgress() + progress);
        String player = quest.getOwner();
        Player p = PlayerUtil.getPlayer(player);
        if(p != null) {
            if(completed) {
                p.playSound(p, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);
                QuestSchema questSchema = quest.getQuestSchema();
                p.sendMessage("§6{========================}");
                p.sendMessage(" ");
                p.sendMessage(" " + messages.getMessage("completed") + "§2" + questSchema.getSchemaName());
                if(!plugin.isUsingNBTAPI()) {
                    p.sendMessage(" §2" + quest.getTranslatedEvent());
                } else {
                    plugin.getNbtAPIController().sendTranslatedEvent(p, " ", quest.getTranslatedEvent(), "");
                    plugin.getNbtAPIController().sendTranslatedSubtitle(p, quest.getTranslatedEvent());
                }
                p.sendMessage(" " + MessageFormat.format(messages.getMessage("progress"),
                        quest.getProgress(),
                        quest.getRequiredProgress()));
                if(questSchema.getRewards().size() != 0) {
                    if(questSchema.getRewardType().equals(RewardType.ALL)) {
                        for(ItemStack is : questSchema.getRewards()) {
                            assignReward(player, is);
                        }
                    } else if(questSchema.getRewardType().equals(RewardType.RANDOM)) {
                        assignReward(player,
                                questSchema.getRewards().get(RandomNumber.randomInt(0, questSchema.getRewards().size() - 1)));
                    }
                    p.sendMessage(" " + messages.getMessage("newAssignedReward"));
                }
                if(isCanTakeSpecialReward(player) && specialReward.size() != 0) {
                    if(!specialRewardReceived.getOrDefault(player, new ArrayList<>()).contains(DateManager.getMonth())) {
                        p.sendMessage(" " + messages.getMessage("assignedSpecialReward"));
                        assignSpecialReward(player);
                        List<Integer> list = specialRewardReceived.getOrDefault(player, new ArrayList<>());
                        list.add(DateManager.getMonth());
                        specialRewardReceived.put(player, list);
                        plugin.getDataHandler().saveReceivedSpecialReward(player, DateManager.getMonth());
                    }
                }
                p.sendMessage(" ");
                p.sendMessage("§6{========================}");
                if(plugin.isUsingPlaceholders()) {
                    plugin.getPlaceholderController().addScore(player, 1);
                }
            } else {
                PlayerUtil.sendActionBarMessage(p,
                        MessageFormat.format(messages.getMessage("scoredInQuest"), quest.getQuestSchema().getSchemaName()));
            }
        }
        quest.saveProgress();
        plugin.getDataHandler().savePlayerRewards(player);
    }

    public void assignQuest(String player, Quest quest) {
        List<Quest> playerQuests = getPlayersQuests(player);
        playerQuests.add(quest);
        quests.put(player, playerQuests);
    }

    public void assignReward(String player, ItemStack itemStack) {
        List<ItemStack> playerRewards = getPlayersRewards(player);
        playerRewards.add(itemStack);
        rewards.put(player, playerRewards);
    }

    public void assignSpecialReward(String player) {
        if(specialRewardType.equals(RewardType.ALL)) {
            for(ItemStack is : specialReward) {
                ItemMeta im = is.getItemMeta();
                if(im != null) {
                    im.setDisplayName(im.getDisplayName()
                            .replace("%year%", DateManager.getFormattedDate("%Y"))
                            .replace("%month%", DateManager.getFormattedDate("%M"))
                            .replace("%monthname%", messages.getMessage("month" + DateManager.getMonthName())));
                    if(im.getLore() != null) {
                        List<String> lore = im.getLore();
                        List<String> newLore = new ArrayList<>();
                        for(String line : lore) {
                            newLore.add(line
                                    .replace("%year%", DateManager.getFormattedDate("%Y"))
                                    .replace("%month%", DateManager.getFormattedDate("%M"))
                                    .replace("%monthname%", messages.getMessage("month" + DateManager.getMonthName())));
                        }
                        im.setLore(newLore);
                    }
                    is.setItemMeta(im);
                }
                assignReward(player, is);
            }
        } else if(specialRewardType.equals(RewardType.RANDOM)) {
            if(specialReward.size() == 0) {
                return;
            }
            ItemStack is = specialReward.get(RandomNumber.randomInt(0, specialReward.size() - 1));
            ItemMeta im = is.getItemMeta();
            if(im != null) {
                im.setDisplayName(im.getDisplayName()
                        .replace("%year%", DateManager.getFormattedDate("%Y"))
                        .replace("%month%", DateManager.getFormattedDate("%M"))
                        .replace("%monthname%", messages.getMessage("month" + DateManager.getMonthName())));
                if(im.getLore() != null) {
                    List<String> lore = im.getLore();
                    List<String> newLore = new ArrayList<>();
                    for(String line : lore) {
                        newLore.add(line
                                .replace("%year%", DateManager.getFormattedDate("%Y"))
                                .replace("%month%", DateManager.getFormattedDate("%M"))
                                .replace("%monthname%", messages.getMessage("month" + DateManager.getMonthName())));
                    }
                    im.setLore(newLore);
                }
                is.setItemMeta(im);
            }
            assignReward(player, is);
        }
    }

    public boolean takeQuest(String player, QuestSchema schema) {
        if(!isPlayerCanTakeQuest(player, schema)) {
            return false;
        }
        Player p = PlayerUtil.getPlayer(player);
        if(p == null) {
            return false;
        }
        Quest q = new Quest();
        q.setOwner(player);
        q.setQuestSchema(schema);
        q.setDateTag(schema.getDateTag());
        q.setTagID(schema.getTagID());
        q.randomizeEvent();
        assignQuest(player, q);
        q.save();
        PlayerUtil.sendTitle(
                p, getMessages().getMessage("acceptedQuest") + "§6" + schema.getSchemaName(),
                "§6" + q.getTranslatedEvent(),
                10,
                100,
                10
        );
        p.sendMessage("§6{========================}");
        p.sendMessage(" ");
        p.sendMessage(" " + getMessages().getMessage("acceptedQuest") + "§2" + schema.getSchemaName());
        if(!plugin.isUsingNBTAPI()) {
            p.sendMessage(" §2" + q.getTranslatedEvent());
        } else {
            plugin.getNbtAPIController().sendTranslatedEvent(p, " ", q.getTranslatedEvent(), "");
            plugin.getNbtAPIController().sendTranslatedSubtitle(p, q.getTranslatedEvent());
        }
        p.sendMessage(" " + MessageFormat.format(getMessages().getMessage("progress"), 0, q.getRequiredProgress()));
        p.sendMessage(" ");
        p.sendMessage("§6{========================}");
        p.playSound(p, Sound.ENTITY_VILLAGER_TRADE, 1.0F, 1.25F);
        return true;
    }

    public void receiveReward(String player, int rewardIndex) {
        List<ItemStack> playerRewards = getPlayersRewards(player);
        if(rewardIndex > playerRewards.size() - 1) {
            return;
        }
        ItemStack is = playerRewards.get(rewardIndex);
        Player p = PlayerUtil.getPlayer(player);
        if(p != null) {
            HashMap<Integer, ItemStack> rest = p.getInventory().addItem(is);
            for(int key : rest.keySet()) {
                p.getWorld().dropItem(p.getLocation(), rest.get(key));
            }
            playerRewards.remove(rewardIndex);
            rewards.put(player, playerRewards);
            plugin.getDataHandler().savePlayerRewards(player);
            p.playSound(p, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 2.0F);
        }
    }

    public void removeReward(String player, int rewardIndex) {
        List<ItemStack> playerRewards = getPlayersRewards(player);
        if(rewardIndex > playerRewards.size() - 1) {
            return;
        }
        playerRewards.remove(rewardIndex);
        rewards.put(player, playerRewards);
        plugin.getDataHandler().savePlayerRewards(player);
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

    public boolean isCanTakeSpecialReward(String player) {
        if(!DateManager.getFormattedDate("%Y/%M/%D").equals(DateManager.getFormattedDate("%Y/%M/" + DateManager.getDaysOfMonth()))) {
            return false;
        }
        double completed = 0;
        double all = 0;
        for(QuestSchema schema : questSchemas) {
            String month = DateManager.getFormattedDate("%Y/%M");
            if(!schema.getMonthTags().containsKey(month)) {
                continue;
            }
            List<Integer> monthTags = schema.getMonthTags().get(month);
            all += monthTags.size();
            List<Quest> playerQuests = getPlayersQuestsBySchema(player, schema);
            for(int tag : monthTags) {
                for(Quest q : playerQuests) {
                    if(q.getTagID() == tag && q.getQuestState().equals(QuestState.COMPLETED)) {
                        completed++;
                        break;
                    }
                }
            }
        }
        if(all == 0) {
            return false;
        }
        return (completed/all) * 100 >= specialRewardPercentage;
    }

    public List<ItemStack> getPlayersRewards(String player) {
        return rewards.getOrDefault(player, new ArrayList<>());
    }

    public List<Quest> getPlayersQuests(String player) {
        return quests.getOrDefault(player, new ArrayList<>());
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
                if(!quest.getQuestState().equals(QuestState.COMPLETED)) {
                    sortedQuests.add(quest);
                }
            }
            for(Quest quest : playerQuests) {
                if(quest.getQuestState().equals(QuestState.COMPLETED)) {
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
        if(index > questSchemas.size() - 1) {
            return null;
        }
        return questSchemas.get(index);
    }

    public Material getEventMaterial(String event) {
        if(event == null) {
            plugin.getLogger().severe("Event is null.");
            return Material.BEDROCK;
        }
        String[] split = event.split(" ");
        if(split.length != 3) {
            plugin.getLogger().severe("Event split is not 3.");
            return Material.BEDROCK;
        }
        Material m = Material.getMaterial(split[2].toUpperCase());
        if(split[0].equalsIgnoreCase("kill") || split[0].equalsIgnoreCase(messages.getMessage("eventKill"))) {
            m = Material.getMaterial(split[2].toUpperCase() + "_SPAWN_EGG");
        }
        if(m == null) {
            m = Material.BEDROCK;
            plugin.getLogger().severe("Event: " + event + " is incorrect.");
        }
        return m;
    }

    public void changeActiveQuestEvent(String player, QuestSchema schema) {
        Player p = PlayerUtil.getPlayer(player);
        if(p == null) {
            return;
        }
        if(!schema.isChangeable()) {
            p.playSound(p, Sound.ENTITY_VILLAGER_NO, 1.0F, 1.5F);
            p.sendMessage(messages.getMessage("cantChangeQuest"));
            return;
        }
        List<Quest> playerQuests = getPlayersQuestsBySchema(player, schema);
        Quest quest = null;
        for(Quest q : playerQuests) {
            if(q.getQuestState().equals(QuestState.NOT_COMPLETED)) {
                quest = q;
                break;
            }
        }
        if(quest == null) {
            p.playSound(p, Sound.ENTITY_VILLAGER_NO, 1.0F, 1.5F);
            p.sendMessage(messages.getMessage("noActiveQuest"));
            return;
        }
        if(quest.isChanged()) {
            p.playSound(p, Sound.ENTITY_VILLAGER_NO, 1.0F, 1.5F);
            p.sendMessage(messages.getMessage("questAlreadyChanged"));
            return;
        }
        List<ItemStack> items = new ArrayList<>();
        items.add(schema.getChangeQuestItem());
        List<Integer> itemSlots = getItemSlots(p, items);
        if(itemSlots.size() == 0) {
            p.sendMessage(messages.getMessage("noRequiredItems"));
            return;
        }
        takeItems(p, itemSlots, items);
        quest.randomizeEvent();
        quest.setChanged(true);
        quest.save();
        p.playSound(p, Sound.BLOCK_END_PORTAL_SPAWN, 1.0F, 1.0F);
        PlayerUtil.sendTitle(p,
                getMessages().getMessage("changedQuest") + "§6" + schema.getSchemaName(),
                "§6" + quest.getTranslatedEvent(),
                10,
                100,
                10);
        p.sendMessage("§6{========================}");
        p.sendMessage(" ");
        p.sendMessage(" " + getMessages().getMessage("changedQuest") + "§2" + schema.getSchemaName());
        if(!plugin.isUsingNBTAPI()) {
            p.sendMessage(" §2" + quest.getTranslatedEvent());
        } else {
            plugin.getNbtAPIController().sendTranslatedEvent(p, " ", quest.getTranslatedEvent(), "");
            plugin.getNbtAPIController().sendTranslatedSubtitle(p, quest.getTranslatedEvent());
        }
        p.sendMessage(" " + MessageFormat.format(getMessages().getMessage("progress"), 0, quest.getRequiredProgress()));
        p.sendMessage(" ");
        p.sendMessage("§6{========================}");
    }

    public List<Integer> getItemSlots(Player player, List<ItemStack> items) {
        List<Integer> slots = new ArrayList<>();
        int goodItems = 0;
        for(ItemStack is : items) {
            int totalAmount = 0;
            for(int i = 0; i < 36; i++) {
                ItemStack inventoryItem = player.getInventory().getItem(i);
                if(inventoryItem == null) {
                    continue;
                }
                if(!isSimilar(inventoryItem, is)) {
                    continue;
                }
                totalAmount += inventoryItem.getAmount();
                slots.add(i);
            }
            if(totalAmount < is.getAmount()) return new ArrayList<>();
            goodItems++;
        }
        if(goodItems < items.size()) return new ArrayList<>();
        return slots;
    }

    public void takeItems(Player player, List<Integer> checkSlots, List<ItemStack> items) {
        for(ItemStack is : items) {
            int required = is.getAmount();
            for(int slot : checkSlots) {
                ItemStack inventoryItem = player.getInventory().getItem(slot);
                if(inventoryItem == null) {
                    continue;
                }
                if(!isSimilar(inventoryItem, is)) {
                    continue;
                }
                required -= inventoryItem.getAmount();
                if(required > 0) {
                    inventoryItem.setAmount(0);
                } else {
                    inventoryItem.setAmount(-required);
                    break;
                }
            }
        }
        player.updateInventory();
    }

    public boolean isSimilar(ItemStack inventoryItem, ItemStack is) {
        if(inventoryItem == null || is == null) {
            return false;
        }
        if(!inventoryItem.getType().equals(is.getType())) {
            return false;
        }
        if(inventoryItem.getItemMeta() == null) {
            return false;
        }
        if(is.getItemMeta() == null) {
            return false;
        }
        if(!inventoryItem.getItemMeta().getDisplayName().equals(is.getItemMeta().getDisplayName())) {
            return false;
        }
        if(inventoryItem.getItemMeta().getLore() == null && is.getItemMeta().getLore() != null) {
            return false;
        }
        if(inventoryItem.getItemMeta().getLore() != null && is.getItemMeta().getLore() == null) {
            return false;
        }
        if(inventoryItem.getItemMeta().getLore() != null) {
            if(is.getItemMeta().getLore() != null) {
                if(!inventoryItem.getItemMeta().getLore().equals(is.getItemMeta().getLore())) {
                    return false;
                }
            }
        }
        if(!inventoryItem.getItemMeta().getEnchants().equals(is.getEnchantments())) {
            return false;
        }
        return (inventoryItem.getItemMeta().isUnbreakable() == is.getItemMeta().isUnbreakable());
    }

    public void registerDateCheckTask() {
        if(dateCheckTask != -1) {
            plugin.getServer().getScheduler().cancelTask(dateCheckTask);
        }
        dateCheckTask = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, this::checkTags, 0L, 20L);
    }

    public void checkTags() {
        String newTag = DateManager.getFormattedDate("%Y/%M/%D");
        boolean updateTags = false;
        for(QuestSchema schema : questSchemas) {
            if(newTag.equals(schema.getDateTag())) {
                continue;
            }
            boolean changedTag = false;
            if(QuestInterval.DAY.equals(schema.getQuestInterval())) {
                schema.setDateTag(newTag);
                changedTag = true;
            } else if(QuestInterval.MONTH.equals(schema.getQuestInterval())) {
                if(newTag.equals(DateManager.getFormattedDate("%Y/%M/1"))) {
                    schema.setDateTag(newTag);
                    changedTag = true;
                } else if(schema.getDateTag() == null) {
                    schema.setDateTag(DateManager.getFormattedDate("%Y/%M/1"));
                    changedTag = true;
                } else {
                    try {
                        int currentMonth = Integer.parseInt(DateManager.getFormattedDate("%M"));
                        int currentYear = Integer.parseInt(DateManager.getFormattedDate("%Y"));
                        String[] split = schema.getDateTag().split("/");
                        int lastRandomizedMonth = Integer.parseInt(split[1]);
                        int lastRandomizedYear = Integer.parseInt(split[0]);
                        if(currentMonth > lastRandomizedMonth && currentYear >= lastRandomizedYear) {
                            schema.setDateTag(DateManager.getFormattedDate("%Y/%M/1"));
                            changedTag = true;
                        }
                    } catch(NumberFormatException | IndexOutOfBoundsException ignored) {}
                }
            } else {
                if(DateManager.getDayName().equalsIgnoreCase(schema.getQuestInterval().name())) {
                    schema.setDateTag(newTag);
                    changedTag = true;
                } else if(schema.getDateTag() == null) {
                    schema.setDateTag(newTag);
                    changedTag = true;
                }
            }
            if(changedTag) {
                schema.setTagID(schema.getTagID() + 1);
                plugin.getDataHandler().saveSchemaTags(schema);
                updateTags = true;
            }
        }
        if(updateTags) {
            updateAllTags();
        }
    }

    public void updateAllTags() {
        for(String player : quests.keySet()) {
            for(Quest quest : quests.get(player)) {
                quest.updateTagID();
            }
        }
    }

}
