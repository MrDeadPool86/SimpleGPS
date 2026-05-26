package de.mrdeadpool.simplegps.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import de.mrdeadpool.simplegps.GPSData;
import de.mrdeadpool.simplegps.network.GPSNetwork;
import de.mrdeadpool.simplegps.network.GPSPacket;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.server.level.ServerPlayer;

public class GPSCommand {

    private static Component t(String key) {
        return Component.translatable(key);
    }

    public static void register(CommandDispatcher<CommandSourceStack> d) {

        d.register(Commands.literal("gps")

                // ---------------- SET ----------------
                .then(Commands.literal("set")
                        .then(Commands.argument("name", StringArgumentType.string())
                                .then(Commands.argument("x", DoubleArgumentType.doubleArg())
                                        .then(Commands.argument("y", DoubleArgumentType.doubleArg())
                                                .then(Commands.argument("z", DoubleArgumentType.doubleArg())
                                                        .executes(ctx -> {
                                                            ServerPlayer p = ctx.getSource().getPlayerOrException();
                                                            String name = StringArgumentType.getString(ctx, "name");
                                                            double x = DoubleArgumentType.getDouble(ctx, "x");
                                                            double y = DoubleArgumentType.getDouble(ctx, "y");
                                                            double z = DoubleArgumentType.getDouble(ctx, "z");

                                                            String dimension = p.level().dimension().location().toString();

                                                            GPSData.set(p.getUUID(), name, x, y, z);
                                                            GPSNetwork.sendToPlayer(p,
                                                                    new GPSPacket(true, name, x, y, z, "red", dimension));

                                                            p.sendSystemMessage(
                                                                    Component.literal("§l§b[GPS] §f")
                                                                            .append(t("gps.nav.to"))
                                                                            .append(" §b" + name)
                                                            );

                                                            return 1;
                                                        })

                                                        .then(Commands.argument("color", StringArgumentType.word())
                                                                .suggests((ctx, builder) -> {
                                                                    builder.suggest("white");
                                                                    builder.suggest("red");
                                                                    builder.suggest("green");
                                                                    builder.suggest("blue");
                                                                    builder.suggest("yellow");
                                                                    return builder.buildFuture();
                                                                })
                                                                .executes(ctx -> {
                                                                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                                                                    String name = StringArgumentType.getString(ctx, "name");
                                                                    double x = DoubleArgumentType.getDouble(ctx, "x");
                                                                    double y = DoubleArgumentType.getDouble(ctx, "y");
                                                                    double z = DoubleArgumentType.getDouble(ctx, "z");
                                                                    String color = StringArgumentType.getString(ctx, "color");

                                                                    String dimension = p.level().dimension().location().toString();

                                                                    GPSData.set(p.getUUID(), name, x, y, z);
                                                                    GPSNetwork.sendToPlayer(p,
                                                                            new GPSPacket(true, name, x, y, z, color, dimension));

                                                                    p.sendSystemMessage(
                                                                            Component.literal("§l§b[GPS] §f")
                                                                                    .append(t("gps.nav.to"))
                                                                                    .append(" §b" + name)
                                                                    );

                                                                    return 1;
                                                                })
                                                        )
                                                )
                                        )
                                )
                        )
                )

                // ---------------- SHARE ----------------
                .then(Commands.literal("share")
                        .executes(ctx -> {
                            ServerPlayer p = ctx.getSource().getPlayerOrException();
                            GPSData.GPSTarget target = GPSData.get(p.getUUID());

                            if (target == null) {
                                p.sendSystemMessage(
                                        Component.literal("§b[GPS] §f")
                                                .append(t("gps.nav.no"))
                                );
                                return 0;
                            }

                            shareTarget(ctx.getSource(), p,
                                    target.name(), target.x(), target.y(), target.z());
                            return 1;
                        })

                        .then(Commands.argument("name", StringArgumentType.string())
                                .then(Commands.argument("x", DoubleArgumentType.doubleArg())
                                        .then(Commands.argument("y", DoubleArgumentType.doubleArg())
                                                .then(Commands.argument("z", DoubleArgumentType.doubleArg())
                                                        .executes(ctx -> {
                                                            ServerPlayer p = ctx.getSource().getPlayerOrException();
                                                            String name = StringArgumentType.getString(ctx, "name");
                                                            double x = DoubleArgumentType.getDouble(ctx, "x");
                                                            double y = DoubleArgumentType.getDouble(ctx, "y");
                                                            double z = DoubleArgumentType.getDouble(ctx, "z");

                                                            shareTarget(ctx.getSource(), p, name, x, y, z);
                                                            return 1;
                                                        })
                                                )
                                        )
                                )
                        )
                )

                // ---------------- END ----------------
                .then(Commands.literal("end")
                        .executes(ctx -> {
                            ServerPlayer p = ctx.getSource().getPlayerOrException();

                            // Server löschen
                            GPSData.remove(p.getUUID());

                            // Client sicher löschen
                            GPSNetwork.sendToPlayer(p,
                                    new GPSPacket(false, "", 0, 0, 0, "red", "minecraft:overworld"));

                            p.sendSystemMessage(
                                    Component.literal("§l§b[GPS] §f")
                                            .append(t("gps.nav.end"))
                            );

                            return 1;
                        }))
        );
    }

    private static void shareTarget(CommandSourceStack source, ServerPlayer sender,
                                    String name, double x, double y, double z) {

        Component msg = Component.literal("")
                .append(Component.literal("§b[GPS] §c" + sender.getName().getString() + "§f "))
                .append(t("gps.nav.shares"))
                .append(" §e'" + name + "' ")
                .append(Component.literal("§7(" + (int)x + ", " + (int)y + ", " + (int)z + ") "))
                .append(
                        Component.literal("§a[► ")
                                .append(t("gps.nav.nav"))
                                .append("]")
                                .withStyle(style -> style
                                        .withClickEvent(new ClickEvent(
                                                ClickEvent.Action.RUN_COMMAND,
                                                "/gps set \"" + name + "\" " + x + " " + y + " " + z
                                        ))
                                        .withHoverEvent(new HoverEvent(
                                                HoverEvent.Action.SHOW_TEXT,
                                                Component.literal("§7")
                                                        .append(t("gps.nav.click"))
                                        ))
                                )
                );

        source.getServer().getPlayerList().getPlayers()
                .forEach(player -> player.sendSystemMessage(msg));

        sender.displayClientMessage(
                Component.literal("§b[GPS] §f")
                        .append(t("gps.nav.target"))
                        .append(" §e'" + name + "' §f")
                        .append(t("gps.nav.was.shared")),
                true
        );
    }
}