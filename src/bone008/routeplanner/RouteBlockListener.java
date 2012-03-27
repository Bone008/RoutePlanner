package bone008.routeplanner;

import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

public class RouteBlockListener implements Listener {

	@EventHandler
	public void onSignChange(SignChangeEvent event) {
		if (event.getBlock().getState() instanceof Sign && RoutePlanner.getSignRouteName(event.getLines()) != null)
			POutput.print(event.getPlayer(), "Successfully created a route-sign!");
	}
	
}
