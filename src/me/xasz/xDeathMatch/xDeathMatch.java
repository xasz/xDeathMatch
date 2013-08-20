package me.xasz.xDeathMatch;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class xDeathMatch extends JavaPlugin{
	
	PluginManager pm = null;
    xEventListener listener = null;
	@Override
    public void onEnable() {
	    this.getConfig().options().copyDefaults(true);
	    saveConfig();
	    
    	System.out.println("[xDeathMatch] enabled");
		pm = getServer().getPluginManager();
		listener = new xEventListener(this);
		pm.registerEvents(listener,this);
	}
    
    @Override
    public void onDisable() {
    	System.out.println("[xDeathMatch] disabled");
    }
    public void sendMessageToPlayer(Player player, String message){
    	if(player != null){
    		player.sendMessage(ChatColor.BLUE+"[xDeathMatch] "+ChatColor.WHITE+message);
    	}
    }
    public void sendBroadCastMessageToWorld(World world,String message){
    	for(Player p :world.getPlayers()){
        	p.sendMessage(ChatColor.BLUE+"[xDeathMatch] "+ChatColor.WHITE+message);
    	}
    }
}
