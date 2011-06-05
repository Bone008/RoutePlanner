package bone008.routeplanner;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

class POutput {
	private static POutput instance;
	private final String prefix;
	
	public POutput(String prefix, ChatColor color){
		this.prefix = color+prefix;
		instance = this;
	}
	
	
	private void displayMessage(Player player, String[] msg, boolean usePrefix){
		for(String ln: msg){
			String rln = (usePrefix ? prefix : "") + ChatColor.WHITE + ln;
			player.sendMessage(rln);
		}
	}
	


	public static void print(Player player, String msg){
		String[] multiline = new String[1];
		multiline[0] = msg;
		print(player, multiline);
	}
	public static void print(Player player, String[] msg){
		print(player, msg, true);
	}
	public static void print(Player player, String[] msg, boolean usePrefix){
		instance.displayMessage(player, msg, usePrefix);
	}
	
	public static void printUsage(Player player, String msg){
		String[] multiline = new String[1];
		multiline[0] = msg;
		printUsage(player, multiline);
	}
	public static void printUsage(Player player, String[] msg){
		for(int i=0; i<msg.length; i++){
			msg[i] = ChatColor.BLUE+msg[i];
		}
		print(player, msg);
	}
	
	public static void printError(Player player, String msg){
		print(player, ChatColor.RED+msg);
	}
}
