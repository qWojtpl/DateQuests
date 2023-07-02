package pl.datequests.gui.list;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import pl.datequests.gui.PluginGUI;
import pl.datequests.quests.Quest;

import java.util.ArrayList;
import java.util.List;

public class AdminPanel extends PluginGUI {

    private final String lookupPlayer;
    private int pageID = 0;
    private int currentOffset = 0;
    private final List<Integer> slots = new ArrayList<>();
    private final List<Integer> interactableSlots = new ArrayList<>();

    public AdminPanel(Player owner, String inventoryName, String lookupPlayer) {
        super(owner, inventoryName, 54);
        this.lookupPlayer = lookupPlayer;
        onLoad();
    }

    public void onLoad() {
        setGUIProtected(true);
        fillWith(Material.BLACK_STAINED_GLASS_PANE);
        pageID = 0;
        setSlot(21, Material.OAK_SIGN, getMessages().getMessage("allPlayersQuests"),
                getLore(getMessages().getMessage("allPlayersQuestsLore")));
        setSlot(23, Material.CHEST, getMessages().getMessage("playerRewards"),
                getLore(getMessages().getMessage("playerRewardsLore")));
    }

    @Override
    public void onClick(int slot) {
        if(pageID == 0) {
            if (slot == 21) {
                loadQuestsPage();
            } else if (slot == 23) {
                loadRewardsPage();
            }
        } else if(slot == 46) {
            previousPage();
        } else if(slot == 52) {
            nextPage();
        }
    }

    public void loadQuestsPage() {
        pageID = 1;
        loadFrame();
        loadQuests();
    }

    public void loadQuests() {
        List<Quest> playerQuests = getQuestsManager().getPlayersQuests(lookupPlayer);
        int i = 0;

    }

    public void loadRewardsPage() {
        pageID = 2;
        loadFrame();
        loadRewards();
    }

    public void loadRewards() {

    }

    private void loadFrame() {
        fillWith(Material.BLACK_STAINED_GLASS_PANE);
        slots.clear();
        interactableSlots.clear();
        currentOffset = 0;
        int[] protectedSlots = new int[]{17, 18, 26, 27, 35, 36, 44};
        for(int i = 10; i < 45; i++) {
            boolean isProtected = false;
            for(int slot : protectedSlots) {
                if(i == slot) {
                    isProtected = true;
                    break;
                }
            }
            if(!isProtected) {
                slots.add(i);
                setSlot(i, Material.WHITE_STAINED_GLASS_PANE, "", getLore(""));
            }
        }
        setSlot(46, Material.ARROW, getMessages().getMessage("previousPage"),
                getLore(getMessages().getMessage("previousPageLore")));
        setSlot(52, Material.ARROW, getMessages().getMessage("nextPage"),
                getLore(getMessages().getMessage("nextPageLore")));
    }

    private void nextPage() {
        if(getQuestsManager().getPlayersQuests(getOwner().getName()).size() - 1 < currentOffset + 36) {
            getOwner().playSound(getOwner(), Sound.ENTITY_VILLAGER_NO, 1.0F, 1.5F);
            return;
        }
        getOwner().playSound(getOwner(), Sound.ITEM_BOOK_PAGE_TURN, 1.0F, 1.0F);
        currentOffset += 36;
        loadQuests();
    }

    private void previousPage() {
        if(currentOffset == 0) {
            getOwner().playSound(getOwner(), Sound.ENTITY_VILLAGER_NO, 1.0F, 1.5F);
            return;
        }
        getOwner().playSound(getOwner(), Sound.ITEM_BOOK_PAGE_TURN, 1.0F, 1.0F);
        currentOffset -= 36;
        loadQuests();
    }

}
