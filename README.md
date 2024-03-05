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

# Configuration

`%nl% - new line`<br>

<details><summary>config.yml</summary>



</details>

# Commands & Permissions

`/dq`                                - Shows available quest categories<br> 
`/dq reload [--skipsave]`            - Reloads configuration and saves data. You can skip data save by using /dq reload --skipsave `dq.reload`<br> 
`/dq load <player>`                  - Loads player `dq.load`<br>
`/dq lookup <player>`                - Lookup information about player (you can here manage player's quests and rewards) `dq.lookup` `dq.lookup.switchcomplete` `dq.lookup.removereward` `dq.lookup.changeevent`<br>
`/dq save`                           - Saves all data `dq.save`<br>
`/dq serialize`                      - Get serialization of item in your hand `dq.serialize`<br>
