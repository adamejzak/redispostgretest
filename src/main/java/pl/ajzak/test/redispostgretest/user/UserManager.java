package pl.ajzak.test.redispostgretest.user;

import pl.ajzak.test.redispostgretest.database.DatabaseManager;
import pl.ajzak.test.redispostgretest.database.RedisManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class UserManager {
    private final DatabaseManager databaseManager;
    private final RedisManager redisManager;

    public UserManager(DatabaseManager databaseManager, RedisManager redisManager) {
        this.databaseManager = databaseManager;
        this.redisManager = redisManager;
    }

    public User getUserFromCache(UUID uuid) {
        String key = "user:" + uuid.toString();
        String userData = redisManager.getSyncCommands().get(key);
        if (userData != null) {
            return deserializeUser(userData);
        }
        return null;
    }

    public User getUserFromDatabase(UUID uuid) {
        return loadFromDatabase(uuid);
    }

    public void updateUserCoins(UUID uuid, int coins) {
        User user = getUser(uuid, null);
        if (user != null) {
            user.setCoins(coins);
            saveUser(user);
        }
    }

    public void addUserCoins(UUID uuid, int amount) {
        User user = getUser(uuid, null);
        if (user != null) {
            user.addCoins(amount);
            saveUser(user);
        }
    }

    public void removeUserFromCache(UUID uuid) {
        String key = "user:" + uuid.toString();
        redisManager.getSyncCommands().del(key);
    }

    public User getUser(UUID uuid, String name) {
        User user = getUserFromCache(uuid);
        if (user != null) {
            return user;
        }

        user = loadFromDatabase(uuid);
        if (user == null && name != null) {
            user = new User(uuid, name, 0);
            saveUser(user);
        }

        if (user != null) {
            String key = "user:" + uuid.toString();
            redisManager.getSyncCommands().set(key, serializeUser(user));
        }
        return user;
    }

    public void saveUser(User user) {
        String sql = "INSERT INTO players (uuid, name, coins) VALUES (?, ?, ?) " +
                "ON CONFLICT (uuid) DO UPDATE SET name = EXCLUDED.name, coins = EXCLUDED.coins";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getUniqueId().toString());
            stmt.setString(2, user.getName());
            stmt.setInt(3, user.getCoins());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String key = "user:" + user.getUniqueId().toString();
        redisManager.getSyncCommands().set(key, serializeUser(user));
    }

    private User deserializeUser(String data) {
        String[] parts = data.split(":");
        if (parts.length != 3) {
            return null;
        }
        UUID uuid = UUID.fromString(parts[0]);
        String name = parts[1];
        int coins = Integer.parseInt(parts[2]);
        return new User(uuid, name, coins);
    }

    private User loadFromDatabase(UUID uuid) {
        String sql = "SELECT * FROM players WHERE uuid = ?";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new User(
                        uuid,
                        rs.getString("name"),
                        rs.getInt("coins")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String serializeUser(User user) {
        return user.getUniqueId().toString() + ":" + user.getName() + ":" + user.getCoins();
    }
}