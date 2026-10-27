package de.heldenmod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CreativeModeTabs;

public class HeldenMod implements ModInitializer {
    public static final String MOD_ID = "heldenmod";

    public static Identifier id(String path) { return Identifier.fromNamespaceAndPath(MOD_ID, path); }

    @Override
    public void onInitialize() {
        ModItems.init();

        PayloadTypeRegistry.playC2S().register(AbilityPayload.TYPE, AbilityPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(AbilityPayload.TYPE, (payload, ctx) ->
                ctx.server().execute(() -> Abilities.use(ctx.player(), payload.slot())));

        ServerTickEvents.END_SERVER_TICK.register(Abilities::tick);

        AttackEntityCallback.EVENT.register((player, level, hand, entity, hit) -> {
            if (level instanceof ServerLevel sl && player instanceof ServerPlayer sp && entity instanceof LivingEntity t)
                Abilities.weaponHit(sl, sp, t);
            return InteractionResult.PASS;
        });

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.COMBAT).register(entries -> {
            for (Suit s : Suit.values()) {
                for (var item : ModItems.ARMOR.get(s).values()) entries.accept(item);
                entries.accept(ModItems.WEAPON.get(s));
            }
        });
    }
}
