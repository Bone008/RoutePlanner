package bone008.routeplanner;

import org.bukkit.configuration.MemorySection;

public class RouteConfiguration{

	public int triggerSelectionItem = 268;
	public boolean useWorldEdit = false;
//	public boolean allowPartialRouteNames = true;
	public String msg_followRoute = "Following the route <routename>";
	public String msg_finishRoute = "You have finished this route.";
	
	
	public RouteConfiguration(MemorySection sec){
		this.triggerSelectionItem		= sec.getInt    ("triggerSelectionItem", this.triggerSelectionItem);
		this.useWorldEdit				= sec.getBoolean("useWorldEdit",         this.useWorldEdit);
//		this.allowPartialRouteNames		= sec.getBoolean("allowPartialRouteNames",this.allowPartialRouteNames);
		this.msg_followRoute			= sec.getString ("msg_followRoute",      this.msg_followRoute);
		this.msg_finishRoute			= sec.getString ("msg_finishRoute",      this.msg_finishRoute);
	}
	
}
