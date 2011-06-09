package bone008.routeplanner;

import org.bukkit.block.Sign;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.SignChangeEvent;

public class RouteBlockListener extends BlockListener {
/*	private final RoutePlanner plugin;
	
	public RouteBlockListener(RoutePlanner instance){
		plugin = instance;
	}*/
	
	@Override
	public void onSignChange(SignChangeEvent event){
		if(event.getBlock().getState() instanceof Sign && RoutePlanner.getSignRouteName(event.getLines()) != null)
			POutput.print(event.getPlayer(), "Successfully created a route-sign!");
	}
}
