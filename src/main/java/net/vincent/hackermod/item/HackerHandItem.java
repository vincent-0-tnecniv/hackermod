package net.vincent.hackermod.item;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;
import net.vincent.hackermod.screen.HackerHandBlockScreen;
import net.vincent.hackermod.screen.HackerHandSummonScreen;

public class HackerHandItem extends Item {

    private static final double MAX_RANGE = 500.0;

    public HackerHandItem(Settings settings) {
        super(settings);
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        return super.postHit(stack, target, attacker);
    }

    // In your HackerHandItem.java
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        if (world.isClient) {
            BlockHitResult hit = (BlockHitResult) player.raycast(MAX_RANGE, 1.0F, true);
            if (player.isSneaking()) {
                MinecraftClient.getInstance().setScreen(
                        new HackerHandSummonScreen(hit.getBlockPos())
                );
                return TypedActionResult.success(player.getStackInHand(hand));
            } else {
                MinecraftClient.getInstance().setScreen(
                        new HackerHandBlockScreen(hit.getBlockPos(), world.getBlockState(hit.getBlockPos()))
                );
                return TypedActionResult.success(player.getStackInHand(hand));
            }
        }
        return TypedActionResult.pass(player.getStackInHand(hand));
    }
}