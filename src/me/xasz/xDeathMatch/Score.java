package me.xasz.xDeathMatch;

public class Score {
	private int kills = 0;
	private int deaths = 0;
	private int assists = 0;
	public void addKill(){kills++;}
	public void addDeath(){deaths++;}
	public void addAssist(){assists++;}
	public int getKills(){return kills;}
	public int getDeaths(){return deaths;}
	public int getAssists(){return assists;}
}
