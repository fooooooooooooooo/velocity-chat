package me.confor.velocity.chat;

import com.google.inject.Inject;
import com.moandjiezana.toml.Toml;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class Config {
    static final long CONFIG_VERSION = 3;

    Path dataDir;
    Toml toml;

    // is there a less ugly way of doing this?
    // defaults are set in loadConfigs()

    public boolean GLOBAL_CHAT_ENABLED;
    public boolean GLOBAL_CHAT_TO_CONSOLE;
    public boolean GLOBAL_CHAT_PASSTHROUGH;
    public boolean GLOBAL_CHAT_ALLOW_MSG_FORMATTING;
    public String GLOBAL_CHAT_FORMAT;

    public boolean JOIN_ENABLE;
    public String JOIN_FORMAT;

    public boolean QUIT_ENABLE;
    public String QUIT_FORMAT;

    public boolean SWITCH_ENABLE;
    public String SWITCH_FORMAT;

    @Inject
    public Config(@DataDirectory Path dataDir) {
        this.dataDir = dataDir;

        loadFile();
        loadConfigs();
    }

    private void loadFile() {
        File dataDirectoryFile = this.dataDir.toFile();
        if (!dataDirectoryFile.exists())
            dataDirectoryFile.mkdir(); // TODO ensure it succeeds

        File dataFile = new File(dataDirectoryFile, "config.toml");

        // create default config if it doesn't exist
        if (!dataFile.exists()) {
            try {
                InputStream in = this.getClass().getResourceAsStream("/config.toml");
                Files.copy(in, dataFile.toPath());
            } catch (IOException e) {
                throw new RuntimeException("ERROR: Can't write default configuration file (permissions/filesystem error?)");
            }
        }

        this.toml = new Toml().read(dataFile);

        // make sure the config makes sense for the current plugin's version
        long version = this.toml.getLong("config_version", 0L);
        if (version != CONFIG_VERSION) {
            throw new RuntimeException("ERROR: Can't use the existing configuration file: version mismatch (intended for another, older version?)");
        }
    }

    public void loadConfigs() {
        this.GLOBAL_CHAT_ENABLED = this.toml.getBoolean("chat.enable", true);
        this.GLOBAL_CHAT_TO_CONSOLE = this.toml.getBoolean("chat.log_to_console", false);
        this.GLOBAL_CHAT_PASSTHROUGH = this.toml.getBoolean("chat.passthrough", false);
        this.GLOBAL_CHAT_ALLOW_MSG_FORMATTING = this.toml.getBoolean("chat.parse_player_messages", false);
        this.GLOBAL_CHAT_FORMAT = this.toml.getString("chat.format", "<player>: <message>");

        this.JOIN_ENABLE = this.toml.getBoolean("join.enable", true);
        this.JOIN_FORMAT = this.toml.getString("join.format", "<player> connected");

        this.QUIT_ENABLE = this.toml.getBoolean("quit.enable", true);
        this.QUIT_FORMAT = this.toml.getString("quit.format", "<player> disconnected");

        this.SWITCH_ENABLE = this.toml.getBoolean("switch.enable", true);
        this.SWITCH_FORMAT = this.toml.getString("switch.format", "<player> moved to <server>");
    }
}
