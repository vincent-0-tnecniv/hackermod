package net.vincent.hackermod.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.block.Block;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.data.server.recipe.ShapelessRecipeJsonBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.Items;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import net.vincent.hackermod.HackerMod;
import net.vincent.hackermod.item.ModItems;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends FabricRecipeProvider {
    public ModRecipeProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    public void generate(RecipeExporter exporter) {

//        List<ItemConvertible> PINK_GARNET_SMELTABLES = List.of(ModItems.PINK_GARNET, ModBlocks.PINK_GARNET_ORE,
//                ModBlocks.PINK_GARNET_DEEPSLATE_ORE);
        ShapedRecipeJsonBuilder.create(RecipeCategory.TOOLS, ModItems.HACKER_HAND)
                .pattern("R R")
                .pattern("RRR")
                .pattern(" R ")
                .input('R', Items.DEBUG_STICK)
                .criterion(hasItem(Items.DEBUG_STICK), conditionsFromItem(Items.DEBUG_STICK))
                .offerTo(exporter);


    }
}