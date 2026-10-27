package de.heldenmod;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record AbilityPayload(int slot) implements CustomPacketPayload {
    public static final Type<AbilityPayload> TYPE = new Type<>(HeldenMod.id("ability"));
    public static final StreamCodec<RegistryFriendlyByteBuf, AbilityPayload> CODEC =
            StreamCodec.composite(ByteBufCodecs.VAR_INT, AbilityPayload::slot, AbilityPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
