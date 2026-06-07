package net.vincent.hackermod.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.vincent.hackermod.screen.HackerHandScreen;

public class HackerHandItem extends Item {
    public HackerHandItem(Settings settings) {
        super(settings);
    }

    // In HackerHandItem.java
    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        PlayerEntity player = context.getPlayer();
        return ActionResult.PASS;
    }
}