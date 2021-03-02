package com.i54m.betterchatcolors.managers;


import com.i54m.betterchatcolors.util.NameFetcher;
import com.i54m.betterchatcolors.util.StorageType;
import com.i54m.betterchatcolors.util.UUIDFetcher;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


@NoArgsConstructor
public class PlayerDataManager implements Listener, Manager {

    private final Map<UUID, String> playerDataCache = new HashMap<>();
    private final Map<UUID, Long> boldDataCache = new HashMap<>();
    private final WorkerManager WORKER_MANAGER = WorkerManager.getINSTANCE();
    @Getter
    private static final PlayerDataManager INSTANCE = new PlayerDataManager();
    private boolean locked = true;
    private final HikariDataSource hikari = new HikariDataSource();
    public String database;
    public Connection connection;
    private int cacheTaskid = -1;


    @Override
    public boolean isStarted() {
        return !locked;
    }

    @Override
    public void start() {
        if (!locked) {
            try {
                throw new Exception("Player Data Manager Already Started!");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }
        PLUGIN.getServer().getPluginManager().registerEvents(this, PLUGIN);
        try {
            setupStorage();
        } catch (Exception e) {
            PLUGIN.getLogger().severe("Error starting player data manager. Error Message: " + e.getMessage());
            e.printStackTrace();
            return;
        }
        locked = false;
        PLUGIN.getLogger().info("Started Player Data Manager!");
    }

    @Override
    public void stop() {
        if (locked) {
            try {
                throw new Exception("Player Data Manager Not Started!");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }
        PLUGIN.getServer().getScheduler().cancelTask(cacheTaskid);
        try {
            if (connection != null && !connection.isClosed()) {
                if (hikari.isRunning())
                    hikari.close();
                connection.close();
                connection = null;
            }
        } catch (Exception e) {
            PLUGIN.getLogger().severe("Error occurred while closing mysql connection. Error Message: " + e.getMessage());
        }
        locked = true;
    }

    private void openMYSQLConnection() throws Exception {
        try {
            if (connection != null && !connection.isClosed() || hikari.isRunning())
                return;
            connection = hikari.getConnection();
        } catch (SQLException e) {
            PLUGIN.getLogger().severe(" ");
            PLUGIN.getLogger().severe("An error was encountered and debug info was logged to log file!");
            PLUGIN.getLogger().severe("Error Message: " + e.getMessage());
            if (e.getCause() != null)
                PLUGIN.getLogger().severe("Error Cause Message: " + e.getCause().getMessage());
            PLUGIN.getLogger().severe(" ");
            throw new Exception("MySQL connection failed", e);
        }
    }

    private void openSQLiteConnection() throws Exception {
        try {
            if (this.connection != null && !this.connection.isClosed())
                return;
            Class.forName("org.sqlite.JDBC");
            PLUGIN.getDataFolder().mkdirs();
            System.out.println(PLUGIN.getDataFolder().getPath());
            this.connection = DriverManager.getConnection("jdbc:sqlite:" + PLUGIN.getDataFolder().getPath() + "/" + database + ".db");
        } catch (Exception e) {
            PLUGIN.getLogger().severe(" ");
            PLUGIN.getLogger().severe("An error was encountered and debug info was logged to log file!");
            PLUGIN.getLogger().severe("Error Message: " + e.getMessage());
            if (e.getCause() != null)
                PLUGIN.getLogger().severe("Error Cause Message: " + e.getCause().getMessage());
            PLUGIN.getLogger().severe(" ");
            throw new Exception("Sqlite connection failed", e);
        }
    }

    private void setupStorage() {
        try {
            StorageType storageType = StorageType.valueOf(PLUGIN.getConfig().getString("Storage-Type", "SQLITE").toUpperCase());
            database = PLUGIN.getConfig().getString("MySQL.database", "betterchatcolors");
            switch (storageType) {
                default:
                case SQLITE: {
                    openSQLiteConnection();
                    String playerdata = "CREATE TABLE IF NOT EXISTS betterchatcolors_playerdata" +
                            " ( UUID text PRIMARY KEY," +
                            " Chat_Color text NOT NULL );";
                    String boldData = "CREATE TABLE IF NOT EXISTS betterchatcolors_bolddata" +
                            " ( UUID text PRIMARY KEY," +
                            " Cooldown integer NOT NULL );";
                    Statement stmt = connection.createStatement();
                    stmt.execute(playerdata);
                    stmt.execute(boldData);
                    stmt.close();
                    break;
                }
                case MYSQL: {
                    String host = PLUGIN.getConfig().getString("MySQL.host", "localhost");
                    String username = PLUGIN.getConfig().getString("MySQL.username", "plugins");
                    String password = PLUGIN.getConfig().getString("MySQL.password", "plugins");
                    int port = PLUGIN.getConfig().getInt("MySQL.port", 3306);
                    String extraArguments = PLUGIN.getConfig().getString("MySQL.extraArguments", "?useSSL=false");
                    hikari.addDataSourceProperty("serverName", host);
                    hikari.addDataSourceProperty("port", port);
                    hikari.setPassword(password);
                    hikari.setUsername(username);
                    hikari.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + extraArguments);
                    hikari.setPoolName("Better-ChatColors");
                    hikari.setMaxLifetime(60000);
                    hikari.setIdleTimeout(45000);
                    hikari.setMaximumPoolSize(50);
                    hikari.setMinimumIdle(10);
                    openMYSQLConnection();
                    String createdb = "CREATE DATABASE IF NOT EXISTS " + database;
                    String playerdata = "CREATE TABLE IF NOT EXISTS `" + database + "`.`betterchatcolors_playerdata`" +
                            " ( `UUID` VARCHAR(36) NOT NULL ," +
                            " `Chat_Color` VARCHAR(32) NOT NULL ," +
                            " PRIMARY KEY (`UUID`(36)))" +
                            " ENGINE = InnoDB CHARSET=utf8 COLLATE utf8_general_ci;";
                    String boldData = "CREATE TABLE IF NOT EXISTS `" + database + "`.`betterchatcolors_bolddata`" +
                            " ( `UUID` VARCHAR(36) NOT NULL ," +
                            " `Cooldown` BIGINT NOT NULL ," +
                            " PRIMARY KEY (`UUID`(36)))" +
                            " ENGINE = InnoDB CHARSET=utf8 COLLATE utf8_general_ci;";
                    String use_db = "USE " + database;
                    PreparedStatement stmt = connection.prepareStatement(createdb);
                    PreparedStatement stmt1 = connection.prepareStatement(playerdata);
                    PreparedStatement stmt2 = connection.prepareStatement(boldData);
                    PreparedStatement stmt3 = connection.prepareStatement(use_db);
                    stmt.executeUpdate();
                    stmt.close();
                    stmt1.executeUpdate();
                    stmt1.close();
                    stmt2.executeUpdate();
                    stmt2.close();
                    stmt3.executeUpdate();
                    stmt3.close();
                    break;
                }
            }
        } catch (Exception e) {
            PLUGIN.getLogger().severe("Invalid storage type defined!!");
            PLUGIN.getLogger().severe(PLUGIN.getConfig().getString("Storage-Type") + " is not a defined storage type (must be either MYSQL or SQLITE)");
        }
    }

    public void startCaching() {
        if (locked) {
            try {
                throw new Exception("Player Data Manager Not Started!");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }
        if (cacheTaskid == -1) {
            cache();
            cacheTaskid = PLUGIN.getServer().getScheduler().scheduleSyncRepeatingTask(PLUGIN, () -> {
                if (PLUGIN.getConfig().getBoolean("MySql.debugMode"))
                    PLUGIN.getLogger().info("Caching PlayerData...");
                try {
                    WORKER_MANAGER.runWorker(new WorkerManager.Worker(this::resetCache));
                } catch (Exception e) {
                    PLUGIN.getLogger().severe("Unable to cache ChatColor and Bold cooldown data. Error Message: " + e.getMessage());
                }
            }, 200, 100);
        }
    }

    private void resetCache() {
        playerDataCache.clear();
        cache();
    }


    public void cache() {
        if (locked) {
            try {
                throw new Exception("Player Data Manager Not Started!");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }
        try {
            if (hikari.isRunning()) {
                try {
                    if (connection != null &&
                            connection.isClosed()) {
                        if (hikari.isRunning())
                            hikari.close();
                        connection.close();
                        connection = null;
                        setupStorage();
                        return;
                    } else if (connection == null) {
                        if (hikari.isRunning())
                            hikari.close();
                        setupStorage();
                        return;
                    }
                } catch (Exception e) {
                    PLUGIN.getLogger().severe("Error occurred while closing mysql connection. Error Message: " + e.getMessage());
                    PLUGIN.getLogger().severe("Unable to cache data. Error Message: " + e.getMessage());
                    return;
                }
            }
            if (PLUGIN.getServer().getOnlinePlayers().size() <= 0) { // if no players online it will not ping the database and connection will die so we make sure connection is still valid
                connection.isValid(15);
                return;
            }
            for (Player player : PLUGIN.getServer().getOnlinePlayers()) {
                loadPlayerData(player.getUniqueId(), false);
                loadBoldData(player.getUniqueId());
            }

        } catch (Exception e) {
            PLUGIN.getLogger().severe("Unable to cache ChatColor data. Error Message: " + e.getMessage());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerLogin(@NotNull final PlayerLoginEvent event) {
        UUIDFetcher.updateStoredUUID(event.getPlayer().getName(), event.getPlayer().getUniqueId());
        NameFetcher.updateStoredName(event.getPlayer().getUniqueId(), event.getPlayer().getName());
        if (locked) {
            try {
                throw new Exception("Player Data Manager Not Started!");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }
        WorkerManager.getINSTANCE().runWorker(new WorkerManager.Worker(() -> {
            UUID uuid = event.getPlayer().getUniqueId();
            try {
                loadPlayerData(uuid, true);
            } catch (Exception e) {
                PLUGIN.getLogger().severe("Unable to load ChatColor data for: " + NameFetcher.getName(uuid) + ". Error Message: " + e.getMessage());
                event.getPlayer().kickPlayer("Unable to load ChatColor data for you, Please try and login again.\n If the issue persists, contact an admin+");
            }
            try {
                loadBoldData(uuid);
            } catch (Exception e) {
                PLUGIN.getLogger().severe("Unable to load ChatColor data for: " + NameFetcher.getName(uuid) + ". Error Message: " + e.getMessage());
                event.getPlayer().kickPlayer("Unable to load ChatColor data for you, Please try and login again.\n If the issue persists, contact an admin+");
            }
        }));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerLogout(@NotNull final PlayerQuitEvent event) {
        if (locked) {
            try {
                throw new Exception("Player Data Manager Not Started!");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }
        WorkerManager.getINSTANCE().runWorker(new WorkerManager.Worker(() -> {
            UUID uuid = event.getPlayer().getUniqueId();
            savePlayerData(uuid, playerDataCache.get(uuid));
            setBoldData(uuid, boldDataCache.get(uuid));
            playerDataCache.remove(uuid);
            boldDataCache.remove(uuid);
        }));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerLogout(@NotNull final PlayerKickEvent event) {
        if (locked) {
            try {
                throw new Exception("Player Data Manager Not Started!");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }
        WorkerManager.getINSTANCE().runWorker(new WorkerManager.Worker(() -> {
            UUID uuid = event.getPlayer().getUniqueId();
            savePlayerData(uuid, playerDataCache.get(uuid));
            setBoldData(uuid, boldDataCache.get(uuid));
            playerDataCache.remove(uuid);
            boldDataCache.remove(uuid);
        }));
    }

    public Long loadBoldData(@NotNull UUID uuid) {
        if (locked) {
            try {
                throw new Exception("Player Data Manager Not Started!");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
        if (PLUGIN.getConfig().getBoolean("MySql.debugMode"))
            PLUGIN.getLogger().info("Loading bold cooldown data for user: " + uuid + ".");
        try {
            String sql = "SELECT * FROM `betterchatcolors_bolddata` WHERE `UUID`='" + uuid.toString() + "';";
            PreparedStatement stmt = connection.prepareStatement(sql);
            ResultSet results = stmt.executeQuery();
            if (results.next()) {
                boldDataCache.put(uuid, results.getLong("Cooldown"));
            } else return 0L;
            results.close();
            stmt.close();
            return boldDataCache.get(uuid);
        } catch (Exception e) {
            PLUGIN.getLogger().severe("An Error was encountered while loading/creating bold cooldown data for player: " + NameFetcher.getName(uuid) + ". Error Message: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public void saveBoldData(@NotNull UUID uuid, @NotNull Long newData) {
        if (locked) {
            try {
                throw new Exception("Player Data Manager Not Started!");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }
        if (PLUGIN.getConfig().getBoolean("MySql.debugMode"))
            PLUGIN.getLogger().info("Saving bold cooldown data for user: " + uuid + ". Data: " + newData);
        try {
            String sql = "UPDATE `betterchatcolors_bolddata` SET `Cooldown`='" + newData + "' WHERE `UUID`='" + uuid.toString() + "';";
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.executeUpdate();
            stmt.close();
        } catch (Exception e) {
            PLUGIN.getLogger().severe("An Error was encountered while saving bold cooldown data for player: " + NameFetcher.getName(uuid) + ". Error Message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public String loadPlayerData(@NotNull UUID uuid, boolean create) {
        if (locked) {
            try {
                throw new Exception("Player Data Manager Not Started!");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
        if (PLUGIN.getConfig().getBoolean("MySql.debugMode"))
            PLUGIN.getLogger().info("Loading playerdata for user: " + uuid + ". Creating new file? " + create);
        try {
            String sql = "SELECT * FROM `betterchatcolors_playerdata` WHERE `UUID`='" + uuid.toString() + "';";
            PreparedStatement stmt = connection.prepareStatement(sql);
            ResultSet results = stmt.executeQuery();
            if (results.next()) {
                playerDataCache.put(uuid, results.getString("Chat_Color"));
            } else if (create) {
                saveDefaultData(uuid);
                loadPlayerData(uuid, false);
            }
            results.close();
            stmt.close();
            return playerDataCache.get(uuid);
        } catch (Exception e) {
            PLUGIN.getLogger().severe("An Error was encountered while loading/creating chatcolor data for player: " + NameFetcher.getName(uuid) + ". Error Message: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private void saveDefaultData(@NotNull UUID uuid) {
        if (locked) {
            try {
                throw new Exception("Player Data Manager Not Started!");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }
        if (PLUGIN.getConfig().getBoolean("MySql.debugMode"))
            PLUGIN.getLogger().info("Saving default playerdata for user: " + uuid + ".");
        try {
            String sql1 = "INSERT INTO `betterchatcolors_playerdata` (`UUID`, `Chat_Color`) VALUES ('" + uuid.toString() + "', 'WHITE');";
            PreparedStatement stmt1 = connection.prepareStatement(sql1);
            stmt1.executeUpdate();
            stmt1.close();
        } catch (Exception e) {
            PLUGIN.getLogger().severe("An Error was encountered while saving default chatcolor data for player: " + NameFetcher.getName(uuid) + ". Error Message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void savePlayerData(@NotNull UUID uuid, @NotNull String newData) {
        if (locked) {
            try {
                throw new Exception("Player Data Manager Not Started!");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }
        if (PLUGIN.getConfig().getBoolean("MySql.debugMode"))
            PLUGIN.getLogger().info("Saving playerdata for user: " + uuid + ". Data: " + newData);
        try {
            String sql = "UPDATE `betterchatcolors_playerdata` SET `Chat_Color`='" + newData + "' WHERE `UUID`='" + uuid.toString() + "';";
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.executeUpdate();
            stmt.close();
        } catch (Exception e) {
            PLUGIN.getLogger().severe("An Error was encountered while saving chatcolor data for player: " + NameFetcher.getName(uuid) + ". Error Message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public String getPlayerData(@NotNull Player player, boolean create) {
        return getPlayerData(player.getUniqueId(), create);
    }

    public String getPlayerData(@NotNull UUID uuid, boolean create) {
        if (PLUGIN.getConfig().getBoolean("MySQL.debugMode"))
            PLUGIN.getLogger().info("Getting playerdata for user: " + uuid + ". Creating new file? " + create);
        if (isPlayerDataLoaded(uuid))
            return playerDataCache.get(uuid);
        else return loadPlayerData(uuid, create);
    }

    public Long getBoldData(@NotNull Player player) {
        return getBoldData(player.getUniqueId());
    }

    public Long getBoldData(@NotNull UUID uuid) {
        if (PLUGIN.getConfig().getBoolean("MySQL.debugMode"))
            PLUGIN.getLogger().info("Getting bolddata for user: " + uuid + ".");
        if (isPlayerDataLoaded(uuid))
            return boldDataCache.get(uuid);
        else return loadBoldData(uuid);
    }

    public void setBoldData(@NotNull UUID uuid, @NotNull Long newData) {
        if (PLUGIN.getConfig().getBoolean("MySQL.debugMode"))
            PLUGIN.getLogger().info("Setting bold cooldown data for user: " + uuid + ". Data: " + newData);
        boldDataCache.put(uuid, newData);
        saveBoldData(uuid, newData);
    }

    public void setPlayerData(@NotNull UUID uuid, @NotNull String newData) {
        if (PLUGIN.getConfig().getBoolean("MySQL.debugMode"))
            PLUGIN.getLogger().info("Setting playerdata for user: " + uuid + ". Data: " + newData);
        playerDataCache.put(uuid, newData);
        savePlayerData(uuid, newData);
    }

    public boolean isPlayerDataLoaded(@NotNull UUID uuid) {
        return playerDataCache.containsKey(uuid);
    }

    public boolean isBoldDataLoaded(@NotNull UUID uuid) {
        return boldDataCache.containsKey(uuid);
    }

}
