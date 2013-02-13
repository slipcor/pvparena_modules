package net.slipcor.pvparena.modules.turrets;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LargeFireball;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.SmallFireball;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.WitherSkull;

import net.slipcor.pvparena.classes.PALocation;

public class Turret {
	private final double yaw;
	private final double offset;
	private static Map<String, Class<? extends Projectile>> types = new HashMap<String, Class<? extends Projectile>>();
	final private Class<? extends Projectile> type;
	
	static {
		types.put("fire", Fireball.class);
		types.put("snow", Snowball.class);
		types.put("large", LargeFireball.class);
		types.put("small", SmallFireball.class);
		types.put("wither", WitherSkull.class);
	}

	public Turret(final String name, final PALocation loc, final double offset) {
		yaw = loc.getYaw();
		this.offset = offset < 10 ? 10 : offset; // safety. less than 10 degrees is nonsense :p
		for (String string : types.keySet()) {
			if (name.contains(string)) {
				type = types.get(string);
				return;
			}
		}
		type = Arrow.class;
	}

	protected Class<? extends Projectile> getType() {
		return type;
	}

	public boolean cancelMovement(final float oldYaw) {
		float yaw = oldYaw;
		// yaw : 0-360

		yaw -= this.yaw; // diff; -360>diff>360
		
		yaw = Math.abs(yaw); // abs; 0>=diff>360
		
		if (yaw > 180) {
			// if we have greater difference than 180 deg
			// => swap diff, because we get nearer again
			yaw = 360 - yaw; // 180 results in 180, 360 results in 0
		}
		
		// yaw now is the absolute degree difference from this to that
		
		return yaw > offset;
	}

	public float getYaw() {
		return (float) (yaw);
	}
}
