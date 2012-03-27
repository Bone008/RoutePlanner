package bone008.routeplanner;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class POutput {
	private static String prefix;
	
	public static void init(String pre, ChatColor color){
		prefix = color+pre;
	}
	
	private POutput(String prefix, ChatColor color){
	}
	
	public static void print(Player player, String msg){
		String[] multiline = new String[1];
		multiline[0] = msg;
		print(player, multiline, true);
	}
	public static void print(Player player, String msg, boolean usePrefix){
		String[] multiline = new String[1];
		multiline[0] = msg;
		print(player, multiline, usePrefix);
	}
	public static void print(Player player, String[] msg){
		print(player, msg, true);
	}
	public static void print(Player player, String[] msg, boolean usePrefix){
		for(String ln: msg){
			String rln = (usePrefix ? prefix : "") + ChatColor.WHITE + ln;
			player.sendMessage(rln);
		}
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
