package bone008.routeplanner;

import java.io.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;
import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;

public class RoutePlanner extends JavaPlugin{
	static final Logger logger = Logger.getLogger("Minecraft");
	
	static POutput playerOut;
	static String consolePrefix;
	
	
	private File routesFile = new File("plugins/RoutePlanner/routes.yml");
	private final RoutePlayerListener playerListener = new RoutePlayerListener(this);
	private final RouteBlockListener blockListener = new RouteBlockListener(/*this*/);

	// yml-configuration
	RouteConfiguration config;

	PluginDescriptionFile pdfFile;
	PermissionHandler permissionHandler;
	WorldEditPlugin worldEdit;
	HashMap<Player,CreatingSession> creatingSessions = new HashMap<Player,CreatingSession>();
	HashMap<Player,RoutingSession> routingSessions = new HashMap<Player,RoutingSession>();
	HashMap<String,Route> routes = new HashMap<String,Route>();
	
	Configuration routesConfig;
	
	
	
	@Override
	public void onLoad() {
		pdfFile = getDescription();
		consolePrefix = "["+pdfFile.getName()+"] ";
		playerOut = new POutput(consolePrefix, ChatColor.GRAY);
	}
	
	@Override
	public void onDisable() {
		log("Plugin was disabled!");
	}
	
	
	@Override
	public void onEnable() {
		getDataFolder().mkdirs();
		
		config = new RouteConfiguration(getConfiguration());
		writeConfig();
		
		routesConfig = new Configuration(routesFile);
		loadRoutes();
		
		PluginManager pManager = getServer().getPluginManager();
		pManager.registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Priority.Highest, this);
		pManager.registerEvent(Event.Type.PLAYER_MOVE, playerListener, Priority.Normal, this);
		pManager.registerEvent(Event.Type.SIGN_CHANGE, blockListener, Priority.Normal, this);

		getCommand("route").setExecutor(new RoutePlannerCommand(this));
		
		// hook into worldedit
		Plugin worldEditPlugin = getServer().getPluginManager().getPlugin("WorldEdit");
		if(worldEditPlugin != null && worldEditPlugin instanceof WorldEditPlugin){
			this.worldEdit = (WorldEditPlugin) worldEditPlugin;
			log("Successfully hooked into WorldEdit ...");
		}
		
		String permVersion = setupPermissions();
		
