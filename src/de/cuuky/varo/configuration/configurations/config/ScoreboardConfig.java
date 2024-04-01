package de.cuuky.varo.configuration.configurations.config;

import java.util.ArrayList;
import java.util.List;

import de.varoplugin.cfw.player.hud.AnimationData;
import de.varoplugin.cfw.player.hud.UnmodifiableAnimationData;

public class ScoreboardConfig extends BoardConfig {

	private AnimationData<String> title;
	private AnimationData<String[]> scoreboard;

	public ScoreboardConfig() {
		super("plugins/Varo/config/scoreboard.yml");
	}

	@Override
	protected boolean shouldReset() {
		return this.configuration.contains("header");
	}
	
	@Override
	protected void load() {
		this.configuration.options().header("Die Liste alle Placeholder findest du unter /varo placeholder!");

		ArrayList<String> titleFrames = new ArrayList<>();
		titleFrames.add("%projectname%");

		ArrayList<String> firstFrame = new ArrayList<>();
		firstFrame.add("%space%");
		firstFrame.add("&7Team&8: %colorcode%%team%");
		firstFrame.add("&7Zeit&8: %colorcode%%min%&8:%colorcode%%sec%");
		firstFrame.add("&7Kills&8: %colorcode%%kills%");
		firstFrame.add("&7Teamkills&8: %colorcode%%teamKills%");
		firstFrame.add("%space%");
		firstFrame.add("&b&lBorder");
		firstFrame.add("&7Size: %colorcode%%bordersize%");
		firstFrame.add("&7Center: %colorcode%%centerDirection%");
		firstFrame.add("%space%");
		firstFrame.add("&b&lTop Players:");
		firstFrame.add("&71. %colorcode%%topplayer-1%");
		firstFrame.add("&72. %colorcode%%topplayer-2%");
		firstFrame.add("&73. %colorcode%%topplayer-3%");


		ArrayList<String> secondFrame = new ArrayList<>();
		secondFrame.add("%space%");
		secondFrame.add("&7Team&8: %colorcode%%team%");
		secondFrame.add("&7Zeit&8: %colorcode%%min%&8:%colorcode%%sec%");
		secondFrame.add("&7Kills&8: %colorcode%%kills%");
		secondFrame.add("&7Teamkills&8: %colorcode%%teamKills%");
		secondFrame.add("%space%");
		secondFrame.add("&b&lBorder");
		secondFrame.add("&7Size: %colorcode%%bordersize%");
		secondFrame.add("&7Center: %colorcode%%centerDirection%");
		secondFrame.add("%space%");
		secondFrame.add("&b&lTop Players:");
		secondFrame.add("&71. %colorcode%%topplayer-1%");
		secondFrame.add("&72. %colorcode%%topplayer-2%");
		secondFrame.add("&73. %colorcode%%topplayer-3%");

		ArrayList<ArrayList<String>> frames = new ArrayList<>();
		frames.add(firstFrame);
		frames.add(secondFrame);

		this.configuration.addDefault("title.updatedelay", 0);
		this.configuration.addDefault("title.content", titleFrames);
		this.configuration.addDefault("scoreboard.updatedelay", 100);
		this.configuration.addDefault("scoreboard.content", frames);

		this.title = new UnmodifiableAnimationData<>(this.configuration.getInt("title.updatedelay"), this.configuration.getStringList("title.content").toArray(new String[0]));
		this.scoreboard = toAnimationData(this.configuration.getInt("scoreboard.updatedelay"), this.configuration.getList("scoreboard.content"));
	}

	public AnimationData<String> getTitle() {
		return this.title;
	}

	public AnimationData<String[]> getScoreboard() {
		return this.scoreboard;
	}
	
	private static AnimationData<String[]> toAnimationData(int delay, List<?> frames) {
	    return new UnmodifiableAnimationData<>(delay, frames.stream().map(f -> {
	        String[] frameArray = ((List<?>) f).toArray(new String[0]);
	        
	        String space = "";
	        for (int i = 0; i < frameArray.length; i++)
	            if (frameArray[i].equals("%space%")) {
	                space += " ";
	                frameArray[i] = space;
	            }
	        return frameArray;
	    }).toArray(String[][]::new));
	}
}