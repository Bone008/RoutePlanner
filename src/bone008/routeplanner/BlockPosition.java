package bone008.routeplanner;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.block.Block;

public class BlockPosition {
	private int x;
	private int y;
	private int z;
	
	public BlockPosition(int x, int y, int z){
		this.x = x;
		this.y = y;
		this.z = z;
	}
	public BlockPosition(Location l){
		this(l.getBlockX(),l.getBlockY(),l.getBlockZ());
	}
	public BlockPosition(Block b){
		this(b.getX(),b.getY(),b.getZ());
	}
	
	public List<Integer> getList(){
		List<Integer> ret = new ArrayList<Integer>();
		ret.add(x);
		ret.add(y);
		ret.add(z);
		return ret;
	}
	
	public int getX(){
		return x;
	}
	public int getY(){
		return y;
	}
	public int getZ(){
		return z;
	}
}
