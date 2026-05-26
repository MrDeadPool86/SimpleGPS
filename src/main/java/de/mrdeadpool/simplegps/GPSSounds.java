package de.mrdeadpool.simplegps;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class GPSSounds {

    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(Registries.SOUND_EVENT, SimpleGPS.MOD_ID);

    public static final DeferredHolder<SoundEvent, SoundEvent> ARRIVAL =
            SOUNDS.register("arrival",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(SimpleGPS.MOD_ID, "arrival")
                    )
    );

    public static final DeferredHolder<SoundEvent, SoundEvent> ARRIVAL_INT =
            SOUNDS.register("arrival_int",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(SimpleGPS.MOD_ID, "arrival_int")
                    )
            );

    public static void register(IEventBus modEventBus) {
        SOUNDS.register(modEventBus);
    }
}