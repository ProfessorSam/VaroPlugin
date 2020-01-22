package de.cuuky.varo;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import de.cuuky.varo.alert.Alert;
import de.cuuky.varo.alert.AlertType;
import de.cuuky.varo.bot.BotLauncher;
import de.cuuky.varo.configuration.config.ConfigEntry;
import de.cuuky.varo.data.BukkitRegisterer;
import de.cuuky.varo.data.DataManager;
import de.cuuky.varo.logger.logger.ConsoleLogger;
import de.cuuky.varo.threads.DailyTimer;
import de.cuuky.varo.utils.JavaUtils;
import de.cuuky.varo.utils.VaroUtils;
import de.cuuky.varo.version.VersionUtils;

public class Main extends JavaPlugin {

	/*
	 * Plugin by Cuuky @ 2019 All rights reserved! Contributors: Korne127
	 */

	private static BotLauncher botLauncher;
	private static final String CONSOLE_PREFIX = "[Varo] ";

	private static DataManager dataManager;
	private static Main instance;

	private boolean failed;

	public File getThisFile() {
		return getFile();
	}
	
	@Override
	public void onDisable() {
		System.out.println(CONSOLE_PREFIX + "--------------------------------");
		System.out.println(CONSOLE_PREFIX + " ");
		System.out.println(CONSOLE_PREFIX + "Disabling " + this.getDescription().getName() + "...");

		if(dataManager != null && !failed) {
			System.out.println(CONSOLE_PREFIX + "Saving files...");
			dataManager.save();
		}

		if(botLauncher != null) {
			System.out.println(CONSOLE_PREFIX + "Disconnecting bots...");
			botLauncher.disconnect();
		}

		if(!failed)
			VersionUtils.getOnlinePlayer().forEach(pl -> pl.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard()));
		Bukkit.getScheduler().cancelTasks(this);

		System.out.println(CONSOLE_PREFIX + "Disabled!");
		System.out.println(CONSOLE_PREFIX + " ");
		System.out.println(CONSOLE_PREFIX + "--------------------------------");
		super.onDisable();
	}

	@Override
	public void onEnable() {
		System.out.println("############################################################################");
		System.out.println("#                                                                          #");
		System.out.println("#  #     #                         ######                                  #");
		System.out.println("#  #     #   ##   #####   ####     #     # #      #    #  ####  # #    #   #");
		System.out.println("#  #     #  #  #  #    # #    #    #     # #      #    # #    # # ##   #   #");
		System.out.println("#  #     # #    # #    # #    #    ######  #      #    # #      # # #  #   #");
		System.out.println("#   #   #  ###### #####  #    #    #       #      #    # #  ### # #  # #   #");
		System.out.println("#    # #   #    # #   #  #    #    #       #      #    # #    # # #   ##   #");
		System.out.println("#     #    #    # #    #  ####     #       ######  ####   ####  # #    #   #");
		System.out.println("#                                                                          #");
		System.out.println("#                               by Cuuky                                   #");
		System.out.println("#                                                                          #");
		System.out.println("#                             Contributors:                                #");
		System.out.println("#                               Korne127                                   #");
		System.out.println("#                                                                          #");
		System.out.println("############################################################################");

		System.out.println(CONSOLE_PREFIX);
		System.out.println(CONSOLE_PREFIX + "Enabling " + getPluginName() + "...");
		System.out.println(CONSOLE_PREFIX + "Running on " + Bukkit.getVersion());
		System.out.println(CONSOLE_PREFIX + "Other plugins enabled: " + (Bukkit.getPluginManager().getPlugins().length - 1));

		try {
			dataManager = DataManager.getInstance(); // Initialisierung

			Object[] updater2 = VaroUtils.checkForUpdates();
			VaroUtils.UpdateResult result = (VaroUtils.UpdateResult) updater2[0];
			String updateVersion = (String) updater2[1];
			System.out.println(Main.getConsolePrefix() + "Updater: " + result.getMessage());
			if(result == VaroUtils.UpdateResult.UPDATE_AVAILABLE)
				new Alert(AlertType.UPDATE_AVAILABLE, "§cEine neue Version des Plugins ( " + updateVersion + ") ist verfügbar!\n§7Im Regelfall kannst du dies ohne Probleme installieren, bitte\n§7informiere dich dennoch auf dem Discord-Server.");
			DailyTimer.startTimer();

			botLauncher = BotLauncher.getInstance(); // Initialisierung
			BukkitRegisterer.registerEvents();
			BukkitRegisterer.registerCommands();
		} catch(Exception e) {
			e.printStackTrace();
			failed = true;
			Bukkit.getPluginManager().disablePlugin(Main.this);
		}

		if(failed)
			return;

		System.out.println(CONSOLE_PREFIX + "Enabled!");
		System.out.println(CONSOLE_PREFIX + " ");
		System.out.println(CONSOLE_PREFIX + "--------------------------------");
		super.onEnable();
	}
	
	@Override
	public void onLoad() {
		failed = false;
		instance = this;

		new ConsoleLogger();
		super.onLoad();
	}

	public static void broadcastMessage(String message) {
		Bukkit.broadcastMessage(getPrefix() + message);
	}

	public static String getColorCode() {
		return ConfigEntry.PROJECTNAME_COLORCODE.getValueAsString();
	}

	public static String getConsolePrefix() {
		return CONSOLE_PREFIX;
	}

	public static String getContributors() {
		return JavaUtils.getArgsToString(JavaUtils.removeString(JavaUtils.arrayToCollection(instance.getDescription().getAuthors()), 0), ",");
	}

	public static Main getInstance() {
		return instance;
	}

	public static String getPluginName() {
		return instance.getDescription().getName() + " v" + instance.getDescription().getVersion() + " by " + instance.getDescription().getAuthors().get(0) + ", Contributors: " + getContributors();
	}

	public static String getPrefix() {
		return ConfigEntry.PREFIX.getValueAsString();
	}

	public static String getProjectName() {
		return getColorCode() + ConfigEntry.PROJECT_NAME.getValueAsString();
	}

	public static boolean isBootedUp() {
		return dataManager != null;
	}
}