package pl.ajzak.test.redispostgretest.user;

import org.bukkit.entity.Player;
import pl.ajzak.test.redispostgretest.Main;

import java.util.UUID;

public class User {
    private final UUID uniqueId;
    private final String name;
    private int coins;

    public User(UUID uniqueId, String name, int coins) {
        this.uniqueId = uniqueId;
        this.name = name;
        this.coins = coins;
    }

    public UUID getUniqueId() {
        return uniqueId;
    }

    public String getName() {
        return name;
    }

    public int getCoins() {
        return coins;
    }

    public void addCoins(int amount) {
        this.coins += amount;
    }

    public void removeCoins(int amount) {
        this.coins = Math.max(0, coins - amount);
    }

    public void setCoins(int coins) {
        this.coins = Math.max(0, coins);
    }

    private Player getPlayer() {
        return Main.getInstance().getServer().getPlayer(uniqueId);
    }
}
