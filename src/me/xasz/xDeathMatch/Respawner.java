package me.xasz.xDeathMatch;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class Respawner implements Listener{
	private xDeathMatch x = null;
	private int myTaskId = 0;
	private Player player = null;
	@SuppressWarnings("deprecation")
	public Respawner(Player p, final int rtime, xDeathMatch plugin){
		x = plugin;
		player = p;
		Bukkit.getPluginManager().registerEvents(this,x);
		
		//respawn player
		Location spawn = new Location(player.getWorld(), player.getWorld().getSpawnLocation().getX(),player.getWorld().getSpawnLocation().getY()+1,player.getWorld().getSpawnLocation().getZ());
		player.teleport(spawn, TeleportCause.PLUGIN);
		player.setHealth(20);
		player.setLevel(0);
		player.setFoodLevel(20);
		
		for(Player cp: Bukkit.getOnlinePlayers()){
			cp.hidePlayer(player);
			player.hidePlayer(cp);
		}
		x.sendMessageToPlayer(player, "You have "+rtime+" seconds Respawn-Protection");
		final Respawner me = this;
		if( rtime >= 0){
			myTaskId = x.getServer().getScheduler().scheduleAsyncRepeatingTask(x,new Runnable(){
				int respawnTime = rtime;
				@Override
				public void run() {
					player.setLevel(respawnTime);
					if(respawnTime < 0){
						x.sendMessageToPlayer(player, "Respawn-Protection went out. FIGHT!");
						me.destroy();
					}else{
						respawnTime--;
					}
				}
			}, 0L, 20L);
		}

	}
	
	public void destroy(){
		x.getServer().getScheduler().cancelTask(myTaskId);
		HandlerList.unregisterAll(this);
		for(Player p: x.getServer().getOnlinePlayers()){
			p.showPlayer(player);
			player.showPlayer(p);
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onEntityDamageEvent(EntityDamageEvent event){
		if(event.getEntity() != null && event.getEntity() instanceof Player){
			Player hitPlayer = (Player)event.getEntity();
			if(hitPlayer == player){
				event.setCancelled(true);
			}else if(event instanceof EntityDamageByEntityEvent && 
					((EntityDamageByEntityEvent)event).getDamager() instanceof Player &&
					((Player)((EntityDamageByEntityEvent)event).getDamager()) == player){
				event.setCancelled(true);
			}
		}
	}

}
