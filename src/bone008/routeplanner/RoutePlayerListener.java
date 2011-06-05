package bone008.routeplanner;



import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;

public class RoutePlayerListener extends PlayerListener{
	private final RoutePlanner plugin;
	
	public RoutePlayerListener(RoutePlanner instance){
		plugin = instance;
	}
	
	
	@Override
	public void onPlayerInteract(PlayerInteractEvent event){
		if(event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_BLOCK){
			Player player = event.getPlayer();
			
			CreatingSession session;
			if	(player.getItemInHand().getTypeId() == plugin.config.triggerSelectionItem &&
				 (session = plugin.creatingSessions.get(player)) != null &&
				 plugin.hasPermission(player, RoutePlanner.PERMISSON_ADMIN)){
					int index = (event.getAction()==Action.LEFT_CLICK_BLOCK ? 0 : 1);
					Block clickedBlock = event.getClickedBlock();
					session.selection[index] = clickedBlock;
					POutput.print(player,"Position "+(index+1)+" was set to "+clickedBlock.getX()+"/"+clickedBlock.getY()+"/"+clickedBlock.getZ()+"!");
			}
		}
	}
	
	
	
	@Override
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
