package io.totemo.lovedup;

import org.bukkit.Location;
import org.bukkit.Particle;

// ----------------------------------------------------------------------------
/**
 * Draws a particle for a duration specified in milliseconds.
 */
public class TrackedParticle {
    // ------------------------------------------------------------------------
    /**
     * Constructor.
     * 
     * @param particle the particle type.
     * @param durationMillis duration of visibility from instantiation,
     *        specified in milliseconds.
     */
    public TrackedParticle(Particle particle, long durationMillis) {
        _endTime = System.currentTimeMillis() + durationMillis;
        _particle = particle;
    }

    // ------------------------------------------------------------------------
    /**
     * Return true if this particle has expired at the specified time.
     * 
     * @param time the System.currentTimeMillis() time.
     */
    public boolean hasExpired(long time) {
        return time >= _endTime;
    }

    // ------------------------------------------------------------------------
    /**
     * Draw the particle at the specified Location.
     * 
     * @param loc the Location.
     */
    public void draw(Location loc) {
        loc.getWorld().spawnParticle(_particle, loc, 1);
    }

    // ------------------------------------------------------------------------
    /**
     * The particle to draw.
     */
    private final Particle _particle;

    /**
     * End time stamp (per System.currentTimeMillis()).
     */
    private final long _endTime;

} // class TrackedParticle