package bone008.routeplanner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.World;
import org.bukkit.util.config.ConfigurationNode;

public class Route {
	private String name;
	private String introMessage;
	private ArrayList<TriggerRegion> triggerRegions = new ArrayList<TriggerRegion>();
	private int targetTriggerNum;
	private String creator;
	
	private RoutePlanner plugin;
	private boolean valid = false;

	
	public Route(RoutePlanner plugin, ConfigurationNode node) {
		this.plugin = plugin;
		try{
			init(
					node.getString("name"),
					node.getString("introMessage"),
					convertTriggers(node.getNodes("triggerRegions")),
					node.getInt("targetTrigger",-1),
					node.getString("creator",null)
			);
			// set valid-flag to show the route was initialized
			this.valid = true;
		} catch(Exception e){
			RoutePlanner.logger.severe(RoutePlanner.consolePrefix + e.getClass().getSimpleName() + " parsing route"+(name==null ? "":" "+name)+": "+e.getMessage());
		}
	}

	
	// called by constructor; parses the triggers defined in routes.yml
	private ArrayList<TriggerRegion> convertTriggers(Map<String, ConfigurationNode> map) {
		ArrayList<TriggerRegion> ret = new ArrayList<TriggerRegion>();
		ret.ensureCapacity(map.size());
		
		for(int i=0; true; i++){
			ConfigurationNode currTrigger = map.get("r"+i);
			if(currTrigger == null)
				break;
			
			World currWorld = plugin.getServer().getWorld(currTrigger.getString("world"));
			List<Integer> pos1 = currTrigger.getIntList("pos1", null);
			List<Integer> pos2 = currTrigger.getIntList("pos2", null);
			String currMessage = currTrigger.getString("message");
			
			try{
				ret.add( i , new TriggerRegion(currWorld.getBlockAt(pos1.get(0), pos1.get(1), pos1.get(2)), currWorld.getBlockAt(pos2.get(0), pos2.get(1), pos2.get(2)), currMessage) );
			} catch(NullPointerException e){
				throw new NullPointerException("invalid trigger-data!");
			} catch(IllegalArgumentException e){
				throw new IllegalArgumentException("invalid trigger-data!");
			}
		}
		return ret;
	}


	private void init(String a, String b, ArrayList<TriggerRegion> c, int d, String e){
		setName(a);
		setIntroMessage(b);
		triggerRegions = c;
		setTargetTrigger(d);
		setCreator(e);
	}
	
	
	
	
	public void setName(String name) {
		if(name == null || name.isEmpty())
			throw new NullPointerException("name can't be null or empty!");
		this.name = name;
	}

	public String getName() {
		return name;
	}

	
	public void setIntroMessage(String introMessage) {
		if(introMessage == null || introMessage.isEmpty())
			throw new NullPointerException("introMessage can't be null or empty!");
		this.introMessage = introMessage;
	}

	public String getIntroMessage() {
		return introMessage;
	}

	public void setCreator(String creator) {
		if(creator == null || creator.isEmpty())
			throw new NullPointerException("creator can't be null or empty!");
		this.creator = creator;
	}

	public String getCreator() {
		return creator;
	}

	public void setTargetTrigger(int index) {
		if(this.triggerRegions.get(index) == null)
			throw new IllegalArgumentException("targetTrigger must be in the trigger-list!");
		this.targetTriggerNum = index;
	}

	public TriggerRegion getTargetTrigger() {
		TriggerRegion ret = null;
		try{
			ret = triggerRegions.get(targetTriggerNum);
		} catch(IndexOutOfBoundsException e){ return null; }
		return ret;
	}

	public ArrayList<TriggerRegion> getTriggerRegions() {
		return triggerRegions;
	}
	
	public boolean isValid(){
		return valid;
	}
	
}
