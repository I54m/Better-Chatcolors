package com.i54m.betterchatcolors.managers;


import com.i54m.betterchatcolors.util.NameFetcher;
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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


@NoArgsConstructor
public class PlayerDataManager implements Listener, Manager {

    @Getter
    private static final PlayerDataManager INSTANCE = new PlayerDataManager();
    private final WorkerManager WORKER_MANAGER = WorkerManager.getINSTANCE();
    private final Map<UUID, String> playerDataCache = new HashMap<>();
    private final Map<UUID, Long> boldDataCache = new HashMap<>();
    private boolean locked = true;
    private final HikariDataSource hikari = new HikariDataSource();
    public String database;
    public Connection connection;
    private String host, username, password, extraArguments;
    private int port;
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
        locked = true;
    }

    private void openConnection() throws Exception {
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

    private void setupStorage() throws Exception {
        host = PLUGIN.getConfig().getString("MySQL.host", "localhost");
        database = PLUGIN.getConfig().getString("MySQL.database", "betterchatcolors");
        username = PLUGIN.getConfig().getString("MySQL.username", "plugins");
        password = PLUGIN.getConfig().getString("MySQL.password", "plugins");
        port = PLUGIN.getConfig().getInt("MySQL.port", 3306);
        extraArguments = PLUGIN.getConfig().getString("MySQL.extraArguments", "?useSSL=false");
        hikari.addDataSourceProperty("serverName", host);
        hikari.addDataSourceProperty("port", port);
        hikari.setPassword(password);
        hikari.setUsername(username);
        hikari.setJdbcUrl("jdbc:mysql://" + this.host + ":" + this.port + "/" + this.extraArguments);
        hikari.setPoolName("Better-ChatColors");
        hikari.setMaxLifetime(60000);
        hikari.setIdleTimeout(45000);
        hikari.setMaximumPoolSize(50);
        hikari.setMinimumIdle(10);
        openConnection();
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


    public String getPlayerData(@NotNull Player player, boolean create) {
        return getPlayerData(player.getUniqueId(), create);
    }

    public String getPlayerData(@NotNull UUID uuid, boolean create) {
        if (locked) {
            try {
                throw new Exception("Player Data Manager Not Started!");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
        if (PLUGIN.getConfig().getBoolean("MySql.debugMode"))
            PLUGIN.getLogger().info("Getting playerdata for user: " + uuid + ". Creating new file? " + create);
        if (isPlayerDataLoaded(uuid))
            return playerDataCache.get(uuid);
        else return loadPlayerData(uuid, create);
    }

    public Long getBoldData(@NotNull UUID uuid) {
        if (locked) {
            try {
                throw new Exception("Player Data Manager Not Started!");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
        if (PLUGIN.getConfig().getBoolean("MySql.debugMode"))
            PLUGIN.getLogger().info("Getting playerdata for user: " + uuid + ".");
        if (isBoldDataLoaded(uuid))
            return boldDataCache.get(uuid);
        else return loadBoldData(uuid);
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

    public void setBoldData(@NotNull UUID uuid, @NotNull Long newData) {
        if (locked) {
            try {
                throw new Exception("Player Data Manager Not Started!");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }
        if (PLUGIN.getConfig().getBoolean("MySql.debugMode"))
            PLUGIN.getLogger().info("Setting bold cooldown data for user: " + uuid + ". Data: " + newData);
        boldDataCache.put(uuid, newData);
        saveBoldData(uuid, newData);
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

    public void setPlayerData(@NotNull UUID uuid, @NotNull String newData) {
        if (locked) {
            try {
                throw new Exception("Player Data Manager Not Started!");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }
        if (PLUGIN.getConfig().getBoolean("MySql.debugMode"))
            PLUGIN.getLogger().info("Setting playerdata for user: " + uuid + ". Data: " + newData);
        playerDataCache.put(uuid, newData);
        savePlayerData(uuid, newData);
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

    private boolean isBoldDataLoaded(UUID uuid) {
        if (locked) {
            try {
                throw new Exception("Player Data Manager Not Started!");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }
        return boldDataCache.containsKey(uuid);
    }

    public boolean isPlayerDataLoaded(@NotNull UUID uuid) {
        if (locked) {
            try {
                throw new Exception("Player Data Manager Not Started!");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }
        return playerDataCache.containsKey(uuid);
    }
}
