package bone008.routeplanner;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class RoutePlayerListener implements Listener {
	private final RoutePlanner plugin;
	
	public RoutePlayerListener(RoutePlanner instance){
		plugin = instance;
	}
	
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerInteract(PlayerInteractEvent event){
		if(event.isCancelled()) return;
		// check for signs
		if(event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock().getState() instanceof Sign){
			Sign s = (Sign)event.getClickedBlock().getState();
			String sRName = RoutePlanner.getSignRouteName(s);
			if(sRName != null)
				event.getPlayer().performCommand("route "+sRName);
		}
		
		
		// check for region selection
		
		// if use of WorldEdit's regions enabled -> return
		if(plugin.worldEdit != null && plugin.config.useWorldEdit)
			return;
		
		
		if(event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_BLOCK){
			Player player = event.getPlayer();
			
			CreatingSession session;
			if	(player.getItemInHand().getTypeId() == plugin.config.triggerSelectionItem &&
				 (session = plugin.creatingSessions.get(player)) != null &&
				 player.hasPermission(RoutePlanner.PERMISSON_ADMIN)){
					int index = (event.getAction()==Action.LEFT_CLICK_BLOCK ? 0 : 1);
					Block clickedBlock = event.getClickedBlock();
					session.selection[index] = clickedBlock;
					POutput.print(player,"Position "+(index+1)+" was set to "+clickedBlock.getX()+"/"+clickedBlock.getY()+"/"+clickedBlock.getZ()+"!");
					event.setCancelled(true);
			}
		}
	}
	
	
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event){
		if(plugin.routingSessions.get(event.getPlayer()) == null)
			return;
		
		// check if move changed the block
		if(event.getFrom().getBlock().equals(event.getTo().getBlock()))
			return;
		
		
		Location from = event.getFrom();
		Location to = event.getTo();
		Player player = event.getPlayer();
		RoutingSession session = plugin.routingSessions.get(player);
		
		TriggerRegion matchingTrigger = session.matchTrigger(to);
		if(matchingTrigger != null && matchingTrigger != session.matchTrigger(from)){
			POutput.print(player, matchingTrigger.getTriggerMessage());
		}
		
	}
	
}
