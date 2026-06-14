package net.vincent.hackermod.item;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.vincent.hackermod.screen.HackerHandBlockScreen;
import net.vincent.hackermod.screen.HackerHandEntityNBTScreen;

public class HackerHandItem extends Item {

    private static final double MAX_RANGE = 500.0;

    public HackerHandItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        if (world.isClient) {
            // Check for entity first
            Entity targetEntity = getTargetEntity(player, MAX_RANGE);

            if (targetEntity != null && !player.isSneaking()) {
                // Standing and clicking on entity - open NBT editor
                MinecraftClient.getInstance().setScreen(new HackerHandEntityNBTScreen(targetEntity));
                return TypedActionResult.success(player.getStackInHand(hand));
            } else if(targetEntity != null && player.isSneaking()){
                // TODO: Sneaking and clicking on entity - open attribute editor
                // MinecraftClient.getInstance().setScreen(new HackerHandEntityAttributeScreen(targetEntity, world));
                return TypedActionResult.success(player.getStackInHand(hand));
            } else{
                // No entity in front - open block editor
                HitResult hit = player.raycast(MAX_RANGE, 1.0F, false);
                if (hit.getType() == HitResult.Type.BLOCK) {
                    BlockHitResult blockHit = (BlockHitResult) hit;
                    MinecraftClient.getInstance().setScreen(
                            new HackerHandBlockScreen(blockHit.getBlockPos(), world.getBlockState(blockHit.getBlockPos()))
                    );
                    return TypedActionResult.success(player.getStackInHand(hand));
                }
            }
        }
        return TypedActionResult.pass(player.getStackInHand(hand));
    }

    private Entity getTargetEntity(PlayerEntity player, double range) {
        // Raycast for entities
        Vec3d cameraPos = player.getCameraPosVec(1.0F);
        Vec3d rotation = player.getRotationVec(1.0F);
        Vec3d endPos = cameraPos.add(rotation.x * range, rotation.y * range, rotation.z * range);

        // Create a bounding box along the ray
        Box searchBox = player.getBoundingBox().stretch(rotation.multiply(range)).expand(2.0);

        Entity closestEntity = null;
        double closestDistance = range;

        for (Entity entity : player.getWorld().getOtherEntities(player, searchBox,
                e -> !e.isSpectator() && e.isAlive())) {
            Box entityBox = entity.getBoundingBox().expand(0.3);

            // Handle Optional correctly
            java.util.Optional<Vec3d> intersectionOpt = entityBox.raycast(cameraPos, endPos);

            if (intersectionOpt.isPresent()) {
                Vec3d intersection = intersectionOpt.get();
                double distance = cameraPos.distanceTo(intersection);
                if (distance < closestDistance) {
                    closestEntity = entity;
                    closestDistance = distance;
                }
            }
        }

        return closestEntity;
    }
}