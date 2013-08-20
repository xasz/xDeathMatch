package me.xasz.xDeathMatch;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;


public class xEventListener implements Listener, CommandExecutor{
	enum DeathMatchmode {
		PVP,
		HUNGER
	}
	
	private xDeathMatch x = null;
	private Map<Player,Score> scores = new HashMap<Player,Score>();
	private boolean matchIsRunning = false;
	private int killLimit = 0;
	private boolean playerCanMove = true;
	private DeathMatchmode matchmode = DeathMatchmode.PVP;
	private List<Respawner> respawner = new ArrayList<Respawner>(); 
	
	
	public xEventListener (final xDeathMatch instance){
		x = instance;
		resetStats();
        x.getCommand("xdm").setExecutor(this);
	}
	public void showScore(){
		playerCanMove = false;
		Player winner = null;
		
	
		if(matchmode == DeathMatchmode.HUNGER){
			for(Map.Entry<Player,Score> pair : scores.entrySet()){
				if( pair.getValue().getDeaths() == 0){
					winner = pair.getKey();
					break;
				}
			}
		}else if(matchmode == DeathMatchmode.PVP){
			for(Player p : scores.keySet()){
				if(scores.get(p).getDeaths() >= killLimit){
					winner = p;
					break;
				}
			}
		}
		x.getServer().getScheduler().scheduleSyncDelayedTask(x, new Runnable() {
		    @Override 
		    public void run() {
				for(World w : x.getServer().getWorlds()){
					x.sendBroadCastMessageToWorld(w, "Match is over.. Thank you for playing with xDeathMatch");
					playerCanMove = true;
				}
		    }
		}, 20L*10);
		
		if(winner == null){
			for(World w : x.getServer().getWorlds()){
				x.sendBroadCastMessageToWorld(w, "There isn't a winner seems to be a canceled match or both Player died the same time");
			}
		}else{
			for(World w : x.getServer().getWorlds()){
				String extension = "";
				if(matchmode == DeathMatchmode.PVP){
					extension =  " with a KillLimit of "+killLimit;
				}
				x.sendBroadCastMessageToWorld(w, winner.getName() + "have WON the match"+extension);
			}
		}		

		for(Respawner r : respawner){
			r.destroy();
		}
		respawner.clear();
	}
	public void startMatch(){
		respawner.clear();
		playerCanMove = true;
		for(World w : x.getServer().getWorlds()){
			if(matchmode == DeathMatchmode.HUNGER){
				x.sendBroadCastMessageToWorld(w, "Hungergame is starting in 1 Minute. If you die, you are dead. No Respawn.");
			}else{
				x.sendBroadCastMessageToWorld(w, "Match with killlimit "+killLimit+" is Starting in 1 Minute");
			}
			x.sendBroadCastMessageToWorld(w, "You are Invisible for 1 Minute. Then you have to Fight");
		}
		for(Player p : Bukkit.getServer().getOnlinePlayers()){
			p.getInventory().clear();
			new Respawner(p,60,x);
		}
	}
	public void resetStats(){
		for(World w : x.getServer().getWorlds()){
			for(Player p :w.getPlayers()){
				scores.put(p, new Score());
				changePlayerNameToScore(p);
			}
		}	
		matchIsRunning = false;
		playerCanMove = true;
	}
	
	
	
		

	
	//EVENTS 
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onMoveEvent(PlayerMoveEvent event){
		if(playerCanMove == false){
			event.setCancelled(true);
		}
	}
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args)
	{
	    String commandName = command.getName().toLowerCase();
	    if( sender instanceof Player){
	    	Player player = (Player)sender;
	    	if(commandName.equals("xdm")){
	    		if(args.length > 0){
	    			if(args.length > 3){
	    				x.sendMessageToPlayer(player, "To many Arguments");
	    			}else{
	    				if(args[0].equals("stop")){
	    					if(!matchIsRunning){
	    						x.sendMessageToPlayer(player, "No Match is running");
	    					}else{
	    						showScore();
	    					}
	    				}else if(args[0].equals("cancel")){
	    					if(!matchIsRunning){
	    						x.sendMessageToPlayer(player, "No Match is running");
	    					}else{	
	    						x.sendMessageToPlayer(player, "Match canceled.");
		    				}
	    					resetStats();
	    					
	    				}else if(args[0].equals("start") && args.length== 3){
	    					//start
	    					if(matchIsRunning){
	    						x.sendMessageToPlayer(player, "Match is already Running");
	    					}else{
	    						try{
		    						killLimit = Integer.parseInt(args[2]);
		    						String gamemode = args[1];
		    						gamemode = gamemode.toLowerCase();
		    						if(gamemode.compareTo("hunger") == 0 ){
		    							matchmode = DeathMatchmode.HUNGER;
		    						}else if(gamemode.compareTo("pvp") == 0) {
		    							matchmode = DeathMatchmode.PVP;
		    						}else{
		    							throw new Exception("Wrong gamemode - have to be hunger or pvp");
		    						}
		    						matchIsRunning = true;
		    						startMatch();
	    						}catch(Exception ex){
	    							x.sendMessageToPlayer(player, "Wrond Parameter - need killlimit - ");
	    						}
	    					}
	    				}else{
	    					x.sendMessageToPlayer(player, "Wrond Parameter");	
	    				}
	    			}
	    		}else{
	    			x.sendMessageToPlayer(player, "parameters possible: stop, cancel, start <gamemode hunger:pvp> <killlimit 1-xxx>");
	    		}
	    	}
	    }else{
	    	System.out.println("Command cannot send from Console.");
	    }
	    return true;	     
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerDeath(PlayerDeathEvent event){
			if(event.getEntity() != null){
				final Player deadPlayer = event.getEntity();
				Player killer = deadPlayer.getKiller();
				if(!scores.containsKey(deadPlayer)){
					scores.put(deadPlayer, new Score());
				}
				if(killer != null && killer instanceof Player){
					if(!scores.containsKey(killer)){
						scores.put(killer, new Score());
					}
					scores.get(killer).addKill();
					changePlayerNameToScore(killer);
					if(killer.getItemInHand() != null)
						x.sendBroadCastMessageToWorld(deadPlayer.getWorld(), deadPlayer.getName()+" was killed by "+ChatColor.RED+killer.getName()+ChatColor.WHITE+" with " +
								killer.getItemInHand().getType().name().substring(0,1)+killer.getItemInHand().getType().name().toLowerCase().substring(1));
					else
						x.sendBroadCastMessageToWorld(deadPlayer.getWorld(), deadPlayer.getName()+" was killed by "+ChatColor.RED+killer.getName()+ChatColor.WHITE+" with Hands");	
				}else{
					x.sendBroadCastMessageToWorld(deadPlayer.getWorld(), deadPlayer.getName()+" was killed");

				}
				scores.get(deadPlayer).addDeath();
				changePlayerNameToScore(deadPlayer);
				if(this.matchIsRunning){
					if(matchmode == DeathMatchmode.PVP){
						if(scores.get(killer).getKills() >= killLimit){
							showScore();
						}
					}else if(matchmode == DeathMatchmode.HUNGER){
						int livingPlayer = 0;
						for(Map.Entry<Player,Score> pair : scores.entrySet()){
							if( pair.getValue().getDeaths() == 0) livingPlayer++;
						}
						if(livingPlayer <= 1){
							showScore();
						}
					}
				}
				//disables the ingame death message
	            event.setDeathMessage(null);
			}	
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerRespawn(PlayerRespawnEvent event){
		//respawn
		int respawntimer = 15;
		if(matchmode == DeathMatchmode.HUNGER){
			respawntimer = -1;
		}
		if(this.matchIsRunning)
			respawner.add(new Respawner(event.getPlayer(), respawntimer, x));	
	}
	
	protected void changePlayerNameToScore(Player player){
		String score =" "+scores.get(player).getKills()+"/"+scores.get(player).getDeaths()+"";
		if(score.length() + player.getName().length() <= 16){
			player.setPlayerListName(player.getName() + score);	
		}else{
			player.setPlayerListName(player.getName().substring(0, 16-score.length()) + score);		
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerJoin(PlayerJoinEvent event){
		if(event.getPlayer() != null){
			Player p = event.getPlayer();
			if(!scores.containsKey(p)){
				scores.put(event.getPlayer(), new Score());
			}
			changePlayerNameToScore(p);
		}
	}

}
