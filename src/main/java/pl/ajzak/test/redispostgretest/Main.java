package pl.ajzak.test.redispostgretest;

import dev.rollczi.litecommands.LiteCommands;
import dev.rollczi.litecommands.adventure.LiteAdventureExtension;
import dev.rollczi.litecommands.bukkit.LiteBukkitFactory;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import pl.ajzak.test.redispostgretest.commands.TestCommand;
import pl.ajzak.test.redispostgretest.database.DatabaseManager;
import pl.ajzak.test.redispostgretest.database.RedisManager;
import pl.ajzak.test.redispostgretest.notice.Notice;
import pl.ajzak.test.redispostgretest.user.UserManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public final class Main extends JavaPlugin {
    private static Main instance;
    private DatabaseManager databaseManager;
    private RedisManager redisManager;
    private UserManager userManager;
    private LiteCommands<CommandSender> liteCommands;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    @Override
    public void onEnable() {
        instance = this;
        this.databaseManager = new DatabaseManager();
        this.redisManager = new RedisManager("redis://localhost:6379");
        this.userManager = new UserManager(databaseManager, redisManager);

        try (Connection connection = databaseManager.getConnection()) {
            getLogger().info("Połączenie z PostgreSQL udane!");
        } catch (SQLException e) {
            getLogger().severe("Błąd połączenia z bazą danych: " + e.getMessage());
        }
        createTable();

        this.liteCommands = LiteBukkitFactory.builder("fallback-prefix", this)
                .commands(TestCommand.class)
                .bind(Notice.class, Notice::new)
                .extension(new LiteAdventureExtension<>(), config -> config
                        .miniMessage(true)
                        .serializer(this.miniMessage)
                )
                .build();

        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
    }

    public void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS players (" +
                "uuid VARCHAR(36) PRIMARY KEY," +
                "name VARCHAR(16) NOT NULL," +
                "coins INT DEFAULT 0)";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        if (this.liteCommands != null) {
            this.liteCommands.unregister();
        }
        databaseManager.close();
        redisManager.close();
    }

    public static Main getInstance() {
        return instance;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public UserManager getUserManager() {
        return userManager;
    }

    public RedisManager getRedisManager() {
        return redisManager;
    }
}