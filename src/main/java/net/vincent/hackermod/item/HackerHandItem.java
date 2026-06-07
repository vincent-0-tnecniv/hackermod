package net.vincent.hackermod.item;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.vincent.hackermod.screen.HackerHandScreen;

public class HackerHandItem extends Item {
    public HackerHandItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        BlockState state = world.getBlockState(pos);
        PlayerEntity player = context.getPlayer();

        // Don't open for block entities
        if (world.getBlockEntity(pos) != null) {
            if (player != null) {
                player.sendMessage(Text.literal("§cBlock entities not supported yet!"), true);
            }
            return ActionResult.FAIL;
        }

        if (world.isClient && player != null) {
            // Open screen WITHOUT passing ServerWorld
            MinecraftClient.getInstance().setScreen(new HackerHandScreen(pos, state));
            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }
}