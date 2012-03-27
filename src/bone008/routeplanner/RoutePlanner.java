package bone008.routeplanner;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;

public class RoutePlanner extends JavaPlugin{
	public static final Logger logger = Logger.getLogger("Minecraft");
	
	private final RoutePlayerListener playerListener = new RoutePlayerListener(this);
	private final RouteBlockListener blockListener = new RouteBlockListener();
	

	private FileConfiguration routesConfig;
	
	private File routesFile;
	private String consolePrefix;
	public PluginDescriptionFile pdfFile;
	
	public RouteConfiguration config;

	public WorldEditPlugin worldEdit;
	public HashMap<Player,CreatingSession> creatingSessions = new HashMap<Player,CreatingSession>();
	public HashMap<Player,RoutingSession> routingSessions = new HashMap<Player,RoutingSession>();
	public HashMap<String,Route> routes = new HashMap<String,Route>();
	
	
	
	
	@Override
	public void onLoad() {
		routesFile = new File(getDataFolder(), "routes.yml");
		pdfFile = getDescription();
		consolePrefix = "["+pdfFile.getName()+"] ";
		POutput.init(consolePrefix, ChatColor.GRAY);
	}
	
	@Override
	public void onDisable() {
		log("is disabled!");
	}
	
	
	@Override
	public void onEnable() {
		getConfig().options().copyDefaults(true);
		getConfig().options().copyHeader(true);
		config = new RouteConfiguration(getConfig());
		saveConfig();
		
		loadRoutes();
		
		PluginManager pManager = getServer().getPluginManager();
		pManager.registerEvents(playerListener, this);
		pManager.registerEvents(blockListener, this);

		getCommand("route").setExecutor(new RoutePlannerCommand(this));
		
		// hook into worldedit
		Plugin worldEditPlugin = getServer().getPluginManager().getPlugin("WorldEdit");
		if(worldEditPlugin != null && worldEditPlugin instanceof WorldEditPlugin){
			this.worldEdit = (WorldEditPlugin) worldEditPlugin;
			log("successfully hooked into WorldEdit.");
		}
		
		log("version "+pdfFile.getVersion()+" is enabled!");
	}
	
	
	
	public void loadRoutes(){
		routesConfig = YamlConfiguration.loadConfiguration(routesFile);
		routes.clear();
		
		ConfigurationSection routesSec = routesConfig.getConfigurationSection("routes");
		// if no route was created yet
		if(routesSec == null) return;
		Set<String> routesSet = routesSec.getKeys(false);
		int errorCounter = 0;
		
		for(String routeKey: routesSet){
			// converting happens in Route-class
			Route currRoute = new Route(this, routesSec.getConfigurationSection(routeKey));
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
		
		ConfigurationSection routesSec = routesConfig.getConfigurationSection("routes");
		if(routesSec == null)
			routesSec = routesConfig.createSection("routes");
		
		ConfigurationSection rootSec = routesSec.createSection(session.getName());

		String creator = player.getName();

		rootSec.set("name", session.getName());
		rootSec.set("creator", creator);
		rootSec.set("introMessage", session.getIntroMessage());
		rootSec.set("targetTrigger", session.getTargetTriggerNum());
		
		ConfigurationSection triggerSec = rootSec.createSection("triggerRegions");
		List<TriggerRegion> allTriggers = session.getTriggers();
		for(int i=0; i < allTriggers.size(); i++){
			TriggerRegion currTrigger = allTriggers.get(i);
			String worldName = currTrigger.getWorld().getName();
			BlockPosition pos1 = currTrigger.getPos1();
			BlockPosition pos2 = currTrigger.getPos2();
			String triggerMessage = currTrigger.getTriggerMessage();
			
			ConfigurationSection currTriggerSec = triggerSec.createSection("r"+i);
			currTriggerSec.set("world", worldName);
			currTriggerSec.set("pos1", pos1.getList());
			currTriggerSec.set("pos2", pos2.getList());
			currTriggerSec.set("message", triggerMessage);
		}
		
		return saveFileConfiguration(routesConfig, routesFile);
	}
	
	public boolean removeRoute(String routeName){
		routesConfig.set("routes."+routeName, null);
		return saveFileConfiguration(routesConfig, routesFile);
	}
	
	
	private boolean saveFileConfiguration(FileConfiguration cfg, File file){
		try {
			cfg.save(file);
			return true;
		} catch (IOException e) {
			return false;
		}
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
	
	
	public static final String PERMISSON_BASIC = "routeplanner.use";
	public static final String PERMISSON_ADMIN = "routeplanner.admin";
	public static final String PERMISSON_ALTEROTHER = "routeplanner.alterother";

	public static final String ERROR_ALREADY_RUNNING = "You are already following a route!";
	public static final String ERROR_ALREADY_CREATING = "You are already creating a new route!";
	public static final String ERROR_ALREADY_EXISTS = "There already exists a route with that name!";
	public static final String ERROR_NOT_EXISTS = "There is no route with that name!";
	public static final String ERROR_NOT_RUNNING = "You are not running a route!";
	public static final String ERROR_NOT_CREATING = "You are not in a creating process of a route!";
	public static final String ERROR_NO_SELECTION = "You have to select a region at first!";
	public static final String ERROR_INVALID_SELECTION = "Your selected points aren't in the same world!";
	public static final String ERROR_TRIGGER_NOT_FOUND = "There is no trigger with that ID!";
	public static final String ERROR_NO_PERMISSION_ALTEROTHER = "You don't have permission to alter routes created by someone else!";
	
	
	// logs a message to console
	public void log(String msg, boolean usePrefix, Level lvl){
		logger.log(lvl, (usePrefix==true ? consolePrefix : "") + msg );
	}
	public void log(String msg, boolean usePrefix){
		log(msg, usePrefix, Level.INFO);
	}
	public void log(String msg){
		log(msg, true);
	}
	
}
