package de.cubeisland.AntiGuest;

import gnu.trove.map.TObjectLongMap;
import gnu.trove.map.hash.TObjectLongHashMap;
import org.bukkit.ChatColor;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Listener;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

/**
 * This class represents a prevention
 *
 * @author Phillip Schichtel
 */
public abstract class Prevention implements Listener
{
    protected static final String PERMISSION_BASE = "antiguest.preventions.";
    
    private final String name;
    private final Permission permission;
    private String message;
    private int throttleDelay;
    private final PreventionPlugin plugin;
    private boolean enabled;
    private boolean enableByDefault;
    private PreventionConfiguration config;

    private TObjectLongMap<Player> messageThrottleTimestamps;

    /**
     * Initializes the prevention with its name and the corresponding plugin.
     * This contructor use "antiguest.preventions.&lt;name&gt;" as the permission!
     *
     * @param name the name of the prevention
     * @param plugin the corresponding plugin
     */
    public Prevention(final String name, final PreventionPlugin plugin)
    {
        this(name, PERMISSION_BASE + name, plugin);
    }

    /**
     * Initializes the prevention with its name, permission and the corresponding plugin.
     * This contructor use "antiguest.preventions&lt;name&gt;" as the permission!
     *
     * @param name the name of the prevention
     * @param permission the permission
     * @param plugin the corresponding plugin
     */
    public Prevention(final String name, final String permission, final PreventionPlugin plugin)
    {
        this.name = name;
        this.permission = new Permission(permission, PermissionDefault.OP);
        this.message = null;
        this.throttleDelay = 0;
        this.plugin = plugin;
        this.enabled = false;
        this.enableByDefault = false;
        this.config = PreventionConfiguration.get(plugin.getConfigurationFolder(), this);
    }

    /**
     * Sets whether to enable this prevention by default
     *
     * @param enable true to enable it by default
     */
    public final void setEnableByDefault(boolean enable)
    {
        this.enableByDefault = enable;
    }

    /**
     * Returns whether this prevention will be enabled by default
     *
     * @return true if it will be enabled by default
     */
    public final boolean getEnableByDefault()
    {
        return this.enableByDefault;
    }

    public final PreventionConfiguration getConfig()
    {
        return this.config;
    }

    public final void resetConfig()
    {
        this.config = PreventionConfiguration.get(getPlugin().getConfigurationFolder(), this, false);
        this.saveConfig();
    }

    public final boolean reloadConfig()
    {
        try
        {
            this.config.load();
            return true;
        }
        catch (Throwable e)
        {
            AntiGuest.error(e.getLocalizedMessage(), e);
        }
        return false;
    }

    public final boolean saveConfig()
    {
        try
        {
            this.config.save();
            return true;
        }
        catch (Throwable e)
        {
            AntiGuest.error(e.getLocalizedMessage(), e);
        }
        return false;
    }

    /**
     * Generates the default configuration of this prevention.
     * This method should be overridden for custom configs.
     *
     * @return  the default config
     */
    public Configuration getDefaultConfig()
    {
        Configuration defaultConfig = new MemoryConfiguration();

        defaultConfig.set("enable", this.enableByDefault);
        defaultConfig.set("message", this.plugin.getTranslation().translate("message_" + this.name));

        return defaultConfig;
    }

    /**
     * Enables the prevention.
     * This method should be overridden for custom configs.
     *
     * @param server an Server instance
     * @param config the configuration of this prevention
     */
    public void enable()
    {
        this.messageThrottleTimestamps = new TObjectLongHashMap<Player>();
        this.throttleDelay = config.getInt("throttleDelay") * 1000;
        this.setMessage(config.getString("message"));
    }

    /**
     * Disables the prevention.
     * This method should be overridden to cleanup customized preventions
     */
    public void disable()
    {
        this.messageThrottleTimestamps.clear();
        this.messageThrottleTimestamps = null;
    }

    /**
     * Returns whether this prevention is enabled.
     *
     * @return true if this prevention is enabled
     */
    public final boolean isEnabled()
    {
        return this.enabled;
    }

