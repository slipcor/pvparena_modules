package net.slipcor.pvparena.modules.maps;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.MapInitializeEvent;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

public class MapListener implements Listener {
	@EventHandler
	public void onMapInit(MapInitializeEvent event) {
		MapView map = event.getMap();

		MapRenderer mr = new MyRenderer();
		map.addRenderer(mr);
	}
}
