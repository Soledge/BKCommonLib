package com.bergerkiller.bukkit.common.scoreboards;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import com.bergerkiller.bukkit.common.internal.CommonPlugin;

public class CommonScoreboard {
	private static Map<Player, CommonScoreboard> boards = new WeakHashMap<Player, CommonScoreboard>();
	private static Map<String, CommonTeam> teams = new HashMap<String, CommonTeam>();
	public static CommonTeam dummyTeam = new CommonTeam("dummy") {
		private static final long serialVersionUID = 2284488822613734842L;
		public void addPlayer(OfflinePlayer player) {}
		public void removePlayer(OfflinePlayer player) {}
		public void show() {}
		public void hide() {}
		public void setDisplayName(String displayName) {}
		public void setPrefix(String prefix) {}
		public void setSuffix(String suffic) {}
		public void setFriendlyFire(FriendlyFireType friendlyFire) {}
		public void send(Player player) {}
		public void setSendToAll(boolean sendToAll) {}
	};
	
	public static CommonScoreboard get(Player player) {
		if(boards.containsKey(player))
			return boards.get(player);
		else {
			CommonScoreboard board = new CommonScoreboard(player);
			boards.put(player, board);
			return board;
		}
	}
	
	private CommonTeam team;
	private CommonObjective[] objectives = new CommonObjective[3];
	private Player player;
	
	public CommonScoreboard(Player player) {
		this.team = dummyTeam;
		this.player = player;
		for(int i = 0; i < 3; i++) {
			Display display = Display.fromInt(i);
			objectives[i] = new CommonObjective(this, display);
		}
	}
	
	/**
	 * Get the player form the scoreboard
	 * 
	 * @return Player
	 */
	public Player getPlayer() {
		return this.player;
	}
	
	/**
	 * Get the scoreboard form a certain display
	 * 
	 * @param display Display
	 * @return Objective
	 */
	public CommonObjective getObjective(Display display) {
		return this.objectives[display.getId()];
	}
	
	/**
	 * Get the scoreboard team from the player
	 * 
	 * @return Scoreboard team
	 */
	public CommonTeam getTeam() {
		return this.team;
	}
	
	/**
	 * Set the team for the player
	 * 
	 * @param team New team
	 */
	public void setTeam(CommonTeam team) {
		if(team == null)
			throw new IllegalArgumentException("Team cannot be null!");
		
		this.team = team;
	}
	
	/**
	 * Create a new scoreboard team
	 * 
	 * @param name Team name
	 * @return New team
	 */
	public static CommonTeam newTeam(String name) {
		CommonTeam team = new CommonTeam(name);
		teams.put(name, team);
		return team;
	}
	
	/**
	 * Load a team form the disk
	 * 
	 * @param team Team name
	 * @return Team form disk (new team if failed)
	 */
	public static CommonTeam loadTeam(String name) {
		CommonTeam team = null;
		
		try {
			File dir = CommonPlugin.getInstance().getDataFolder();
			dir.mkdir();
			dir = new File(dir, "teams");
			dir.mkdir();
			File file = new File(dir, name + ".bin");
			if(file.exists()) {
				ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
				team = (CommonTeam) ois.readObject();
				ois.close();
			}
		} catch(Exception e) {
			CommonPlugin.LOGGER.log(Level.SEVERE, "Failed to load team form disk", e);
		}
		
		if(team == null) {
			//Failed to load team
			team = new CommonTeam(name);
		}
		
		teams.put(name, team);
		return team;
	}
	
	/**
	 * Save a team to the disk
	 * 
	 * @param team Team to save
	 */
	public static void saveTeam(CommonTeam team) {
		try {
			File dir = CommonPlugin.getInstance().getDataFolder();
			dir.mkdir();
			dir = new File(dir, "teams");
			dir.mkdir();
			File file = new File(dir, team.getName() + ".bin");
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
			oos.writeObject(team);
			oos.flush();
			oos.close();
		} catch(Exception e) {
			CommonPlugin.LOGGER.log(Level.SEVERE, "Failed ot save team to disk", e);
		}
	}
	
	/**
	 * Get all registered teams by BKCommonLib
	 * 
	 * @return All registered teams
	 */
	public static CommonTeam[] getTeams() {
		return teams.values().toArray(new CommonTeam[0]);
	}
	
	/**
	 * Get a team by name
	 * 
	 * @param name Team name
	 * @return Team (null if not found)
	 */
	public static CommonTeam getTeam(String name) {
		return teams.get(name);
	}
	
	/**
	 * Copy the scoreboard
	 * 
	 * @param player New player
	 * @param from Old scoreboard
	 * @return New scoreboard
	 */
	public static CommonScoreboard copyFrom(Player player, CommonScoreboard from) {
		CommonScoreboard board = new CommonScoreboard(player);
		
		//Copy all objectives
		for(int i = 0; i < 3; i++) {
			Display display = Display.fromInt(i);
			board.objectives[i] = CommonObjective.copyFrom(board, from.getObjective(display));
		}
		
		return board;
	}
	
	public static enum Display {
		LIST(0, "list", "List"),
		SIDEBAR(1, "sidebar", "SideBar"),
		BELOWNAME(2, "belowname", "BelowName");
		
		private int id;
		private String name;
		private String displayName;
		
		Display(int id, String name, String displayName) {
			this.id = id;
			this.name = name;
			this.displayName = displayName;
		}
		
		public int getId() {
			return this.id;
		}
		
		public String getName() {
			return this.name;
		}
		
		public String getDisplayName() {
			return this.displayName;
		}
		
		public static Display fromInt(int from) {
			for(Display display : values()) {
				if(display.id == from)
					return display;
			}
			
			return null;
		}
	}
}