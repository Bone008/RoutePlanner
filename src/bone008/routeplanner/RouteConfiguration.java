package bone008.routeplanner;

import org.bukkit.util.config.Configuration;

public class RouteConfiguration{

	public int triggerSelectionItem = 268;
//	public boolean useWorldEdit = false;
//	public boolean allowPartialRouteNames = true;
	public String msg_followRoute = "Following the route <routename>";
	public String msg_finishRoute = "You have finished this route.";
	
	
	public RouteConfiguration(Configuration config){
		this.triggerSelectionItem		= config.getInt		("triggerSelectionItem",	this.triggerSelectionItem);
//		this.useWorldEdit				= config.getBoolean	("useWorldEdit",			this.useWorldEdit);
//		this.allowPartialRouteNames		= config.getBoolean	("allowPartialRouteNames",	this.allowPartialRouteNames);
		this.msg_followRoute			= config.getString	("msg_followRoute",			this.msg_followRoute);
		this.msg_finishRoute			= config.getString	("msg_finishRoute",			this.msg_finishRoute);
	}
	
}
