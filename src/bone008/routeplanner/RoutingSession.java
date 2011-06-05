package bone008.routeplanner;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class RoutingSession {
	private RoutePlanner plugin;
	private Route route;
	private Player player;
	private ArrayList<TriggerRegion> passedTriggers = new ArrayList<TriggerRegion>();
	

	public RoutingSession(RoutePlanner plugin, Player player, Route route) {
		if(route == null)
			throw new NullPointerException("route equals null");
		this.plugin = plugin;
		this.route = route;
		this.player = player;
		if(!plugin.config.msg_followRoute.trim().equalsIgnoreCase("none")){
			String printStr = plugin.config.msg_followRoute.replace("<routename>", ChatColor.GREEN+route.getName()+ChatColor.WHITE);
			POutput.print(player, printStr);
		}
		POutput.print(player, route.getIntroMessage());
	}
	
	
	public TriggerRegion matchTrigger(Location l){
		for(TriggerRegion tR: route.getTriggerRegions()){
			if(!passedTriggers.contains(tR) && tR.hitTest(l)){
				passedTriggers.add(tR);
				if(route.getTargetTrigger() == tR){
					targetReached();
				}
				return tR;
			}
		}
		return null;
	}
	
	private void targetReached(){
		if(!plugin.config.msg_finishRoute.trim().equalsIgnoreCase("none"))
			POutput.print(player, plugin.config.msg_finishRoute);
		plugin.routingSessions.remove(player);
	}
}
