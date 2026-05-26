package de.mrdeadpool.simplegps.client;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

@EventBusSubscriber(
        modid = de.mrdeadpool.simplegps.SimpleGPS.MOD_ID,
        value = Dist.CLIENT
)
public class GPSClientCommands {

    @SubscribeEvent
    public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(
                Commands.literal("gps")
                        .then(Commands.literal("history")
                                .executes(ctx -> {
                                    net.minecraft.client.Minecraft.getInstance()
                                            .setScreen(new GPSHistoryScreen());
                                    return 1;
                                })
                        )
        );
    }
}