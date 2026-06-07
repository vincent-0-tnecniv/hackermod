package net.vincent.hackermod.item;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.vincent.hackermod.HackerMod;

public class ModItems {
    public static final Item HACKER_HAND = registerItem("hacker_hand",
            new HackerHandItem(new Item.Settings().maxCount(1)));


    private static Item registerItem(String pID, Item pItem) {
        // To be used to register any item with the ID pID and its item properties
        // in the Item format in pItem
        return Registry.register(Registries.ITEM, Identifier.of(HackerMod.MOD_ID, pID), pItem);
    }

    public static void registerModItems() {
        HackerMod.LOGGER.info("Registering Mod Items for " + HackerMod.MOD_ID);
    }
}
