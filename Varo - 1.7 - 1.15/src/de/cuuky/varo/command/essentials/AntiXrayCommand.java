package de.cuuky.varo.command.essentials;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import de.cuuky.varo.Main;
import de.cuuky.varo.configuration.configurations.messages.ConfigMessages;
import de.cuuky.varo.version.BukkitVersion;
import de.cuuky.varo.version.VersionUtils;

public class AntiXrayCommand implements CommandExecutor {

	private boolean antiXrayActivated;
	private byte xrayAvailable;

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!sender.hasPermission("varo.xray")) {
			sender.sendMessage(ConfigMessages.NOPERMISSION_NO_PERMISSION.getValue());
			return false;
		}
		antiXrayActivated = false;

		if(!VersionUtils.getVersion().isHigherThan(BukkitVersion.ONE_8)) {
			xrayAvailable = 0;
		} else if(!VersionUtils.getVersion().isHigherThan(BukkitVersion.ONE_13)) {
			xrayAvailable = 1;
		} else {
			xrayAvailable = 2;
		}

		if(VersionUtils.getSpigot() == null)
			if(xrayAvailable != 1)
				xrayAvailable = 2;

		YamlConfiguration spigotConfig = null;
		if(xrayAvailable == 0)
			try {
				Method m = VersionUtils.getSpigot().getClass().getMethod("getConfig");
				m.setAccessible(true);
				spigotConfig = (YamlConfiguration) m.invoke(VersionUtils.getSpigot());
				m.setAccessible(false);
			} catch(Exception e) {
				e.printStackTrace();
			}

		if(xrayAvailable == 0) {
			String enabled = spigotConfig.getString("world-settings.default.anti-xray.enabled");
			String engineMode = spigotConfig.getString("world-settings.default.anti-xray.engine-mode");
			if(enabled == null || engineMode == null) {
				sender.sendMessage(Main.getPrefix() + "§cEs gab einen Fehler mit dem Anti-Xray-System.");
				sender.sendMessage(Main.getPrefix() + "Dies kann daran liegen, dass du eine nicht-unterstuetzte Serverversion benutzt.");
				return false;
			}
			if(!enabled.contentEquals("true") || !engineMode.contentEquals("2")) {
				antiXrayActivated = false;
			} else {
				antiXrayActivated = true;
			}
		}

		if(xrayAvailable == 1) {
			File file = new File("plugins/Orebfuscator4", "config.yml");
			if(file.exists()) {
				YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
				String enabled = config.getString("Booleans.Enabled");
				String engineMode = config.getString("Integers.EngineMode");
				if(!enabled.contentEquals("true") || !engineMode.contentEquals("2")) {
					antiXrayActivated = false;
				} else {
					antiXrayActivated = true;
				}
			} else {
				sender.sendMessage(Main.getPrefix() + "Das Anti-Xray-Plugin wird installiert und der Server danach heruntergefahren.");

				boolean xrayDownload = Main.getDataManager().getPluginLoader().downloadAdditionalPlugin(22818, "plugins/Anti-Xray.jar", true);

				if(!xrayDownload) {
					sender.sendMessage(Main.getPrefix() + "Es gab einen kritischen Fehler beim Download des Plugins.");
					sender.sendMessage(Main.getPrefix() + "Du kannst dir das externe Plugin hier manuell herunterladen: https://www.spigotmc.org/resources/22818/");
					return false;
				}

				Bukkit.getServer().shutdown();
				return false;
			}
		}

		if(xrayAvailable == 2) {
			sender.sendMessage(Main.getPrefix() + "In deiner Serverversion ist kein Anti-Xray verfuegbar.");
			return false;
		}

		if(args.length != 1 || (!args[0].equalsIgnoreCase("on") && !args[0].equalsIgnoreCase("off"))) {
			sender.sendMessage(Main.getPrefix() + "§7Anti-Xray momentan §l" + (antiXrayActivated ? "aktiviert" : "deaktiviert"));
			sender.sendMessage(Main.getPrefix() + "§7/antixray on - Aktiviert den Schutz vor X-Ray");
			sender.sendMessage(Main.getPrefix() + "§7/antixray off - Deaktiviert den Schutz vor X-Ray");
			return false;
		}

		if(args[0].equalsIgnoreCase("on")) {
			if(antiXrayActivated) {
				sender.sendMessage(Main.getPrefix() + "§7Das Anti-Xray ist bereits aktiviert.");
				return false;
			} else {
				if(xrayAvailable == 0) {
					spigotConfig.set("world-settings.default.anti-xray.enabled", true);
					spigotConfig.set("world-settings.default.anti-xray.engine-mode", 2);

					try {
						spigotConfig.save("spigot.yml");
					} catch(IOException e) {
						sender.sendMessage(Main.getPrefix() + "§cFehler: §7Das Anti-Xray konnte nicht aktiviert werden.");
						return false;
					}
				} else if(xrayAvailable == 1) {
					Bukkit.dispatchCommand(sender, "ofc enable");
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "ofc engine 2");
				}

				sender.sendMessage(Main.getPrefix() + "§7Das Anti-Xray wurde aktiviert.");
			}
		} else if(args[0].equalsIgnoreCase("off")) {
			if(!antiXrayActivated) {
				sender.sendMessage(Main.getPrefix() + "§7Das Anti-Xray ist bereits deaktiviert.");
				return false;
			} else {
				if(xrayAvailable == 0) {
					spigotConfig.set("world-settings.default.anti-xray.enabled", true);
					spigotConfig.set("world-settings.default.anti-xray.engine-mode", 1);

					try {
						spigotConfig.save("spigot.yml");
					} catch(IOException e) {
						sender.sendMessage(Main.getPrefix() + "§cFehler: §7Das Anti-Xray konnte nicht deaktiviert werden.");
						return false;
					}
				} else if(xrayAvailable == 1) {
					Bukkit.dispatchCommand(sender, "ofc disable");
				}

				sender.sendMessage(Main.getPrefix() + "§7Das Anti-Xray wurde deaktiviert.");
			}
		}

		Bukkit.getServer().shutdown();
		return false;
	}
}