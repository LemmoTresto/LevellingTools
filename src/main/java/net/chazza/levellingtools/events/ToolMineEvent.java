package net.chazza.levellingtools.events;

import de.tr7zw.itemnbtapi.NBTItem;
import de.tr7zw.itemnbtapi.utils.MinecraftVersion;
import net.chazza.levellingtools.LevellingTools;
import net.chazza.levellingtools.entity.UserEntity;
import net.chazza.levellingtools.tool.LevellingTool;
import net.chazza.levellingtools.util.MongoDB;
import net.chazza.levellingtools.util.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

public class ToolMineEvent implements Listener {

    LevellingTools levellingTools;
    public ToolMineEvent(LevellingTools levellingTools) {
        this.levellingTools = levellingTools;
        Bukkit.getPluginManager().registerEvents(this, levellingTools);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Player player = e.getPlayer();
        Block block = e.getBlock();
        ItemStack item = player.getItemInHand();

        MinecraftVersion.setLogging(false);
        NBTItem nbtItem = new NBTItem(item);

        /*
        TODO:
        - Add XP System
        - Create Multiplier System (Perm based, with Redeemable Additions)
         */

        if(nbtItem.hasKey("omnitool")) {
            UserEntity user = UserEntity.getUser(player);
            LevellingTool playerTool = LevellingTool.getTools().get(user.getLevel());
            //player.sendMessage(ChatColor.YELLOW + "You used your level " + user.getLevel() + " pickaxe to mine " + e.getBlock().getType().name());

            int xpFained = playerTool.getXpFromBlock(block);

            StringUtil.sendActionbar(player, StringUtil.translate("&6&l+" + xpFained + " EXP"));
            int currentLvl = user.getLevel();

            for(LevellingTool tool : LevellingTool.getTools().values()) {
                if(tool.getLevel() == 1) continue;
                if(tool.getLevel() <= user.getLevel()) continue;
                if(user.getExperience()+xpFained < tool.getXpRequired()) continue;

                currentLvl++;
                tool.executeCommands(player);
            }

            user.setLevel(currentLvl);
            user.setExperience(user.getExperience() + xpFained);
            user.setBlocksBroken(user.getBlocksBroken()+1);
            MongoDB.getDatabase().save(user);
            player.setItemInHand(LevellingTool.getItemStack(player, block));

            //UUID toolOwner = UUID.fromString(nbtItem.getString("omnitool"));
        }
    }
}