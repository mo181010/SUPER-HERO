package de.heldenmod;

import java.util.*;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class Abilities {
    private static final int[] COOLDOWN = {80, 160, 300}; // Ticks
    private static final Map<UUID, long[]> READY = new HashMap<>();

    // ---------- Anzug erkennen ----------
    public static Suit worn(ServerPlayer p) {
        for (Suit s : Suit.values()) {
            if (p.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.ARMOR.get(s).get(ArmorType.HELMET))
             && p.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.ARMOR.get(s).get(ArmorType.CHESTPLATE))
             && p.getItemBySlot(EquipmentSlot.LEGS).is(ModItems.ARMOR.get(s).get(ArmorType.LEGGINGS))
             && p.getItemBySlot(EquipmentSlot.FEET).is(ModItems.ARMOR.get(s).get(ArmorType.BOOTS))) return s;
        }
        return null;
    }

    // ---------- Fähigkeit per Taste ----------
    public static void use(ServerPlayer p, int slot) {
        if (slot < 0 || slot > 2) return;
        Suit suit = worn(p);
        if (suit == null) {
            p.displayClientMessage(Component.literal("Du brauchst einen kompletten Anzug!"), true);
            return;
        }
        ServerLevel l = (ServerLevel) p.level();
        long now = l.getGameTime();
        long[] ready = READY.computeIfAbsent(p.getUUID(), k -> new long[3]);
        if (now < ready[slot]) {
            p.displayClientMessage(Component.literal(suit.abilities[slot] + ": noch "
                    + String.format("%.1f", (ready[slot] - now) / 20.0) + "s"), true);
            return;
        }
        ready[slot] = now + COOLDOWN[slot];
        p.displayClientMessage(Component.literal("§l" + suit.abilities[slot] + "!"), true);

        switch (suit) {
            case VOLTSTRAHL -> volt(l, p, slot);
            case EISWAECHTER -> frost(l, p, slot);
            case SCHATTENFUERST -> shadow(l, p, slot);
            case INFERNUS -> inferno(l, p, slot);
        }
    }

    // ---------- Voltstrahl ----------
    private static void volt(ServerLevel l, ServerPlayer p, int slot) {
        switch (slot) {
            case 0 -> { // Blitzschlag
                LivingEntity t = rayEntity(l, p, 40);
                Vec3 pos = t != null ? t.position() : rayPoint(l, p, 40);
                LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(l, EntitySpawnReason.TRIGGERED);
                if (bolt != null) { bolt.setPos(pos.x, pos.y, pos.z); bolt.setVisualOnly(true); l.addFreshEntity(bolt); }
                for (LivingEntity e : near(l, p, pos, 3.5)) hurt(l, p, e, 12);
                particles(l, ParticleTypes.ELECTRIC_SPARK, pos.add(0, 1, 0), 60, 1.5, 0.3);
                sound(l, pos, SoundEvents.LIGHTNING_BOLT_THUNDER, 1.0f);
            }
            case 1 -> { // Blitzsprint
                Vec3 d = p.getLookAngle();
                beam(l, p.position().add(0, 1, 0), p.position().add(0, 1, 0).add(d.scale(10)), dust(Suit.VOLTSTRAHL));
                p.setDeltaMovement(d.scale(2.4).add(0, 0.15, 0));
                p.hurtMarked = true;
                p.addEffect(new MobEffectInstance(MobEffects.SPEED, 100, 2));
                sound(l, p.position(), SoundEvents.FIRECHARGE_USE, 1.5f);
            }
            case 2 -> { // Stromstoß
                Vec3 c = p.position();
                for (LivingEntity e : near(l, p, c, 7)) {
                    hurt(l, p, e, 8);
                    Vec3 away = e.position().subtract(c);
                    e.knockback(1.5, -away.x, -away.z);
                    e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 3));
                }
                ring(l, ParticleTypes.ELECTRIC_SPARK, c.add(0, 0.3, 0), 7, 120);
                ring(l, dust(Suit.VOLTSTRAHL), c.add(0, 1.0, 0), 4, 60);
                sound(l, c, SoundEvents.LIGHTNING_BOLT_THUNDER, 0.7f);
            }
        }
    }

    // ---------- Eiswächter ----------
    private static void frost(ServerLevel l, ServerPlayer p, int slot) {
        switch (slot) {
            case 0 -> { // Frostlanze
                Vec3 s = p.getEyePosition();
                LivingEntity t = rayEntity(l, p, 30);
                Vec3 end = t != null ? t.position().add(0, t.getBbHeight() / 2, 0) : rayPoint(l, p, 30);
                beam(l, s, end, ParticleTypes.SNOWFLAKE);
                beam(l, s, end, dust(Suit.EISWAECHTER));
                if (t != null) {
                    hurt(l, p, t, 9);
                    t.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 3));
                    t.setTicksFrozen(300);
                    particles(l, ParticleTypes.SNOWFLAKE, end, 40, 0.5, 0.1);
                }
                sound(l, s, SoundEvents.GLASS_BREAK, 1.2f);
            }
            case 1 -> { // Eisschild
                p.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 400, 4));
                p.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 200, 1));
                ring(l, ParticleTypes.SNOWFLAKE, p.position().add(0, 0.2, 0), 1.6, 50);
                ring(l, ParticleTypes.SNOWFLAKE, p.position().add(0, 1.0, 0), 1.6, 50);
                ring(l, ParticleTypes.SNOWFLAKE, p.position().add(0, 1.8, 0), 1.2, 40);
                sound(l, p.position(), SoundEvents.GLASS_BREAK, 0.8f);
            }
            case 2 -> { // Blizzard
                Vec3 c = p.position();
                for (LivingEntity e : near(l, p, c, 9)) {
                    hurt(l, p, e, 5);
                    e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 160, 2));
                    e.setTicksFrozen(400);
                }
                particles(l, ParticleTypes.SNOWFLAKE, c.add(0, 2, 0), 400, 9, 0.05);
                ring(l, dust(Suit.EISWAECHTER), c.add(0, 0.3, 0), 9, 100);
                sound(l, c, SoundEvents.GLASS_BREAK, 1.5f);
            }
        }
    }

    // ---------- Schattenfürst ----------
    private static void shadow(ServerLevel l, ServerPlayer p, int slot) {
        switch (slot) {
            case 0 -> { // Schattenschritt
                Vec3 from = p.position();
                Vec3 target = rayPoint(l, p, 12).subtract(p.getLookAngle().scale(0.6));
                particles(l, ParticleTypes.LARGE_SMOKE, from.add(0, 1, 0), 40, 0.4, 0.05);
                p.connection.teleport(target.x, target.y, target.z, p.getYRot(), p.getXRot());
                particles(l, ParticleTypes.SQUID_INK, target.add(0, 1, 0), 40, 0.4, 0.05);
                for (LivingEntity e : near(l, p, target, 5))
                    e.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
                sound(l, target, SoundEvents.ENDERMAN_TELEPORT, 1f);
            }
            case 1 -> { // Seelenraub
                Vec3 s = p.getEyePosition();
                LivingEntity t = rayEntity(l, p, 20);
                if (t == null) { p.displayClientMessage(Component.literal("Kein Ziel!"), true); return; }
                Vec3 end = t.position().add(0, t.getBbHeight() / 2, 0);
                beam(l, end, s, ParticleTypes.SOUL);
                beam(l, end, s, dust(Suit.SCHATTENFUERST));
                hurt(l, p, t, 8);
                p.heal(6);
                sound(l, end, SoundEvents.WITHER_SHOOT, 0.8f);
            }
            case 2 -> { // Dunkle Welle
                Vec3 c = p.position();
                for (LivingEntity e : near(l, p, c, 8)) {
                    hurt(l, p, e, 4);
                    e.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 1));
                    e.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 120, 0));
                }
                ring(l, ParticleTypes.LARGE_SMOKE, c.add(0, 0.5, 0), 8, 100);
                ring(l, ParticleTypes.SQUID_INK, c.add(0, 1.2, 0), 5, 60);
                ring(l, dust(Suit.SCHATTENFUERST), c.add(0, 1.0, 0), 8, 80);
                sound(l, c, SoundEvents.WITHER_SHOOT, 1.2f);
            }
        }
    }

    // ---------- Infernus ----------
    private static void inferno(ServerLevel l, ServerPlayer p, int slot) {
        switch (slot) {
            case 0 -> { // Feuerstrahl
                Vec3 s = p.getEyePosition();
                LivingEntity t = rayEntity(l, p, 25);
                Vec3 end = t != null ? t.position().add(0, t.getBbHeight() / 2, 0) : rayPoint(l, p, 25);
                beam(l, s, end, ParticleTypes.FLAME);
                beam(l, s, end, dust(Suit.INFERNUS));
                if (t != null) {
                    hurt(l, p, t, 7);
                    t.igniteForSeconds(8);
                    particles(l, ParticleTypes.LAVA, end, 20, 0.4, 0.1);
                }
                sound(l, s, SoundEvents.BLAZE_SHOOT, 1.2f);
            }
            case 1 -> { // Lavasprung
                Vec3 d = p.getLookAngle();
                p.setDeltaMovement(d.x * 0.8, 1.4, d.z * 0.8);
                p.hurtMarked = true;
                p.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 200, 0));
                p.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 80, 0));
                particles(l, ParticleTypes.FLAME, p.position(), 80, 0.5, 0.1);
                particles(l, ParticleTypes.LAVA, p.position(), 15, 0.5, 0.1);
                sound(l, p.position(), SoundEvents.FIRECHARGE_USE, 1.2f);
            }
            case 2 -> { // Inferno
                Vec3 c = p.position();
                for (LivingEntity e : near(l, p, c, 8)) {
                    hurt(l, p, e, 6);
                    e.igniteForSeconds(6);
                }
                ring(l, ParticleTypes.FLAME, c.add(0, 0.3, 0), 8, 140);
                ring(l, ParticleTypes.EXPLOSION, c.add(0, 0.5, 0), 5, 12);
                particles(l, ParticleTypes.LAVA, c.add(0, 1, 0), 50, 4, 0.2);
                sound(l, c, SoundEvents.FIRECHARGE_USE, 1.6f);
            }
        }
    }

    // ---------- Waffen-Effekte beim Treffer ----------
    public static void weaponHit(ServerLevel l, ServerPlayer p, LivingEntity t) {
        Suit s = null;
        for (Suit x : Suit.values()) if (p.getMainHandItem().is(ModItems.WEAPON.get(x))) s = x;
        if (s == null) return;
        Vec3 pos = t.position().add(0, t.getBbHeight() / 2, 0);
        switch (s) {
            case VOLTSTRAHL -> {
                t.hurtServer(l, l.damageSources().lightningBolt(), 3);
                particles(l, ParticleTypes.ELECTRIC_SPARK, pos, 30, 0.5, 0.2);
                sound(l, pos, SoundEvents.LIGHTNING_BOLT_THUNDER, 0.3f);
            }
            case EISWAECHTER -> {
                t.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 2));
                t.setTicksFrozen(200);
                particles(l, ParticleTypes.SNOWFLAKE, pos, 30, 0.5, 0.05);
            }
            case SCHATTENFUERST -> {
                t.addEffect(new MobEffectInstance(MobEffects.WITHER, 80, 1));
                p.heal(2);
                particles(l, ParticleTypes.SOUL, pos, 25, 0.4, 0.05);
            }
            case INFERNUS -> {
                t.igniteForSeconds(6);
                particles(l, ParticleTypes.FLAME, pos, 30, 0.4, 0.05);
            }
        }
    }

    // ---------- Passive Anzug-Effekte ----------
    public static void tick(MinecraftServer server) {
        int t = server.getTickCount();
        if (t % 10 != 0) return;
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            Suit s = worn(p);
            if (s == null) continue;
            ServerLevel l = (ServerLevel) p.level();
            Vec3 c = p.position().add(0, 1, 0);
            switch (s) {
                case VOLTSTRAHL -> particles(l, ParticleTypes.ELECTRIC_SPARK, c, 3, 0.4, 0.02);
                case EISWAECHTER -> particles(l, ParticleTypes.SNOWFLAKE, c, 3, 0.4, 0.02);
                case SCHATTENFUERST -> particles(l, ParticleTypes.SOUL, c, 2, 0.4, 0.02);
                case INFERNUS -> particles(l, ParticleTypes.FLAME, c, 3, 0.4, 0.02);
            }
            if (t % 40 == 0) {
                switch (s) {
                    case VOLTSTRAHL -> { eff(p, MobEffects.SPEED, 1); eff(p, MobEffects.JUMP_BOOST, 1); }
                    case EISWAECHTER -> { eff(p, MobEffects.RESISTANCE, 0); eff(p, MobEffects.WATER_BREATHING, 0); }
                    case SCHATTENFUERST -> { eff(p, MobEffects.NIGHT_VISION, 0); eff(p, MobEffects.STRENGTH, 0); }
                    case INFERNUS -> { eff(p, MobEffects.FIRE_RESISTANCE, 0); eff(p, MobEffects.STRENGTH, 1); }
                }
            }
        }
    }

    private static void eff(ServerPlayer p, Holder<MobEffect> e, int amp) {
        p.addEffect(new MobEffectInstance(e, 260, amp, true, false));
    }

    // ---------- Helfer ----------
    private static void hurt(ServerLevel l, ServerPlayer p, LivingEntity t, float dmg) {
        t.hurtServer(l, l.damageSources().playerAttack(p), dmg);
    }

    private static ParticleOptions dust(Suit s) { return new DustParticleOptions(s.color, 1.3f); }

    private static void particles(ServerLevel l, ParticleOptions o, Vec3 c, int n, double spread, double speed) {
        l.sendParticles(o, c.x, c.y, c.z, n, spread, spread, spread, speed);
    }

    private static void ring(ServerLevel l, ParticleOptions o, Vec3 c, double r, int n) {
        for (int i = 0; i < n; i++) {
            double a = 2 * Math.PI * i / n;
            l.sendParticles(o, c.x + Math.cos(a) * r, c.y, c.z + Math.sin(a) * r, 1, 0, 0.05, 0, 0.01);
        }
    }

    private static void beam(ServerLevel l, Vec3 a, Vec3 b, ParticleOptions o) {
        Vec3 d = b.subtract(a);
        double len = d.length();
        if (len < 0.01) return;
        Vec3 dir = d.scale(1 / len);
        for (double t = 0; t < len; t += 0.6) {
            Vec3 q = a.add(dir.scale(t));
            l.sendParticles(o, q.x, q.y, q.z, 1, 0, 0, 0, 0);
        }
    }

    private static void sound(ServerLevel l, Vec3 p, SoundEvent s, float pitch) {
        l.playSound(null, p.x, p.y, p.z, s, SoundSource.PLAYERS, 1.0f, pitch);
    }

    private static List<LivingEntity> near(ServerLevel l, ServerPlayer p, Vec3 c, double r) {
        return l.getEntitiesOfClass(LivingEntity.class, new AABB(c, c).inflate(r),
                e -> e != p && e.isAlive() && e.distanceToSqr(c) <= r * r);
    }

    private static Vec3 rayPoint(ServerLevel l, ServerPlayer p, double range) {
        Vec3 s = p.getEyePosition(), e = s.add(p.getLookAngle().scale(range));
        BlockHitResult r = l.clip(new ClipContext(s, e, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, p));
        return r.getType() == HitResult.Type.MISS ? e : r.getLocation();
    }

    private static LivingEntity rayEntity(ServerLevel l, ServerPlayer p, double range) {
        Vec3 s = p.getEyePosition(), d = p.getLookAngle(), e = s.add(d.scale(range));
        double maxD = s.distanceToSqr(rayPoint(l, p, range));
        AABB box = p.getBoundingBox().expandTowards(d.scale(range)).inflate(1);
        LivingEntity best = null; double bd = Double.MAX_VALUE;
        for (LivingEntity t : l.getEntitiesOfClass(LivingEntity.class, box, x -> x != p && x.isAlive())) {
            Optional<Vec3> h = t.getBoundingBox().inflate(0.3).clip(s, e);
            if (h.isPresent()) {
                double dd = s.distanceToSqr(h.get());
                if (dd < bd && dd <= maxD + 1) { bd = dd; best = t; }
            }
        }
        return best;
    }
}
