package bone008.routeplanner;



import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.bukkit.selections.Selection;

public class RoutePlannerCommand implements CommandExecutor{
	private final RoutePlanner plugin;
	
	public RoutePlannerCommand(RoutePlanner instance){
		plugin = instance;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] split) {
		if(!(sender instanceof Player))
			return true;
		Player player = (Player) sender;
		
		// check arguments and display basic usage-message (return false) 
		if(split.length < 1)
			return false;
		
				
		
		if(plugin.hasPermission(player, RoutePlanner.PERMISSON_ADMIN)){
			
			// route create <routename>
			if(split[0].equalsIgnoreCase("create"))
				return rCreate(player,split);
			
			// route edit <routename>
			if(split[0].equalsIgnoreCase("edit"))
				return rEdit(player,split);
			
			// route remove <routename>
			if(split[0].equalsIgnoreCase("remove"))
				return rRemove(player,split);
			
			// route discard
			if(split[0].equalsIgnoreCase("discard"))
				return rDiscard(player,split);
			
			// route intro <intro-message>
			if(split[0].equalsIgnoreCase("intro"))
				return rIntro(player,split);
			
			// route addtrigger <trigger-text>
			if(split[0].equalsIgnoreCase("addtrigger"))
				return rAddTrigger(player,split);
			
			// route remtrigger <trigger-id>
			if(split[0].equalsIgnoreCase("remtrigger"))
				return rRemoveTrigger(player,split);
			
			// route settarget <trigger-id>
			if(split[0].equalsIgnoreCase("settarget"))
				return rSetTarget(player,split);
			
			// route info
			if(split[0].equalsIgnoreCase("info"))
				return rInfo(player,split);
			
			// route save
			if(split[0].equalsIgnoreCase("save"))
				return rSave(player,split);
			
		}

		
		
		if(plugin.hasPermission(player, RoutePlanner.PERMISSON_BASIC)){
			
			// route list
			if(split[0].equalsIgnoreCase("list"))
				return rList(player);
			
			// route cancel
			if(split[0].equalsIgnoreCase("cancel"))
				return rCancel(player);
			
			// route help
			if(split[0].equalsIgnoreCase("help")){
				displayHelp(player);
				return true;
			}
			
			
			// ======== default user starting a new route ============
			
			if(!plugin.hasPermission(player, RoutePlanner.PERMISSON_BASIC))
				return true;
			if(plugin.routingSessions.get(player) != null){
				POutput.printError(player, RoutePlanner.ERROR_ALREADY_RUNNING);
				return true;
			}
			
			Route route = plugin.getRoute(split[0]);
			if(route == null || !route.isValid()){
				POutput.printError(player, RoutePlanner.ERROR_NOT_EXISTS);
				return true;
			}
			
			plugin.routingSessions.put(player, new RoutingSession(plugin, player, route));
			
		}
		
		return true;
	}

	
	
	

	// ============== User commands ========================== 
	
	
	
	private void displayHelp(Player player){
		POutput.print(player, ChatColor.GRAY+"Available commands [Version "+plugin.pdfFile.getVersion()+"]:");
		
		String[] helpList = {
			ChatColor.GOLD+"Note: /rp is an alias for /route",
			c("/rp help")+" displays this help",
			c("/rp <routename>")+" follows the specified route",
			c("/rp list")+" lists all available routes",
			c("/rp cancel")+" cancels the route you are following"
		};
		POutput.print(player, helpList, false);
		
		if(plugin.hasPermission(player, RoutePlanner.PERMISSON_ADMIN)){
			String[] adminHelp = {
				ChatColor.GRAY+"-- Admin Commands --",
				c("/rp create <routename>")+" starts the creation of a new route",
				c("/rp edit <routename>")+" starts editing an existing route",
				c("/rp remove <routename>")+" removes an existing route",
				c("/rp discard")+" cancels the current creation/editing process",
				c("/rp intro <intro-message>")+" sets the intro message",
				c("/rp addtrigger <trigger-text>")+" adds a trigger",
				c("/rp remtrigger <trigger-id>")+" removes an existing trigger",
				c("/rp settarget <trigger-id>")+" sets the route's target",
				c("/rp info")+" displays the current state of the creating route",
				c("/rp save")+" saves the route and ends the creation"
			};
			POutput.print(player, adminHelp, false);
		}
	}
	
	private String c(String s){
		return ChatColor.BLUE+s+ChatColor.WHITE;
	}
	
	
	
	private boolean rCancel(Player player) {
		if(plugin.routingSessions.remove(player) == null)
			POutput.printError(player, RoutePlanner.ERROR_NOT_RUNNING);
		else
			POutput.print(player, "Your current route was canceled!");
		return true;
	}
	
	
	private boolean rList(Player player){
		StringBuilder names = new StringBuilder();
		String splitter = ", ";
		for(Route r: plugin.routes.values()){
			names.append(splitter).append(r.getName());
		}
		POutput.print(player, "Available routes:");
		if(plugin.routes.size() > 0)
			POutput.print(player, "  "+names.substring(splitter.length()), false);
		else
			POutput.print(player, "  "+ChatColor.RED+"none", false);
		return true;
	}
	
	
	
	// ============== Admin commands =========================
	
	
	private boolean rEdit(Player player, String[] split){
		// check usage (if routename is given)
		if(split.length < 2){
			POutput.printUsage(player, RoutePlanner.USAGE_EDIT);
			return true;
		}
		// get routename
		String routename = split[1];
		
		// check if player is already creating
		if(getCreatingSession(player) != null){
			POutput.printError(player, RoutePlanner.ERROR_ALREADY_CREATING);
			return true;
		}
		
		Route route2edit = plugin.getRoute(routename);
		// check if route exists
		if(route2edit == null){
			POutput.printError(player, RoutePlanner.ERROR_NOT_EXISTS);
			return true;
		}
		
		// check if player is allowed to alter the route
		if(!plugin.hasPermission(player, RoutePlanner.PERMISSON_ALTEROTHER) && !route2edit.getCreator().equals(player.getName())){
			POutput.printError(player, RoutePlanner.ERROR_NO_PERMISSION_ALTEROTHER);
			return true;
		}
		
		POutput.print(player,"Editing route "+ChatColor.GREEN+route2edit.getName());
		plugin.creatingSessions.put(player, new CreatingSession(route2edit));
		
		return true;
	}
	
	
	private boolean rCreate(Player player, String[] split){
		// check usage (if routename is given)
		if(split.length < 2){
			POutput.printUsage(player, RoutePlanner.USAGE_CREATE);
			return true;
		}
		// get routename
		String routename = split[1];
		
		// check if player is already creating
		if(getCreatingSession(player) != null){
			POutput.printError(player, RoutePlanner.ERROR_ALREADY_CREATING);
			return true;
		}
		
		// check if route already exists
		if(plugin.routes.get(routename.toLowerCase()) != null){
			POutput.printError(player, RoutePlanner.ERROR_ALREADY_EXISTS);
			return true;
		}
		
		POutput.print(player,"Starting creation of route "+ChatColor.GREEN+routename);
		plugin.creatingSessions.put(player, new CreatingSession(routename));
		
		return true;
	}
	
	
	private boolean rRemove(Player player, String[] split){
		// check usage (if routename is given)
		if(split.length < 2){
			POutput.printUsage(player, RoutePlanner.USAGE_REMOVE);
			return true;
		}
		// get routename
		String routename = split[1];
		
		Route route2remove = plugin.getRoute(routename);
		// check if route exists
		if(route2remove == null){
			POutput.printError(player, RoutePlanner.ERROR_NOT_EXISTS);
			return true;
		}
		
		if(!plugin.hasPermission(player, RoutePlanner.PERMISSON_ALTEROTHER) && !route2remove.getCreator().equals(player.getName())){
			POutput.printError(player, RoutePlanner.ERROR_NO_PERMISSION_ALTEROTHER);
			return true;
		}
		
		plugin.removeRoute(route2remove.getName());
		POutput.print(player, "The route was removed successfully!");
		plugin.loadRoutes();
		
		return true;
	}
	
	
	private boolean rDiscard(Player player, String[] split){
		CreatingSession session = getCreatingSession(player);
		// check if player is creating
		if(session == null){
			POutput.printError(player, RoutePlanner.ERROR_NOT_CREATING);
			return true;
		}
		
		plugin.creatingSessions.remove(player);
		POutput.print(player, "The route you were editing was discarded!");
		return true;
	}
	
	
	private boolean rIntro(Player player, String[] split){
		// check usage (if message is given)
		if(split.length < 2){
			POutput.printUsage(player, RoutePlanner.USAGE_INTRO);
			return true;
		}
		
		CreatingSession session = getCreatingSession(player);
		// check if player is creating
		if(session == null){
			POutput.printError(player, RoutePlanner.ERROR_NOT_CREATING);
			return true;
		}
		
		// builds the message out of the params
		StringBuilder introMsg = new StringBuilder();
		for(int i=1; i<split.length; i++){
			introMsg.append(split[i]);
			if(i+1 < split.length) introMsg.append(" ");
		}
		
		session.setIntroMessage(introMsg.toString());
		
		POutput.print(player,"The intro-message was set!");
		return true;
	}
	
	
	
	private boolean rAddTrigger(Player player, String[] split){
		// check usage (if message is given)
		if(split.length < 2){
			POutput.printUsage(player, RoutePlanner.USAGE_ADDTRIGGER);
			return true;
		}
		
		CreatingSession session = getCreatingSession(player);
		// check if player is creating
		if(session == null){
			POutput.printError(player, RoutePlanner.ERROR_NOT_CREATING);
			return true;
		}
		
		// builds the message out of the params
		StringBuilder triggerMsg = new StringBuilder();
		for(int i=1; i<split.length; i++){
			triggerMsg.append(split[i]);
			if(i+1 < split.length) triggerMsg.append(" ");
		}
		
		
		// get the player selection (based on WorldEdit or native method)
		Block sel_pos1 = null;
		Block sel_pos2 = null;
		if(plugin.worldEdit != null && plugin.config.useWorldEdit){
			Selection weSel = plugin.worldEdit.getSelection(player);
			if(weSel != null){
				sel_pos1 = weSel.getWorld().getBlockAt(weSel.getMinimumPoint());
				sel_pos2 = weSel.getWorld().getBlockAt(weSel.getMaximumPoint());
			}
		} else{
			sel_pos1 = session.selection[0];
			sel_pos2 = session.selection[1];
		}
		
		if(sel_pos1 == null || sel_pos2 == null){
			POutput.printError(player, RoutePlanner.ERROR_NO_SELECTION);
			return true;
		}
		
		try{
			TriggerRegion newTrigger = new TriggerRegion(sel_pos1, sel_pos2, triggerMsg.toString());
			int triggerId = session.addTrigger(newTrigger);
			session.resetSelection();
			POutput.print(player, "The trigger was added with the ID #" + triggerId + "!");
		}
		catch(IllegalArgumentException e){
			POutput.printError(player, RoutePlanner.ERROR_INVALID_SELECTION);
			return true;
		}
		
		return true;
	}
	
	
	private boolean rRemoveTrigger(Player player, String[] split){
		// check usage (if message is given)
		if(split.length < 2){
			POutput.printUsage(player, RoutePlanner.USAGE_REMTRIGGER);
			return true;
		}
		int trId;
		try{
			trId = Integer.parseInt(split[1]);
		} catch(NumberFormatException e){
			POutput.printError(player, "The trigger-id must be a number!");
			return true;
		}
		
		CreatingSession session = getCreatingSession(player);
		// check if player is creating
		if(session == null){
			POutput.printError(player, RoutePlanner.ERROR_NOT_CREATING);
			return true;
		}
		
		TriggerRegion remTrigger = session.getTrigger(trId);
		if(remTrigger == null){
			POutput.printError(player, RoutePlanner.ERROR_TRIGGER_NOT_FOUND);
			return true;
		}
		
		
		if(session.removeTrigger(remTrigger)){
			POutput.print(player, "Note: The trigger you are deleting is the target trigger.");
		}
		POutput.print(player, "The trigger #"+trId+" was successfully removed!");
		
		return true;
	}
	
	
	
	private boolean rSetTarget(Player player, String[] split){
		// check usage (if trigger-id is given)
		if(split.length < 2){
			POutput.printUsage(player, RoutePlanner.USAGE_SETTARGET);
			return true;
		}
		int trId;
		try{
			trId = Integer.parseInt(split[1]);
		} catch(NumberFormatException e){
			POutput.printError(player, "The trigger-id must be a number!");
			return true;
		}
		
		CreatingSession session = getCreatingSession(player);
		// check if player is creating
		if(session == null){
			POutput.printError(player, RoutePlanner.ERROR_NOT_CREATING);
			return true;
		}
		
		TriggerRegion targetTrigger = session.getTrigger(trId);
		if(targetTrigger == null){
			POutput.printError(player, RoutePlanner.ERROR_TRIGGER_NOT_FOUND);
			return true;
		}
		
		session.setTargetTrigger(targetTrigger);
		POutput.print(player, "The trigger #"+trId+" was set as the target!");
		return true;
	}

	
	private boolean rInfo(Player player, String[] split){
		CreatingSession session = getCreatingSession(player);
		// check if player is creating
		if(session == null){
			POutput.printError(player, RoutePlanner.ERROR_NOT_CREATING);
			return true;
		}
		
		session.dump(player);
		
		return true;
	}
	
	
	private boolean rSave(Player player, String[] split){
		CreatingSession session = getCreatingSession(player);
		// check if player is creating
		if(session == null){
			POutput.printError(player, RoutePlanner.ERROR_NOT_CREATING);
			return true;
		}
		
		
		if(session.isComplete()){
			if(plugin.saveRoute(player,session)){
				plugin.log("Player "+player.getName()+" saved route "+session.getName()+"! Now reloading routes ...");
				plugin.loadRoutes(); 
				POutput.print(player, "The route was saved successfully!");
				plugin.creatingSessions.remove(player);
			}
			else{
				POutput.printError(player, "An error occurred while saving the route!");
			}
		}
		else{
			POutput.print(player, "Route incomplete! Missing:");
			for(String missing: session.getMissing()){
				POutput.print(player, " "+missing);
			}
		}
		
		return true;
	}
	

	
	

	private CreatingSession getCreatingSession(Player player){
		return plugin.creatingSessions.get(player);
	}
}
