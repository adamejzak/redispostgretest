package pl.ajzak.test.redispostgretest.notice;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class Notice {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    public static void send(@NotNull CommandSender sender, @NotNull String message) {
        Component component = parse(message);
        Audience audience = Audience.audience(sender);
        audience.sendMessage(component);
    }

    private static Component parse(String message) {
        return MINI_MESSAGE.deserialize(message);
    }

}
