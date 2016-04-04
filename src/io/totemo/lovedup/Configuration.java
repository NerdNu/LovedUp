package io.totemo.lovedup;

import org.bukkit.plugin.Plugin;

// ----------------------------------------------------------------------------
/**
 * Configuration wrapper.
 */
public class Configuration {
    /**
     * True if debug messages hit the logs.
     */
    public boolean DEBUG;

    /**
     * True if players can match with themselves, triggering a message and
     * firework.
     *
     * Players can always send themselves hearts with /love <name>.
     */
    public boolean SELF_MATCH;

    /**
     * If true, the only kind of projectile that can signifies love is an arrow.
     *
     * If false, other types of projectiles, such as snowballs also signify
     * love.
     *
     * Players can always love each other with the /love command, regardless.
     */
    public boolean ONLY_ARROWS_LOVE;

    /**
     * Set to true to enable effects on arrows.
     */
    public boolean ENABLE_ARROW_EFFECTS;

    /**
     * Set to true to enable other projectile effects (not arrows).
     */
    public boolean ENABLE_OTHER_PROJECTILE_EFFECTS;

    /**
     * Duration to show hearts on a player in milliseconds.
     */
    public int PLAYER_MS;

    /**
     * Duration to show hearts on a mob in milliseconds.
     */
    public int MOB_MS;

    /**
     * Duration to show hearts on a projectile in milliseconds.
     */
    public int PROJECTILE_MS;

    // ------------------------------------------------------------------------
    /**
     * Constructor.
     *
     * @param plugin the owning plugin.
     */
    public Configuration(Plugin plugin) {
        _plugin = plugin;
    }

    // ------------------------------------------------------------------------
    /**
     * Load the plugin configuration.
     */
    public void reload() {
        _plugin.reloadConfig();
        DEBUG = _plugin.getConfig().getBoolean("debug");
        SELF_MATCH = _plugin.getConfig().getBoolean("self_match");
        ONLY_ARROWS_LOVE = _plugin.getConfig().getBoolean("only_arrows_love");
        ENABLE_ARROW_EFFECTS = _plugin.getConfig().getBoolean("enable_arrow_effects");
        ENABLE_OTHER_PROJECTILE_EFFECTS = _plugin.getConfig().getBoolean("enable_other_projectile_effects");
        PLAYER_MS = _plugin.getConfig().getInt("tracking.player_ms");
        MOB_MS = _plugin.getConfig().getInt("tracking.mob_ms");
        PROJECTILE_MS = _plugin.getConfig().getInt("tracking.projectile_ms");
    }

    // ------------------------------------------------------------------------
    /**
     * Save the configuration to disk.
     */
    public void save() {
        _plugin.getConfig().set("debug", DEBUG);
        _plugin.getConfig().set("self_match", SELF_MATCH);
        _plugin.getConfig().set("only_arrows_love", ONLY_ARROWS_LOVE);
        _plugin.getConfig().set("enable_arrow_effects", ENABLE_ARROW_EFFECTS);
        _plugin.getConfig().set("enable_other_projectile_effects", ENABLE_OTHER_PROJECTILE_EFFECTS);
        _plugin.getConfig().set("tracking.player_ms", PLAYER_MS);
        _plugin.getConfig().set("tracking.mob_ms", MOB_MS);
        _plugin.getConfig().set("tracking.projectile_ms", PROJECTILE_MS);
        _plugin.saveConfig();
    }

    // ------------------------------------------------------------------------
    /**
     * Owning plugin.
     */
    private final Plugin _plugin;

} // class Configuration