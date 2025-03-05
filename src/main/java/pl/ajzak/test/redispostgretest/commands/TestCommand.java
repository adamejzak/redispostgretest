package pl.ajzak.test.redispostgretest.commands;

import dev.rollczi.litecommands.annotations.argument.Arg;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.inject.Inject;
import dev.rollczi.litecommands.annotations.join.Join;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import pl.ajzak.test.redispostgretest.Main;
import pl.ajzak.test.redispostgretest.notice.Notice;
import pl.ajzak.test.redispostgretest.user.User;
import pl.ajzak.test.redispostgretest.user.UserManager;

import java.util.UUID;

@Command(name = "test")
public class TestCommand {

    private final UserManager userManager;

    @Inject
    TestCommand(Notice notice) {
        this.userManager = Main.getInstance().getUserManager();
    }

    @Execute(name = "load")
    void loadUser(@Context Player player, @Join String identifier) {
        UUID uuid = getUUIDFromIdentifier(identifier);
        if (uuid == null) {
            Notice.send(player, "<red>Nie znaleziono gracza o podanym identifierze.");
            return;
        }
        User userFromDatabase = userManager.getUserFromDatabase(uuid);
        User userFromCache = userManager.getUserFromCache(uuid);
        if (userFromCache != null) {
            Notice.send(player, "<green>Użytkownik w bazie: " + userFromCache.getName() + " z " + userFromDatabase.getCoins() + " monetami.");
            Notice.send(player, "<green>Użytkownik w cache: " + userFromCache.getName() + " z " + userFromCache.getCoins() + " monetami.");
        } else {
            Notice.send(player, "<red>Użytkownik nie znaleziony w cache.");
        }
    }

    @Execute(name = "set")
    void setCoins(@Context Player player, @Arg String identifier, @Arg int coins) {
        UUID uuid = getUUIDFromIdentifier(identifier);
        User userFromCache = userManager.getUserFromCache(uuid);
        if (uuid == null) {
            Notice.send(player, "<red>Nie znaleziono gracza o podanym identifierze.");
            return;
        }
        userManager.updateUserCoins(uuid, coins);
        Notice.send(player, "<green>Ustawiono " + coins + " monet dla gracza " + userFromCache.getName());
    }

    private UUID getUUIDFromIdentifier(String identifier) {
        try {
            return UUID.fromString(identifier);
        } catch (IllegalArgumentException e) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(identifier);
            if (offlinePlayer.hasPlayedBefore() || offlinePlayer.isOnline()) {
                return offlinePlayer.getUniqueId();
            } else {
                return null;
            }
        }
    }
}