		log("Version "+pdfFile.getVersion()+" is enabled using "+ (permVersion==null ? "the Op-System" : "Permissions "+permVersion) +"!");
	}
	
	
	
	private void writeConfig() {
		if(!new File(getDataFolder().getPath()+File.separator+"config.yml").exists())
			log("Creating config file ...");
		// loop through fields of the RouteConfiguration-instance and write them into the yml
		for(Field configEntry: RouteConfiguration.class.getFields()){
			try {
				getConfiguration().setProperty(configEntry.getName(), configEntry.get(config));
			}
			// only handle public stuff!
			catch (IllegalArgumentException e) {}
			catch (IllegalAccessException e) {}
		}
		
		getConfiguration().save();
	}
	
	
	
	void loadRoutes(){
		routesConfig.load();
		
		List<String> routesList = routesConfig.getKeys("routes");
		// if no route was created yet
		if(routesList == null) return;
		int errorCounter = 0;
		
		routes.clear();
		for(String routeKey: routesList){
			// converting happens in Route-class
			Route currRoute = new Route(this, routesConfig.getNode("routes."+routeKey));
			if(currRoute.isValid())
				routes.put(routeKey.toLowerCase(), currRoute);
			else
				errorCounter++;
		}
		
		log("Loaded "+routes.size()+" saved route"+ (routes.size()==1 ? "" : "s") +"!");
		if(errorCounter > 0)
			log("WARNING: Skipped "+errorCounter+" invalid route entr"+ (errorCounter==1 ? "y" : "ies") +"!");
	}
	
	


	public boolean saveRoute(Player player, CreatingSession session) {
		if(!session.isComplete())
			throw new IllegalArgumentException("incomplete session");
		
		String creator = player.getName();
		String root = "routes."+session.getName();
		
		// reset property if already exists (editing process)
		if(routesConfig.getProperty(root) != null){
			routesConfig.removeProperty(root);
		}
		
		routesConfig.setProperty(root+".name", session.getName());
		routesConfig.setProperty(root+".creator", creator);
		routesConfig.setProperty(root+".introMessage", session.getIntroMessage());
		routesConfig.setProperty(root+".targetTrigger", session.getTargetTriggerNum());
		
		String trRoot = root+".triggerRegions";
		List<TriggerRegion> allTriggers = session.getTriggers();
		for(int i=0; i < allTriggers.size(); i++){
			String rRoot = trRoot+".r"+i;
			String worldName = allTriggers.get(i).getWorld().getName();
			BlockPosition pos1 = allTriggers.get(i).getPos1();
			BlockPosition pos2 = allTriggers.get(i).getPos2();
			String triggerMessage = allTriggers.get(i).getTriggerMessage();

			routesConfig.setProperty(rRoot+".world", worldName);
			routesConfig.setProperty(rRoot+".pos1", pos1.getList());
			routesConfig.setProperty(rRoot+".pos2", pos2.getList());
			routesConfig.setProperty(rRoot+".message", triggerMessage);
		}
		
		return routesConfig.save();
	}
	
	boolean removeRoute(String routeName){
		routesConfig.removeProperty("routes."+routeName);
		return routesConfig.save();
	}
	

	
	public Route getRoute(String name){
		if(name == null) return null;
		ArrayList<Route> exact = new ArrayList<Route>();
		ArrayList<Route> results = new ArrayList<Route>();
		for(Route r: this.routes.values()){
			if(r.getName().equalsIgnoreCase(name))
				exact.add(r);
			else if(r.getName().toLowerCase().contains(name.toLowerCase()))
				results.add(r);
		}
		
		if(exact.size() == 1)
			return exact.get(0);
		if(results.size() == 1)
			return results.get(0);
		return null;
	}
	
	
	public static String colorize(String s){
		if(s == null) return null;
		return s.replaceAll("&([0-9a-f])", "\u00A7$1");
	}
	
	
	public static String getSignRouteName(Sign s){
		return getSignRouteName(s.getLines());
	}
	public static String getSignRouteName(String[] lines){
		if(lines == null || lines.length != 4)
			throw new IllegalArgumentException("invalid lines parameter");
		// only for first 3 lines!
		for(int i=0; i<3; i++){
			if(lines[i].trim().equals("RoutePlanner") && !lines[i+1].trim().isEmpty())
				return lines[i+1].trim();
		}
		return null;
	}
	
	

	public static final String	 USAGE_CREATE = 
		"Usage: /route create <routename>";
	public static final String	 USAGE_EDIT = 
		"Usage: /route edit <routename>";
	public static final String	 USAGE_REMOVE = 
		"Usage: /route remove <routename>";
	public static final String[] USAGE_INTRO = {
		"Sets the message shown to the player",
		"when he enables the route.",
		"Usage: /route intro <intro-message>"};
	public static final String[] USAGE_ADDTRIGGER = {
		"Adds the current selection as a trigger-area to the route.",
		"Usage: /route addtrigger <trigger-message>",
		"You have to select an area first!",
		"The message is shown to the player",
		"when he moves into the specified area."};
	public static final String[] USAGE_REMTRIGGER = {
		"Removes the specified trigger from the list.",
		"Usage: /route remtrigger <trigger-id>",
		"Note: Trigger-IDs above the removed one get shifted!"};
	public static final String[] USAGE_SETTARGET = {
		"Sets the specified trigger as the target.",
		"The route ends when the user reaches it.",
		"Usage: /route settarget <trigger-id>"};
	
	
	// not static!
	public final String[] HELP = {
		
	};
	
	
	static final String PERMISSON_BASIC = "routeplanner.use";
	static final String PERMISSON_ADMIN = "routeplanner.admin";
	static final String PERMISSON_ALTEROTHER = "routeplanner.alterother";

	static final String ERROR_ALREADY_RUNNING = "You are already following a route!";
	static final String ERROR_ALREADY_CREATING = "You are already creating a new route!";
	static final String ERROR_ALREADY_EXISTS = "There already exists a route with that name!";
	static final String ERROR_NOT_EXISTS = "There is no route with that name!";
	static final String ERROR_NOT_RUNNING = "You are not running a route!";
	static final String ERROR_NOT_CREATING = "You are not in a creating process of a route!";
	static final String ERROR_NO_SELECTION = "You have to select a region at first!";
	static final String ERROR_INVALID_SELECTION = "Your selected points aren't in the same world!";
	static final String ERROR_TRIGGER_NOT_FOUND = "There is no trigger with that ID!";
	static final String ERROR_NO_PERMISSION_ALTEROTHER = "You don't have permission to alter routes created by someone else!";


	
	
	
	
	/**
	 * Hooks into Permissions
	 * 
	 * @return String version of Permissions-Plugin
	 */
	private String setupPermissions(){
		Plugin permissionsPlugin = this.getServer().getPluginManager().getPlugin("Permissions");
		

		if (permissionHandler == null) {
			if (permissionsPlugin != null) {
				permissionHandler = ((Permissions) permissionsPlugin).getHandler();
				return permissionsPlugin.getDescription().getVersion();
			}
			return null;
		}
		return null;
	}
	
	boolean hasPermission(Player player, String perm){
		if(permissionHandler == null){
			if(perm.contains("admin") && !player.isOp())
				return false;
			return true;
		}
		else{
			return permissionHandler.has(player, perm);
		}
	}
	
	// logs a message to console
	void log(String msg, boolean usePrefix){
		logger.info( (usePrefix==true ? consolePrefix : "") + msg );
	}
	void log(String msg){
		this.log(msg,true);
	}
	
}
