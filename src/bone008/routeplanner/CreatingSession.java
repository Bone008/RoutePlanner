package bone008.routeplanner;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class CreatingSession {
	private String routeName;
	private String introMessage;
	private ArrayList<TriggerRegion> triggerRegions = new ArrayList<TriggerRegion>();
	private TriggerRegion targetTrigger;
	Block[] selection = new Block[2];

	
	public CreatingSession(String routeName){
		if(routeName == null || routeName.isEmpty()) throw new NullPointerException("undefined routeName");
		this.setName(routeName);
	}
	
	// loads data from an existing route
	public CreatingSession(Route route){
		if(!route.isValid())
			throw new IllegalArgumentException("passed route must be valid!");

		this.setName(route.getName());
		this.setIntroMessage(route.getIntroMessage());
		this.triggerRegions = route.getTriggerRegions();
		this.setTargetTrigger(route.getTargetTrigger());
	}
	
	public void setName(String routeName) {
		this.routeName = routeName;
	}
	public String getName() {
		return routeName;
	}
	
	public void setIntroMessage(String introMessage) {
		this.introMessage = introMessage;
	}
	public String getIntroMessage() {
		return introMessage;
	}

	public int addTrigger(TriggerRegion tR){
		triggerRegions.add(tR);
		return triggerRegions.size()-1;
	}
	
	public TriggerRegion getTrigger(int trId) {
		try{
			return triggerRegions.get(trId);
		}
		catch(IndexOutOfBoundsException e){
			return null;
		}
	}
	
	
	public boolean removeTrigger(TriggerRegion tR){
		triggerRegions.remove(tR);

		if(tR == targetTrigger){
			targetTrigger = null;
			return true;
		}
		return false;
	}
	
	
	
	List<TriggerRegion> getTriggers(){
		return triggerRegions;
	}
	
	
	public void setTargetTrigger(TriggerRegion tR){
		if(tR == null || !triggerRegions.contains(tR))
			throw new IllegalArgumentException("invalid targetTrigger");
		targetTrigger = tR;
	}
	public TriggerRegion getTargetTrigger(){
		return targetTrigger;
	}
	public int getTargetTriggerNum(){
		return triggerRegions.indexOf(targetTrigger);
	}
	
	
	public void resetSelection(){
		this.selection = new Block[2];
	}
	
	
	
	public boolean isComplete(){
		if(	routeName != null && routeName.length() > 0 &&
			introMessage != null &&
			targetTrigger != null &&
			triggerRegions.contains(targetTrigger))
				return true;
		return false;
	}
	
	public ArrayList<String> getMissing(){
		ArrayList<String> ret = new ArrayList<String>();
		
		// Note: routeName should not be invalid (set in constructor)
		if(routeName == null || routeName.length() <= 0)
			ret.add("There is no route name set!");
		if(introMessage == null)
			ret.add("There is no intro message set!");
		if(targetTrigger == null){
			ret.add("There is no target set!");
			return ret;
		}
		// Should never happen!
		if(!triggerRegions.contains(targetTrigger))
			ret.add("The target trigger is invalid!");
		
		return ret;
	}
	
	
	
	
	
	public void dump(Player player){
		player.sendMessage(ChatColor.GRAY+"route name: "+ChatColor.WHITE+routeName);
		player.sendMessage(ChatColor.GRAY+"intro message: "+ChatColor.WHITE+RoutePlanner.colorize(introMessage));
		player.sendMessage(ChatColor.GRAY+"trigger regions:");
		for(int i=0; i<triggerRegions.size(); i++){
			TriggerRegion currReg = triggerRegions.get(i);
			player.sendMessage(ChatColor.GRAY+"  #"+i+": "+ChatColor.WHITE+currReg.getTriggerMessage()+ (currReg == targetTrigger ? ChatColor.GREEN+" [target]" : ""));
		}
	}

}
