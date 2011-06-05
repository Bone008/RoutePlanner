package bone008.routeplanner;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;


public class TriggerRegion {
	private World world;
	private BlockPosition pos1;
	private BlockPosition pos2;
	private String triggerMessage;

	
	public TriggerRegion(Block b1, Block b2, String triggerMsg) throws IllegalArgumentException{
		if(b1 == null || b2 == null || triggerMsg == null) throw new IllegalArgumentException("argument was null");
		
		if(!checkWorldMatch(b1,b2)){
			throw new IllegalArgumentException("Worlds of corner points do not match!");
		}

		world = b1.getWorld();
		pos1 = new BlockPosition(b1);
		pos2 = new BlockPosition(b2);
		setTriggerMessage(triggerMsg);
	}
	public TriggerRegion(World world, BlockPosition b1, BlockPosition b2, String triggerMsg){
		this(world.getBlockAt(b1.getX(),b1.getY(),b1.getZ()), world.getBlockAt(b2.getX(),b2.getY(),b2.getZ()), triggerMsg);
	}

	public World getWorld(){
		return world;
	}
	public BlockPosition getPos1(){
		return pos1;
	}
	public BlockPosition getPos2(){
		return pos2;
	}

	public void setTriggerMessage(String triggerMessage) {
		this.triggerMessage = triggerMessage;
	}
	public String getTriggerMessage() {
		return triggerMessage;
	}
	
	public BlockPosition getMin(){
		return new BlockPosition(
				Math.min(pos1.getX(), pos2.getX()),
				Math.min(pos1.getY(), pos2.getY()),
				Math.min(pos1.getZ(), pos2.getZ())
		);
	}
	public BlockPosition getMax(){
		return new BlockPosition(
				Math.max(pos1.getX(), pos2.getX()),
				Math.max(pos1.getY(), pos2.getY()),
				Math.max(pos1.getZ(), pos2.getZ())
		);
	}
	
	public boolean hitTest(BlockPosition hitPos){
		int x = hitPos.getX();
		int y = hitPos.getY();
		int z = hitPos.getZ();
		BlockPosition min = getMin();
		BlockPosition max = getMax();
		
		return	   x >= min.getX() && x <= max.getX()
		 		&& y >= min.getY() && y <= max.getY()
		 		&& z >= min.getZ() && z <= max.getZ();
	}

	public boolean hitTest(Block hitPos){
		if(world.equals(hitPos.getWorld()))
			return hitTest(new BlockPosition(hitPos));
		return false;
	}
	public boolean hitTest(Location hitPos){
		if(world.equals(hitPos.getWorld()))
			return hitTest(new BlockPosition(hitPos));
		return false;
	}
	
	private boolean checkWorldMatch(Block b1, Block b2){
		return b1.getWorld().equals(b2.getWorld());
	}

}
