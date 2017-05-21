package io.totemo.lovedup;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

// ----------------------------------------------------------------------------
/**
 * Main plugin class.
 */
public class LovedUp extends JavaPlugin implements Listener {
    /**
     * Configuration instance.
     */
    public static Configuration CONFIG;

    // ------------------------------------------------------------------------
    /**
     * @see org.bukkit.plugin.java.JavaPlugin#onCommand(org.bukkit.command.CommandSender,
     *      org.bukkit.command.Command, java.lang.String, java.lang.String[])
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("lovedup")) {
            if (args.length == 1 && args[0].equalsIgnoreCase("help")) {
                // Let Bukkit show usage help from plugin.yml.
                return false;
            }

            if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
                CONFIG.reload();
                sender.sendMessage(ChatColor.LIGHT_PURPLE + getName() + " configuration reloaded.");
            }
            return true;

        } else if (command.getName().equalsIgnoreCase("loveall")) {
            cmdLoveAll(sender);
            return true;

        } else if (command.getName().equalsIgnoreCase("sparkall")) {
            cmdSparkAll(sender, args);
            return true;

        } else if (command.getName().equalsIgnoreCase("love")) {
            cmdLove(sender, args);
            return true;
            
        } else if (command.getName().equalsIgnoreCase("hate")) {
        	cmdHate(sender, args);
        	return true;

        } else if (command.getName().equalsIgnoreCase("unlovable")) {
            cmdUnlovable(sender);
            return true;
        
        } else if (command.getName().equalsIgnoreCase("unhateable")) {
        	cmdUnhateable(sender);
        	return true;
        }

        sender.sendMessage(ChatColor.RED + "Invalid command syntax.");
        return false;
    } // onCommand

    // ------------------------------------------------------------------------
    /**
     * @see org.bukkit.plugin.java.JavaPlugin#onEnable()
     */
    @Override
    public void onEnable() {
        saveDefaultConfig();
        CONFIG = new Configuration(this);
        CONFIG.reload();

        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                showEffects(_trackedProjectiles, 0.3, 0, 64, false);
            }
        }, 1, 1);

        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                showEffects(_trackedLiving, 1.25, 1.0, 32, true);
            }
        }, 2, 2);

        Bukkit.getPluginManager().registerEvents(this, this);
    } // onEnable

    // ------------------------------------------------------------------------
    /**
     * @see org.bukkit.plugin.java.JavaPlugin#onDisable()
     */
    @Override
    public void onDisable() {
        Bukkit.getScheduler().cancelTasks(this);
        CONFIG.save();
    }

    // ------------------------------------------------------------------------
    /**
     * When a projectile is launched, add it to the tracked projectiles.
     */
    @EventHandler(ignoreCancelled = true)
    protected void onProjectileLaunch(ProjectileLaunchEvent event) {
        Projectile projectile = event.getEntity();
        if (CONFIG.ENABLE_ARROW_EFFECTS && projectile instanceof Arrow ||
            CONFIG.ENABLE_OTHER_PROJECTILE_EFFECTS && !(projectile instanceof Arrow)) {
            long endTime = System.currentTimeMillis() + CONFIG.PROJECTILE_MS;
            TrackedEffect tEffect = new TrackedEffect(endTime, Effect.HEART, 0);
            _trackedProjectiles.put(projectile, tEffect);
            
        }
    }

    // ------------------------------------------------------------------------
    /**
     * When a projectile hits, remove it from tracking.
     */
    @EventHandler(ignoreCancelled = true)
    protected void onProjectileHit(ProjectileHitEvent event) {
        Projectile projectile = event.getEntity();
        _trackedProjectiles.remove(projectile);
    }

    // ------------------------------------------------------------------------
    /**
     * When a player shoots another player, treat that the same as if the
     * shooter ran /love <victim>.
     *
     * This event is registered as the lowest priority (handled first) and also
     * ignores cancellation of the other event by other plugins, so that it is
     * guaranteed to see things happen even if they are cancelled by another
     * plugin, e.g. WorldGuard.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    protected void onPlayerShootPlayer(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Projectile) {
            Projectile projectile = (Projectile) event.getDamager();
            if (projectile instanceof EnderPearl) {
                return;
            }

            if (CONFIG.ONLY_ARROWS_LOVE && !(projectile instanceof Arrow)) {
                return;
            }

            if (!CONFIG.ENABLE_ARROW_EFFECTS && projectile instanceof Arrow) {
                return;
            }

            if (!CONFIG.ENABLE_OTHER_PROJECTILE_EFFECTS && !(projectile instanceof Arrow)) {
                return;
            }

            if (projectile.getShooter() instanceof Player) {
                Player attacker = (Player) projectile.getShooter();
                Player victim = (Player) event.getEntity();
                sendLove(attacker, victim);
            }
        }
    } // onPlayerShootPlayer

    // ------------------------------------------------------------------------
    /**
     * Called when a mob targets or untargets a player or mob.
     *
     * Handle with highest priority so that it will already be cancelled if the
     * player is in ModMode.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    protected void onEntityTarget(EntityTargetEvent event) {
        if (event.getEntity() instanceof LivingEntity) {
            LivingEntity attacker = (LivingEntity) event.getEntity();
            if (event.getReason() == TargetReason.FORGOT_TARGET) {
                _trackedLiving.remove(attacker);
            } else {
                long endTime = System.currentTimeMillis() + CONFIG.MOB_MS;
                TrackedEffect tEffect = new TrackedEffect(endTime, Effect.HEART, 0);
                _trackedLiving.put(attacker, tEffect);
            }
        }
    }

    // ------------------------------------------------------------------------
    /**
     * Cancel damage to entities by firework entities that have been tagged with
     * this plugin's metadata value.
     */
    @EventHandler(ignoreCancelled = true)
    protected void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Firework &&
            event.getDamager().getMetadata(META_KEY).size() != 0) {
            event.setCancelled(true);
        }
    }

    // ------------------------------------------------------------------------
    /**
     * Handle the /love [<player>] command.
     *
     * @param sender command sender.
     * @param args command arguments.
     */
    protected void cmdLove(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.LIGHT_PURPLE
                               + "Usage: /love <player> - Send your love, anonymously to a player.");

        } else if (args.length == 1) {
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Me too, babe, but that player isn't online.");
            } else {
                if (sender instanceof Player) {
                    sendLove((Player) sender, target);
                }
            }

        } else {
            sender.sendMessage(ChatColor.LIGHT_PURPLE + "Whoa! Slow down there Romeo!");
        }
    } // cmdLove
    
    // ------------------------------------------------------------------------
    /**
     * Handle the /hate [<player>] command.
     *
     * @param sender command sender.
     * @param args command arguments.
     */
    protected void cmdHate(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.LIGHT_PURPLE
                               + "Usage: /hate <player> - Send your hate, anonymously to a player.");

        } else if (args.length == 1) {
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Me too, killer, but that player isn't online.");
            } else {
                if (sender instanceof Player) {
                    sendHate((Player) sender, target);
                }
            }

        } else {
            sender.sendMessage(ChatColor.LIGHT_PURPLE + "Whoa! Slow down there Romeo!");
        }
    } // cmdHate

    // --------------------------------------------------------------------------
    /**
     * Handle the /lovall command.
     *
     * @param sender command sender.
     */
    protected void cmdLoveAll(CommandSender sender) {
        long endTime = System.currentTimeMillis() + CONFIG.PLAYER_MS;
        TrackedEffect tEffect = new TrackedEffect(endTime, Effect.HEART, 0);
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!_unlovable.contains(player.getName())) {
                _trackedLiving.put(player, tEffect);
            }
        }
        sender.sendMessage(ChatColor.LIGHT_PURPLE + "Everybody got some!");
    }

    // ------------------------------------------------------------------------
    /**
     * Handle the /sparkall [<message>] command.
     *
     * @param sender command sender.
     * @param args command arguments.
     */
    protected void cmdSparkAll(CommandSender sender, String[] args) {
        StringBuilder msg = new StringBuilder("&d");
        String sep = "";
        for (String arg : args) {
            msg.append(sep);
            msg.append(arg);
            sep = " ";
        }
        String message = ChatColor.translateAlternateColorCodes('&', msg.toString());

        long endTime = System.currentTimeMillis() + CONFIG.PLAYER_MS;
        TrackedEffect tEffect = new TrackedEffect(endTime, Effect.HEART, 0);
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!_unlovable.contains(player.getName())) {
                _trackedLiving.put(player, tEffect);
                spawnFirework(player);
                if (args.length != 0) {
                    player.sendMessage(message);
                }
            }
        }

        if (args.length != 0) {
            sender.sendMessage(ChatColor.LIGHT_PURPLE + "Sent sparks to all and the message: " + message);
        } else {
            sender.sendMessage(ChatColor.LIGHT_PURPLE + "Sent sparks to all!");
        }
    } // cmdSparkAll

    // ------------------------------------------------------------------------
    /**
     * Handle the /unlovable command.
     *
     * Mark the command sender as immune to hearts and fireworks.
     *
     * @param sender the command sender.
     */
    private void cmdUnlovable(CommandSender sender) {
        String name = sender.getName();
        if (_unlovable.contains(name)) {
            sender.sendMessage(ChatColor.LIGHT_PURPLE + "You open your heart to others!");
            _unlovable.remove(name);
        } else {
            sender.sendMessage(ChatColor.LIGHT_PURPLE + "Your heart is as cold and dead as stone!");
            _unlovable.add(name);
        }
    }
    
    // ------------------------------------------------------------------------
    /**
     * Handle the /unhateable command.
     *
     * Mark the command sender as immune to hate and evil.
     *
     * @param sender the command sender.
     */
    private void cmdUnhateable(CommandSender sender) {
        String name = sender.getName();
        if (_unhateable.contains(name)) {
            sender.sendMessage(ChatColor.LIGHT_PURPLE + "Revenge is a dish best served cold!");
            _unhateable.remove(name);
        } else {
            sender.sendMessage(ChatColor.LIGHT_PURPLE + "You turn the other cheek!");
            _unhateable.add(name);
        }
    }

    // ------------------------------------------------------------------------
    /**
     * Send love from the sender to the receiver.
     *
     * The receiver will not show particles or fireworks if they are exempt, or
     * unlovable and in the event that they are unlovable, the sender will be
     * informed.
     *
     * @param sender player sending love.
     * @param receiver player receiveing love.
     */
    protected void sendLove(Player sender, Player receiver) {
        if (_unlovable.contains(receiver.getName())) {
            sender.sendMessage(ChatColor.RED + receiver.getName() + " is unlovable. :(");
        } else {
            if (!receiver.hasPermission("lovedup.exempt")) {
                long endTime = System.currentTimeMillis() + CONFIG.PLAYER_MS;
                TrackedEffect tEffect = new TrackedEffect(endTime, Effect.HEART, 0);
                _trackedLiving.put(receiver, tEffect);
            }
            sender.sendMessage(ChatColor.LIGHT_PURPLE + receiver.getName() + " is loved.");
            checkMatch(sender.getName(), receiver.getName());
        }
    }
    
    // ------------------------------------------------------------------------
    /**
     * Send hate from the sender to the receiver.
     *
     * The receiver will not show particles or fireworks if they are exempt, or
     * unhateable and in the event that they are unhateable, the sender will be
     * informed.
     *
     * @param sender player sending hate.
     * @param receiver player receiving hate.
     */
    protected void sendHate(Player sender, Player receiver) {
        if (_unhateable.contains(receiver.getName())) {
            sender.sendMessage(ChatColor.RED + receiver.getName() + " is unhateable. :D");
        } else {
            if (!receiver.hasPermission("lovedup.exempt")) {
                long endTime = System.currentTimeMillis() + CONFIG.PLAYER_MS;
                TrackedEffect tEffect = new TrackedEffect(endTime, Effect.VILLAGER_THUNDERCLOUD, 0);
                _trackedLiving.put(receiver, tEffect);
            }
            sender.sendMessage(ChatColor.LIGHT_PURPLE + receiver.getName() + " is hated.");
            checkNemesis(sender.getName(), receiver.getName());
        }
    }

    // ------------------------------------------------------------------------
    /**
     * If sender has /love'd recipient and vice versa, then do a firework for
     * each and notify each player that they matched.
     */
    protected void checkMatch(String sender, String receiver) {
        if (!CONFIG.SELF_MATCH) {
            if (sender.equals(receiver)) {
                // You can't match with yourself.
                return;
            }
        }

        if (admires(receiver, sender)) {
            // Instant match. No need to record admiration.
            spawnFireworkFrom(sender, Bukkit.getPlayerExact(receiver));
            if (!sender.equals(receiver)) {
                spawnFireworkFrom(receiver, Bukkit.getPlayerExact(sender));
            }

            removeAdmiration(sender, receiver);
            removeAdmiration(receiver, sender);

        } else {
            // Add sender to the set of receiver's admirers.
            HashSet<String> admirers = _admirers.get(receiver);
            if (admirers == null) {
                admirers = new HashSet<String>();
                _admirers.put(receiver, admirers);
            }
            admirers.add(sender);
        }
    } // checkMatch
    
    // ------------------------------------------------------------------------
    /**
     * If sender has /hated'd recipient and vice versa, then do a evil sound for
     * each and notify each player that they are arch nemesies.
     */
    protected void checkNemesis(String sender, String receiver) {
        if (!CONFIG.SELF_MATCH) {
            if (sender.equals(receiver)) {
                // You can't match with yourself.
                return;
            }
        }

        if (hates(receiver, sender)) {
            // Instant match. No need to record nemesis.
            Player player = Bukkit.getPlayerExact(receiver);
            long endTime = System.currentTimeMillis() + CONFIG.PLAYER_MS;
            TrackedEffect tEffect = new TrackedEffect(endTime, Effect.FLAME, 0);
            
            if(player != null) {
            	player.getLocation().getWorld().playEffect(player.getLocation(), Effect.ENDERDRAGON_GROWL, 0);
            	_trackedLiving.put(player, tEffect);
            }
            
            if (!sender.equals(receiver)) {
                player = Bukkit.getPlayerExact(sender);
                if(player != null) {
                	player.getLocation().getWorld().playEffect(player.getLocation(), Effect.ENDERDRAGON_GROWL, 0);
                	_trackedLiving.put(player, tEffect);
                }
            }

            removeHatred(sender, receiver);
            removeHatred(receiver, sender);

        } else {
            // Add sender to the set of receiver's admirers.
            HashSet<String> nemesies = _nemesies.get(receiver);
            if (nemesies == null) {
            	nemesies = new HashSet<String>();
                _nemesies.put(receiver, nemesies);
            }
            nemesies.add(sender);
        }
    } // checkNemesis

    // ------------------------------------------------------------------------
    /**
     * Return true if subject admires object.
     *
     * @param subject the player name of the subject in question.
     * @param object the name of the player that may or may not be admired.
     *
     * @return true if subject admires object.
     */
    protected boolean admires(String subject, String object) {
        HashSet<String> admirers = _admirers.get(object);
        return admirers != null && admirers.contains(subject);
    }
    
    // ------------------------------------------------------------------------
    /**
     * Return true if subject hates object.
     *
     * @param subject the player name of the subject in question.
     * @param object the name of the player that may or may not be hated.
     *
     * @return true if subject hates object.
     */
    protected boolean hates(String subject, String object) {
        HashSet<String> nemesies = _nemesies.get(object);
        return nemesies != null && nemesies.contains(subject);
    }

    // ------------------------------------------------------------------------
    /**
     * Remove subject's admiration of object.
     *
     * @param subject the player name of the subject in question.
     * @param object the name of the player that is admired.
     */
    protected void removeAdmiration(String subject, String object) {
        HashSet<String> admirers = _admirers.get(object);
        if (admirers != null) {
            admirers.remove(subject);
        }
    }
    
    // ------------------------------------------------------------------------
    /**
     * Remove subject's hatred of object.
     *
     * @param subject the player name of the subject in question.
     * @param object the name of the player that is hated.
     */
    protected void removeHatred(String subject, String object) {
        HashSet<String> nemesies = _nemesies.get(object);
        if (nemesies != null) {
        	nemesies.remove(subject);
        }
    }

    // ------------------------------------------------------------------------
    /**
     * Spawn a small random firework at the specified player and notify them of
     * the sender.
     *
     * @param sender the exact player name of the sender.
     * @param recipient the recipient of the message and firework; may be null
     *        if offline.
     */
    protected void spawnFireworkFrom(String sender, Player recipient) {
        if (recipient == null || recipient.hasPermission("lovedup.exempt")) {
            return;
        }

        recipient.sendMessage(ChatColor.LIGHT_PURPLE + sender + " loves you!");
        spawnFirework(recipient);
    }

    // ------------------------------------------------------------------------
    /**
     * Spawn a firework at the recipient.
     *
     * The caller must ensure that the recipient is non-null and does not have
     * the "lovedup.exempt" permission.
     *
     * @param recipient player whose location is used as the origin of the
     *        spawned firework.
     */
    protected void spawnFirework(Player recipient) {
        Location origin = recipient.getLocation();
        World world = origin.getWorld();
        Firework firework = (Firework) world.spawnEntity(origin, EntityType.FIREWORK);
        if (firework != null) {
            firework.setMetadata(META_KEY, _metaValue);
            FireworkEffect.Builder builder = FireworkEffect.builder();
            if (Math.random() < 0.3) {
                builder.withFlicker();
            }
            if (Math.random() < 0.3) {
                builder.withTrail();
            }

            builder.with(FIREWORK_TYPES[_random.nextInt(FIREWORK_TYPES.length)]);
            builder.withColor(Color.fromRGB(255, _random.nextInt(64), _random.nextInt(64)));
            final int primaryColors = 1 + _random.nextInt(4);
            for (int i = 0; i < primaryColors; ++i) {
                builder.withColor(Color.fromRGB(255, 128 + _random.nextInt(64), 128 + _random.nextInt(64)));
            }

            builder.withFade(Color.fromRGB(255, 255, 255));

            FireworkMeta meta = firework.getFireworkMeta();
            meta.setPower(_random.nextInt(2));
            meta.addEffect(builder.build());
            firework.setFireworkMeta(meta);
        }
    } // spawnFirework

    // ------------------------------------------------------------------------
    /**
     * Show hearts around all members of a set of Entities.
     *
     * Remove Entities from the set when they are no longer valid (i.e. when the
     * Entity no longer exists in the world).
     *
     * @param targets set of instances of some Entity subtype. Note that the set
     *        is mutated by this method.
     * @param wobble amplitude of the random jitter of the heart effect position
     *        in blocks.
     * @param yBias additional fixed Y coordinate offset.
     * @param radius radius of visibility of the effect in blocks.
     * @param checkExempt if true, the entities are checked for exempted players
     *        who will not show hearts.
     */
    protected <T extends Entity> void showEffects(HashMap<T, TrackedEffect> targets, double wobble,
                                           double yBias, int radius, boolean checkExempt) {
        long now = System.currentTimeMillis();
        Iterator<Entry<T, TrackedEffect>> itr = targets.entrySet().iterator();
        while(itr.hasNext()) {
        	Entry<T, TrackedEffect> entry = itr.next();
            T entity = entry.getKey();
            TrackedEffect tEffect = entry.getValue();
            if (now >= tEffect.ttl) {
            	itr.remove();
            } else if (entity.isValid()) {
                if (checkExempt && entity instanceof Player) {
                    Player player = (Player) entity;
                    if (player.hasPermission("lovedup.exempt")) {
                    	itr.remove();
                        continue;
                    }
                }
                Location loc = entity.getLocation();
                loc.add(scaledRandom(wobble), yBias + scaledRandom(wobble), scaledRandom(wobble));
                loc.getWorld().playEffect(loc, tEffect.effect, tEffect.data, radius);

            } else {
            	itr.remove();
            }
        }
    } // showEffects

    // ------------------------------------------------------------------------
    /**
     * Return a random number in the range [-amplitude, +amplitude].
     *
     * @param amplitude the maximum positive or negative value.
     * @return a random number in the range [-amplitude, +amplitude].
     */
    protected double scaledRandom(double amplitude) {
        double random = _random.nextDouble();
        return (random - 0.5) * 2 * amplitude;
    }

    // ------------------------------------------------------------------------
    /**

     * Firework types.
     */
    protected static final FireworkEffect.Type[] FIREWORK_TYPES = { Type.BALL, Type.BALL_LARGE, Type.STAR, Type.BURST };

    /**
     * Metadata key applied to fireworks, used to prevent LovedUp fireworks from
     * damaging players.
     */
    protected static final String META_KEY = "LovedUp_FW";

    /**
     * Shared metadata value.
     */
    protected MetadataValue _metaValue = new FixedMetadataValue(this, null);

    /**
     * Random number generator.
     */
    protected Random _random = new Random();

    /**
     * Map tracked projectile to end time of hearts effect.
     */
    protected HashMap<Projectile, TrackedEffect> _trackedProjectiles = new HashMap<Projectile, TrackedEffect>();

    /**
     * Map tracked living entities (players, mobs) to end time of hearts effect.
     */
    protected HashMap<LivingEntity, TrackedEffect> _trackedLiving = new HashMap<LivingEntity, TrackedEffect>();

    /**
     * For each love recipient name (key), this map records the name of those
     * players who sent love to them, until such time as there is a match, when
     * the corresponding entries are removed.
     */
    protected HashMap<String, HashSet<String>> _admirers = new HashMap<String, HashSet<String>>();
    
    /**
     * For each hate recipient name (key), this map records the name of those
     * players who sent hate to them, until such time as there is a match, when
     * the corresponding entries are removed.
     */
    protected HashMap<String, HashSet<String>> _nemesies = new HashMap<String, HashSet<String>>();

    /**
     * Set of unlovable player names. They don't see hearts or fireworks.
     */
    protected HashSet<String> _unlovable = new HashSet<String>();
    
    /**
     * Set of unhateable player names. They don't see hate.
     */
    protected HashSet<String> _unhateable = new HashSet<String>();
    
    /**
     * A structure to store effect data
     * 
     */
    private class TrackedEffect {
    	/**
    	 * Time to live
    	 */
    	public long ttl;
    	
    	/**
    	 * The effect to display
    	 */
    	public Effect effect;
    	
    	/**
    	 * a data bit needed for some effects.
    	 */
    	public int data;
    	
    	/**
    	 * Convenience constructor
    	 */
    	public TrackedEffect(long ttl, Effect effect, int data) {
    		this.ttl = ttl;
    		this.effect = effect;
    		this.data = data;
    	}
    }

} // class LovedUp
