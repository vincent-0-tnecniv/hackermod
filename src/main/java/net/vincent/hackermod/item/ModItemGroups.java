package net.vincent.hackermod.item;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.vincent.hackermod.HackerMod;

public class ModItemGroups {

    // Order of adding the item groups does NOT matter

    public static final ItemGroup MOD_ITEMS_GROUP = createModItemGroups(
            "mod_items", ModItems.HACKER_HAND,
            "itemgroup.tutorialmod.mod_items"
    );

    /*
    public static final ItemGroup MOD_ITEMS_GROUP = Registry.register(Registries.ITEM_GROUP,
            Identifier.of(TutorialMod.MOD_ID, "mod_items"),
            FabricItemGroup.builder().icon(() -> new ItemStack(ModItems.PINK_GARNET))
                    .displayName(Text.translatable("itemgroup.tutorialmod.mod_items"))
                    .entries((displayContext, entries) -> {
                        entries.add(ModItems.PINK_GARNET);
                        entries.add(ModItems.RAW_PINK_GARNET);
                    }).build());
    */


    public static void registerModItemGroups() {
        HackerMod.LOGGER.info("Registering Item Groups for " + HackerMod.MOD_ID);

        // Order of adding the items does NOT matter

        addItemToModItemGroup(MOD_ITEMS_GROUP, ModItems.HACKER_HAND);
    }

    // The following are all helper methods

    // The following three are overloaded methods, serving same functions
    // They are used to create an item group
    // P.S. Item groups are the Forge equivalent of creative mode tabs
    // It has the item group ID pID, an icon pIcon
    //        the key for translating in en_us.json using pKey
    // For pIcon, either use the data types Item or Block
    // otherwise, a pre-conversation to ItemStack is needed

    public static ItemGroup createModItemGroups(String pID, Item pIcon,
                                                String pKey) {
        return Registry.register(Registries.ITEM_GROUP,
                Identifier.of(HackerMod.MOD_ID, pID),
                FabricItemGroup.builder().icon(() -> new ItemStack(pIcon))
                        .displayName(Text.translatable(pKey))
                        .entries((displayContext, entries) -> {})
                        .build());
    }

    public static ItemGroup createModItemGroups(String pID, Block pIcon,
                                                String pKey) {
        return Registry.register(Registries.ITEM_GROUP,
                Identifier.of(HackerMod.MOD_ID, pID),
                FabricItemGroup.builder().icon(() -> new ItemStack(pIcon))
                        .displayName(Text.translatable(pKey))
                        .entries((displayContext, entries) -> {})
                        .build());
    }

    public static ItemGroup createModItemGroups(String pID, ItemStack pIcon,
                                                String pKey) {
        return Registry.register(Registries.ITEM_GROUP,
                Identifier.of(HackerMod.MOD_ID, pID),
                FabricItemGroup.builder().icon(() -> pIcon)
                        .displayName(Text.translatable(pKey))
                        .entries((displayContext, entries) -> {})
                        .build());
    }

    // The following three are overloaded methods, serving same functions
    // They are used to add an "item" pItem to an item group pGroup
    // This "item" is the actual item held by the entities and their inventories
    // For pItem, either use the data types Item or Block
    // otherwise, a pre-conversation to ItemStack is needed


    public static void addItemToModItemGroup(ItemGroup pGroup, Item pItem) {
        RegistryKey<ItemGroup> modItemGroupRegistryKey = RegistryKey.of(
                Registries.ITEM_GROUP.getKey(), Registries.ITEM_GROUP.getId(pGroup));

        ItemGroupEvents.modifyEntriesEvent(modItemGroupRegistryKey).register(
                entries -> {
                    entries.add(pItem);
                });
    }

    public static void addItemToModItemGroup(ItemGroup pGroup, Block pBlock) {
        RegistryKey<ItemGroup> modItemGroupRegistryKey = RegistryKey.of(
                Registries.ITEM_GROUP.getKey(), Registries.ITEM_GROUP.getId(pGroup));

        ItemGroupEvents.modifyEntriesEvent(modItemGroupRegistryKey).register(
                entries -> {
                    entries.add(pBlock);
                });
    }

    public static void addItemToModItemGroup(ItemGroup pGroup, ItemStack pItemStack) {
        RegistryKey<ItemGroup> modItemGroupRegistryKey = RegistryKey.of(
                Registries.ITEM_GROUP.getKey(), Registries.ITEM_GROUP.getId(pGroup));

        ItemGroupEvents.modifyEntriesEvent(modItemGroupRegistryKey).register(
                entries -> {
                    entries.add(pItemStack);
                });
    }

}