    /**
     * Sets the enabled state of this prevention
     *
     * @param enable
     */
    public final void setEnabled(boolean enable)
    {
        this.enabled = enable;
    }

    /**
     * Returns the prevention's name
     * 
     * @return the name
     */
    public final String getName()
    {
        return this.name;
    }

    /**
     * Returns the prevention's permission
     *
     * @return the permission
     */
    public final Permission getPermission()
    {
        return this.permission;
    }

    /**
     * Returns the plugin corresponding to this prevention
     *
     * @return the plugin
     */
    public final PreventionPlugin getPlugin()
    {
        return this.plugin;
    }

    /**
     * Returns the message this prevention will send to players
     *
     * @return the message
     */
    public String getMessage()
    {
        return this.message;
    }

    /**
     * Sets the message this prevention will send to players
     *
     * @param message the new message
     */
    public void setMessage(String message)
    {
        this.message = parseMessage(message);
    }

    /**
     * Returns the delay this preventions uses for throttled messages
     *
     * @return the delay
     */
    public int getThrottleDelay()
    {
        return this.throttleDelay;
    }

    /**
     * Checks whether a player can pass a prevention
     *
     * @param player the player
     * @return true if the player can pass the prevention
     */
    public boolean can(final Player player)
    {
        //AntiGuest.debug("Checking permission: " + this.permission.getName());
        return player.hasPermission(this.permission);
    }

    /**
     * Sends the configured message to the player or nothing of the message is null
     * (empty string in configuration)
     *
     * @param player the player to send to
     */
    public void sendMessage(final Player player)
    {
        if (this.message != null)
        {
            player.sendMessage(this.message);
        }
    }

    /**
     * Does the same as sendMessage(Player), except that this method throttles the messages sending
     * 
     * @param player hte player to send to
     */
    public void sendThrottledMessage(final Player player)
    {
        if (this.throttleDelay < 1)
        {
            sendMessage(player);
            return;
        }

        final long next = this.messageThrottleTimestamps.get(player);
        final long current = System.currentTimeMillis();
        if (next < current)
        {
            this.sendMessage(player);
            this.messageThrottleTimestamps.put(player, current + this.throttleDelay);
        }
    }

    @Override
    public String toString()
    {
        return "Prevention{name=" + this.name + ", permission=" + this.permission.toString() + ", plugin=" + this.plugin.toString() + "}";
    }

    /**
     * This method combines can(Player) and sendMessage(Player),
     * by first checking whether player can pass the prevention and if not,
     * the given cancellable event gets cancelled and the message is sent to the
     * player.
     *
     * @param event a cancellable event
     * @param player the player
     * @return true if the action was prevented
     */
    public boolean prevent(final Cancellable event, final Player player)
    {
        if (!this.can(player))
        {
            event.setCancelled(true);
            this.sendMessage(player);
            return true;
        }
        return false;
    }

    /**
     * This method combines can(Player) and sendThrottledMessage(Player),
     * by first checking whether player can pass the prevention and if not,
     * the given cancellable event gets cancelled and the message is sent to the
     * player.
     *
     * @param event a cancellable event
     * @param player the player
     * @return true if the action was prevented
     */
    public boolean preventThrottled(final Cancellable event, final Player player)
    {
        if (!this.can(player))
        {
            event.setCancelled(true);
            this.sendThrottledMessage(player);
            return true;
        }
        return false;
    }

    /**
     * This method is a small util to parse color codes of the syntax &amp;&lt;code&gt;
     *
     * @param string hte string to parse
     * @return the parsed string
     */
    public static String parseColors(final String string)
    {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    /**
     * Parses a message
     *
     * @param message the message to parse
     * @return null if message is null or empty, otherwise the parsed message
     */
    public static String parseMessage(final String message)
    {
        if (message == null)
        {
            return null;
        }
        if (message.length() == 0)
        {
            return null;
        }
        return parseColors(message);
    }
}

