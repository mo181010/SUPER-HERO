package de.heldenmod;

import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAssets;

public class ModItems {
    public static final ArmorType[] PIECES = {ArmorType.HELMET, ArmorType.CHESTPLATE, ArmorType.LEGGINGS, ArmorType.BOOTS};
    public static final Map<Suit, Map<ArmorType, Item>> ARMOR = new EnumMap<>(Suit.class);
    public static final Map<Suit, Item> WEAPON = new EnumMap<>(Suit.class);

    private static Item register(String name, Item.Properties props) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, HeldenMod.id(name));
        return Registry.register(BuiltInRegistries.ITEM, key, new Item(props.setId(key)));
    }

    public static void init() {
        for (Suit suit : Suit.values()) {
            ArmorMaterial mat = new ArmorMaterial(
                    37,
                    Map.of(ArmorType.BOOTS, 3, ArmorType.LEGGINGS, 6, ArmorType.CHESTPLATE, 8,
                           ArmorType.HELMET, 3, ArmorType.BODY, 11),
                    15, SoundEvents.ARMOR_EQUIP_NETHERITE, 3.0f, 0.1f,
                    ItemTags.REPAIRS_NETHERITE_ARMOR,
                    ResourceKey.create(EquipmentAssets.ROOT_ID, HeldenMod.id(suit.id)));

            Map<ArmorType, Item> set = new EnumMap<>(ArmorType.class);
            for (ArmorType t : PIECES) {
                set.put(t, register(suit.id + "_" + t.getName(),
                        new Item.Properties().humanoidArmor(mat, t).rarity(Rarity.EPIC)));
            }
            ARMOR.put(suit, set);

            WEAPON.put(suit, register(suit.weaponId(),
                    new Item.Properties().sword(ToolMaterial.NETHERITE, 4.0f, -2.2f)
                            .rarity(Rarity.EPIC)
                            .component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)));
        }
    }
}
