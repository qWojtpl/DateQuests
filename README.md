<p align="center">
  <img src="images/logo.png">
</p>

<br>

# DateQuests

<p>Add date quests to your Minecraft server.</p>
<p>Tested minecraft versions: </p> 

`1.19.3`

# Installation

<p>Put DateQuests.jar to your plugins folder and restart the server.</p>
<p>Put BeaverLib.jar from <a href="https://github.com/qWojtpl/BeaverLib">BeaverLib repository</a>, this library is required!</p>

# Configuration

`%nl% - new line`<br>

<details><summary>config.yml</summary>

## Config

`loadAllPlayers` - When set to true plugin on startup will load all players from data.yml. When set to false player data will be downloaded when player will join the server. May affect performance (depends on player count saved in data.yml)

<hr>

`saveInterval` - Specify save interval in seconds. In default, every 120 seconds all data will be saved to data.yml

<hr>

`loadLeaderboard` - When set to true plugin will load leaderboard from data.yml. Leaderboard will work only with PAPI (PlaceholderAPI).
  - %datequests_top_<number>% - iteration starts from 0, you can have as many top requests as you want
  - %datequests_playertop% - returns player's completed quests count and their place in leaderboard if available

<hr>

`leaderboardMaxRecords` - If you have 120 players saved in data.yml, but you don't want to load these all players into leaderboard, you can set leaderboardMaxRecords. In default, only 10 players will be loaded.

<hr>

`leaderboardUpdateInterval` - Leaderboard is not updating every tick. This could cause performance issues. In default, leaderboard will update every 30 seconds.

## NPC

`name` - This requires Citizens plugin. If npc with this name will be clicked, then DateQuests GUI will be opened to player. Player can deliver quests' items to this NPC.

## Permissions

`reload` - Reload plugin

<hr>

`lookup` - Get players' quests information

<hr>

`lookupSwitchComplete` - Switch other players' quests as completed

<hr>

`lookupRemoveReward` - Remove other players' rewards

<hr>

`lookupChangeEvent` - Change other players' quest event

<hr>

`save` - Save all data to file

<hr>

`loadPlayer` - Load player from memory if not loaded

<hr>

`serialize` - Serialize item in your hand and print it to data.yml. It can be helpful if you have eg. head and you want to put it into daily/monthly rewards.

## Quests

```yml
quests:
  day: <--- quests identifier
    icon: <--- icon for this quests type in GUI
      material: GRASS_BLOCK
      amount: 1
      name: "&aEasy quests"
    interval: DAY <--- how often you can take this quest, it can be DAY (everyday), MONTH (first day of month), or every week day (eg. MONDAY, SUNDAY)
    questGroups: <--- quest groups, you can't get the same group for two quests in the same type in a row
      0: <--- group 0
        0: <--- first quest in group 0
          event: break %random% stone <--- event for completing this quest, use %random% for random number. Possible events are break, kill, deliver
          range: 1-10 <--- range for random number
        1:
          event: break %random% dirt
          range: 10-15
      1:
        0:
          event: kill %random% zombie
          range: 5-10
        1:
          event: kill %random% cow
          range: 5-10
    changeable: true <--- when set to true, you can change this quest once
    changeQuestItem: <--- item required to change quest, you can give it name, lore etc.
      material: DIAMOND
      amount: 1
    rewards: <--- rewards for completing this quests
      # Available reward types:
      #   RANDOM - gives random rewards from the list below
      #   ALL - gives all rewards from the list below
      rewardType: RANDOM
      items:
        0:
          material: DIAMOND
          amount: 1
```

Special reward will be assigned to player when player will complete minimumCompletePercentage percent of quests in all categories. Player must complete at least one quest in last day of a month.

```yml
specialReward:
  minimumCompletedPercentage: 90
  rewardType: ALL
  items:
    0:
      material: DIAMOND
      name: "%year% %month% diamond"
      amount: 64
```

</details>

# Commands

`/dq`                                - Shows available quest categories<br> 
`/dq reload [--skipsave]`            - Reloads configuration and saves data. You can skip data save by using /dq reload --skipsave<br> 
`/dq load <player>`                  - Loads player<br>
`/dq lookup <player>`                - Lookup information about player (you can here manage player's quests and rewards)<br>
`/dq save`                           - Saves all data<br>
`/dq serialize`                      - Get serialization of item in your hand<br>
