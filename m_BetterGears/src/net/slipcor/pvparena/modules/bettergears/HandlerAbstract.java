package net.slipcor.pvparena.modules.bettergears;

import net.slipcor.pvparena.arena.ArenaPlayer;

public abstract class HandlerAbstract {
	
	BetterGears bg;
	
	public HandlerAbstract(BetterGears bg) {
		this.bg = bg;
	}
	
	abstract void equip(ArenaPlayer ap);
}
