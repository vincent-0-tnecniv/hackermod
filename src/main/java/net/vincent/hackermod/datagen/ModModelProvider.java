package net.vincent.hackermod.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.block.Block;
import net.minecraft.data.client.*;
import net.minecraft.item.Item;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.Identifier;
import net.vincent.hackermod.item.ModItems;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class ModModelProvider extends FabricModelProvider {

    public ModModelProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator blockStateModelGenerator) {
        // no mod blocks here lol
    }

    @Override
    public void generateItemModels(ItemModelGenerator itemModelGenerator) {
        // If a model predicate is used, set a comment on the item
        // i.e. cannot use data gen
        // When an item has a model predicate, its texture can change based on a set criteria
        // e.g. Coral Chunk in Cataclysm, it changes texture based on its stack size

        itemModelGenerator.register(ModItems.HACKER_HAND, Models.GENERATED);
        // for the hand item

        // itemModelGenerator.registerArmor(((ArmorItem) ModItems.PINK_GARNET_HELMET));
        // for the goggles item
    }

    // The following are all helper methods

    public void generateEGG(ItemModelGenerator itemModelGenerator, Item spawnEgg) {
        itemModelGenerator.register(spawnEgg,
                new Model(Optional.of(Identifier.of("item/template_spawn_egg")), Optional.empty()));
    }

    public void generateBlockSetStateModels(BlockStateModelGenerator pBlockStateModelGenerator, Block pBlock,
                                            @Nullable Block pStairs, @Nullable Block pSlab, @Nullable Block pButton,
                                            @Nullable Block pPressurePlate, @Nullable Block pFence, @Nullable Block pFenceGate,
                                            @Nullable Block pWall) {
        // This method generates the whole SET of blocks.
        // The set here includes stairs, slabs, buttons, pressure plates, fences, fence gates, walls AND the block itself
        // Note: if the BASE block is used in this method, do NOT generate it via any other sources!!
        // Otherwise the code may crash!
        BlockStateModelGenerator.BlockTexturePool blockPool = pBlockStateModelGenerator.registerCubeAllModelTexturePool(pBlock);

        if(pStairs != null) {
            blockPool.stairs(pStairs);
        }
        if(pSlab != null) {
            blockPool.slab(pSlab);
        }
        if(pButton != null) {
            blockPool.button(pButton);
        }
        if(pPressurePlate != null) {
            blockPool.pressurePlate(pPressurePlate);
        }
        if(pFence != null) {
            blockPool.fence(pFence);
        }
        if(pFenceGate != null) {
            blockPool.fenceGate(pFenceGate);
        }
        if(pWall != null) {
            blockPool.wall(pWall);
        }
    }

    public void generateBooleanPropertiesCubeAllBlockModels(BlockStateModelGenerator pBlockStateModelGenerator, Block pBlock, BooleanProperty pBP, String pTrueSuffix) {
        // To be used to generate a block pBlock with a on/off property, e.g. Redstone Lamp
        // The boolean property pBP comes with two json files, one normal (for false)
        // and one adding a suffix pSuffix behind the normal file (for true)
        Identifier falseIdentifier = TexturedModel.CUBE_ALL.upload(pBlock, pBlockStateModelGenerator.modelCollector);
        Identifier trueIdentifier = pBlockStateModelGenerator.createSubModel(pBlock, pTrueSuffix, Models.CUBE_ALL, TextureMap::all);
        pBlockStateModelGenerator.blockStateCollector.accept(VariantsBlockStateSupplier.create(pBlock)
                .coordinate(BlockStateModelGenerator.createBooleanModelMap(pBP, trueIdentifier, falseIdentifier)));
    }
}