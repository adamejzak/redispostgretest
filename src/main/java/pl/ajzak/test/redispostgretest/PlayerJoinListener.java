package pl.ajzak.test.redispostgretest;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import pl.ajzak.test.redispostgretest.user.User;
import pl.ajzak.test.redispostgretest.user.UserManager;

import java.util.UUID;

public class PlayerJoinListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        String name = player.getName();
        UserManager userManager = Main.getInstance().getUserManager();
        User user = userManager.getUser(player.getUniqueId(), player.getName());

    }
}
