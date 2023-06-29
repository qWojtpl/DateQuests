package pl.datequests.quests;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import pl.datequests.DateQuests;
import pl.datequests.gui.SortType;
import pl.datequests.util.DateManager;
import pl.datequests.util.PlayerUtil;
import pl.datequests.util.RandomNumber;

import javax.annotation.Nullable;
import java.util.*;

@Getter
public class QuestsManager {

    private final DateQuests plugin = DateQuests.getInstance();
    private final List<QuestSchema> questSchemas = new ArrayList<>();
    private final HashMap<String, List<Quest>> quests = new HashMap<>();
    private final HashMap<String, List<ItemStack>> rewards = new HashMap<>();
    @Setter
    private List<ItemStack> rewardForAll = new ArrayList<>();
    @Setter
    private RewardType rewardForAllType;
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
                p.sendMessage(" §eQuest completed: §2" + questSchema.getSchemaName());
                p.sendMessage(" §2" + quest.getEvent());
                p.sendMessage(" §eProgress: §f" + quest.getProgress() + "§6/§2" + quest.getRequiredProgress());
                if(questSchema.getRewards().size() != 0) {
                    if(questSchema.getRewardType().equals(RewardType.ALL)) {
                        for(ItemStack is : questSchema.getRewards()) {
                            assignReward(player, is);
                        }
                    } else if(questSchema.getRewardType().equals(RewardType.RANDOM)) {
                        assignReward(player,
                                questSchema.getRewards().get(RandomNumber.randomInt(0, questSchema.getRewards().size() - 1)));
                    }
                    p.sendMessage(" §aYou have new assigned reward");
                }
                if(isCompletedAllQuests(player) && rewardForAll.size() != 0) {
                    p.sendMessage(" §a§lYou have completed all quests in month. There's special reward waiting for you!");
                    assignMonthReward(player);
                }
                p.sendMessage(" ");
                p.sendMessage("§6{========================}");
            } else {
                PlayerUtil.sendActionBarMessage(p, "§aYou scored in quest " + quest.getQuestSchema().getSchemaName());
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

    public void assignMonthReward(String player) {
        DateManager dateManager = plugin.getDateManager();
        if(rewardForAllType.equals(RewardType.ALL)) {
            for(ItemStack is : rewardForAll) {
                ItemMeta im = is.getItemMeta();
                if(im != null) {
                    im.setDisplayName(im.getDisplayName()
                            .replace("%year%", dateManager.getFormattedDate("%Y"))
                            .replace("%month%", dateManager.getFormattedDate("%M")));
                    is.setItemMeta(im);
                    if(im.getLore() != null) {
                        List<String> lore = im.getLore();
                        List<String> newLore = new ArrayList<>();
                        for(String line : lore) {
                            newLore.add(line
                                    .replace("%year%", dateManager.getFormattedDate("%Y"))
                                    .replace("%month%", dateManager.getFormattedDate("%M")));
                        }
                        im.setLore(newLore);
                    }
                }
                assignReward(player, is);
            }
        } else if(rewardForAllType.equals(RewardType.RANDOM)) {
            if(rewardForAll.size() == 0) {
                return;
            }
            ItemStack is = rewardForAll.get(RandomNumber.randomInt(0, rewardForAll.size() - 1));
            ItemMeta im = is.getItemMeta();
            if(im != null) {
                im.setDisplayName(im.getDisplayName()
                        .replace("%year%", dateManager.getFormattedDate("%Y"))
                        .replace("%month%", dateManager.getFormattedDate("%M")));
                if(im.getLore() != null) {
                    List<String> lore = im.getLore();
                    List<String> newLore = new ArrayList<>();
                    for(String line : lore) {
                        newLore.add(line
                                .replace("%year%", dateManager.getFormattedDate("%Y"))
                                .replace("%month%", dateManager.getFormattedDate("%M")));
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
                p, "§eAccepted quest: §6" + schema.getSchemaName(),
                "§6" + q.getEvent(),
                10,
                100,
                10
        );
        p.sendMessage("§6{========================}");
        p.sendMessage(" ");
        p.sendMessage(" §eAccepted quest: §2" + schema.getSchemaName());
        p.sendMessage(" §2" + q.getEvent());
        p.sendMessage(" §eProgress: §f0§6/§2" + q.getRequiredProgress());
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

    public boolean isCompletedAllQuests(String player) {
        DateManager dateManager = plugin.getDateManager();
        if(!dateManager.getFormattedDate("%Y/%M/%D").equals(dateManager.getFormattedDate("%Y/%M/" + dateManager.getDaysOfMonth()))) {
            return false;
        }
        for(QuestSchema schema : questSchemas) {
            String month = plugin.getDateManager().getFormattedDate("%Y/%M");
            if(!schema.getMonthTags().containsKey(month)) {
                continue;
            }
            List<Integer> monthTags = schema.getMonthTags().get(month);
            List<Quest> playerQuests = getPlayersQuestsBySchema(player, schema);
            for(int tag : monthTags) {
                boolean found = false;
                for(Quest q : playerQuests) {
                    if(q.getTagID() == tag && q.getQuestState().equals(QuestState.COMPLETED)) {
                        found = true;
                        break;
                    }
                }
                if(!found) {
                    return false;
                }
            }
        }
        return true;
    }

    public List<ItemStack> getPlayersRewards(String player) {
        if(rewards.containsKey(player)) {
            return rewards.get(player);
        }
        return new ArrayList<>();
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

    public void changeActiveQuestEvent(String player, QuestSchema schema) {
        Player p = PlayerUtil.getPlayer(player);
        if(p == null) {
            return;
        }
        if(!schema.isChangeable()) {
            p.sendMessage("You can't change this quest.");
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
            p.sendMessage("You don't have any active quest in this category.");
            return;
        }
        if(quest.isChanged()) {
            p.sendMessage("You've already changed this quest!");
            return;
        }
        List<ItemStack> items = new ArrayList<>();
        items.add(schema.getChangeQuestItem());
        List<Integer> itemSlots = getItemSlots(p, items);
        if(itemSlots.size() == 0) {
            p.sendMessage("You don't have required items to change quest!");
            return;
        }
        takeItems(p, itemSlots, items);
        quest.randomizeEvent();
        quest.setChanged(true);
        quest.save();
        PlayerUtil.sendTitle(p,
                "§eChanged quest: §6" + schema.getSchemaName(),
                "§6" + quest.getEvent(),
                10,
                100,
                10);
        p.sendMessage("§6{========================}");
        p.sendMessage(" ");
        p.sendMessage(" §eChanged quest: §2" + schema.getSchemaName());
        p.sendMessage(" §2" + quest.getEvent());
        p.sendMessage(" §eProgress: §f0§6/§2" + quest.getRequiredProgress());
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
                } else if(schema.getDateTag() == null) {
                    schema.setDateTag(plugin.getDateManager().getFormattedDate("%Y/%M/1"));
                    changedTag = true;
                } else {
                    try {
                        int currentMonth = Integer.parseInt(plugin.getDateManager().getFormattedDate("%M"));
                        int currentYear = Integer.parseInt(plugin.getDateManager().getFormattedDate("%Y"));
                        String[] split = schema.getDateTag().split("/");
                        int lastRandomizedMonth = Integer.parseInt(split[1]);
                        int lastRandomizedYear = Integer.parseInt(split[0]);
                        if(currentMonth > lastRandomizedMonth && currentYear >= lastRandomizedYear) {
                            schema.setDateTag(plugin.getDateManager().getFormattedDate("%Y/%M/1"));
                            changedTag = true;
                        }
                    } catch(NumberFormatException | IndexOutOfBoundsException ignored) {}
                }
            } else {
                if(plugin.getDateManager().getDayName().equalsIgnoreCase(schema.getQuestInterval().name())) {
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
            }
        }
    }

}
