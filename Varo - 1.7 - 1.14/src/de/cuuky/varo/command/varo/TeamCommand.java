package de.cuuky.varo.command.varo;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import de.cuuky.varo.Main;
import de.cuuky.varo.command.VaroCommand;
import de.cuuky.varo.player.VaroPlayer;
import de.cuuky.varo.team.Team;
import de.cuuky.varo.utils.UUIDUtils;

public class TeamCommand extends VaroCommand {

	public TeamCommand() {
		super("team", "Hauptcommand f�r das Managen der Teams", "varo.teams", "teams");
	}

	@Override
	public void onCommand(CommandSender sender, VaroPlayer vp, Command cmd, String label, String[] args) {
		if(args.length == 0) {
			sender.sendMessage(Main.getPrefix() + Main.getProjectName() + " �7Team setup Kommands:");
			sender.sendMessage(Main.getPrefix() + Main.getColorCode() + "/varo team create �7<Teamname> <Spieler 1, 2, 3...>");
			sender.sendMessage(Main.getPrefix() + Main.getColorCode() + "/varo team remove �7<Team/TeamID/Player>");
			sender.sendMessage(Main.getPrefix() + Main.getColorCode() + "/varo team add �7<Player> <Team/TeamID>");
			sender.sendMessage(Main.getPrefix() + Main.getColorCode() + "/varo team rename �7<Team-Name> <Neuer Team-Name>");
			sender.sendMessage(Main.getPrefix() + Main.getColorCode() + "/varo team list");
			return;
		}

		if(args[0].equalsIgnoreCase("create")) {
			if(!(args.length >= 2)) {
				sender.sendMessage(Main.getPrefix() + "/varo team create <Teamname> [Spieler 1, Spieler 2, Spieler 3...]");
				return;
			}

			if(args[1].contains("#")) {
				sender.sendMessage(Main.getPrefix() + "Der Name darf kein '#' enthalten. (Wird automatisch hinzugef�gt)");
				return;
			}

			Team team = Team.getTeam(args[1]);
			if(team != null) {
				sender.sendMessage(Main.getPrefix() + "Dieses Team existiert bereits!");
				return;
			}

			team = new Team(args[1]);
			sender.sendMessage(Main.getPrefix() + "Team " + Main.getColorCode() + team.getName() + " �7mit der ID " + Main.getColorCode() + team.getId() + " �7erfolgreich erstellt!");

			for(String arg : args) {
				if(arg.equals(args[0]) || arg.equals(args[1]))
					continue;

				VaroPlayer varoplayer = VaroPlayer.getPlayer(arg);
				if(varoplayer == null) {
					String uuid = null;
					try {
						uuid = UUIDUtils.getUUID(arg).toString();
					} catch(Exception e) {
						sender.sendMessage(Main.getPrefix() + arg + " besitzt keinen Minecraft-Account!");
						continue;
					}

					varoplayer = new VaroPlayer(arg, uuid);
				}

				team.addMember(varoplayer);
				sender.sendMessage(Main.getPrefix() + "Spieler " + Main.getColorCode() + varoplayer.getName() + " �7erfolgreich hinzugef�gt!");
			}
			return;
		} else if(args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("delete")) {
			if(args.length != 2) {
				sender.sendMessage(Main.getPrefix() + "/varo team remove <Team/TeamID/Player>");
				return;
			}

			Team team = Team.getTeam(args[1]);
			VaroPlayer varoplayer = VaroPlayer.getPlayer(args[1]);

			if(team != null) {
				team.delete();
				sender.sendMessage(Main.getPrefix() + "Team erfolgreich gel�scht!");
			} else if(varoplayer != null) {
				varoplayer.getTeam().removeMember(varoplayer);
				sender.sendMessage(Main.getPrefix() + "Spieler " + Main.getColorCode() + varoplayer.getName() + " �7erfolgreich aus seinem Team entfernt!");
			} else
				sender.sendMessage(Main.getPrefix() + "Team, TeamID oder Spieler nicht gefunden!");
			return;
		} else if(args[0].equalsIgnoreCase("list")) {
			if(Team.getTeams().isEmpty()) {
				sender.sendMessage(Main.getPrefix() + "Keine Teams gefunden!");
				return;
			}

			sender.sendMessage(Main.getPrefix() + "�lListe aller " + Main.getColorCode() + " �lTeams�7�l:");
			for(Team team : Team.getTeams()) {
				sender.sendMessage(Main.getPrefix() + Main.getColorCode() + " �l" + team.getId() + "�7; " + Main.getColorCode() + team.getName());
				String list = "";
				for(VaroPlayer member : team.getMember())
					list = (list.isEmpty() ? member.getName() : list + "�7, " + Main.getColorCode() + member.getName());
				sender.sendMessage(Main.getPrefix() + "  " + list);
				sender.sendMessage(Main.getPrefix());
			}
			return;
		} else if(args[0].equalsIgnoreCase("rename")) {
			if(args.length != 3) {
				sender.sendMessage(Main.getPrefix() + Main.getColorCode() + "/varo team rename �7<Team/TeamID> <Team>");
				return;
			}

			if(args[1].startsWith("#") || args[2].startsWith("#")) {
				sender.sendMessage(Main.getPrefix() + "Deine Teamnamen d�rfen nicht mit " + Main.getColorCode() + "# �7anfangen!");
				return;
			}

			Team team = Team.getTeam(args[1]);

			if(team == null) {
				sender.sendMessage(Main.getPrefix() + "Team nicht gefunden!");
				return;
			}

			team.setName(args[2]);
			sender.sendMessage(Main.getPrefix() + "Das Team " + Main.getColorCode() + args[1] + " �7wurde erfolgreich in " + Main.getColorCode() + team.getName() + " �7umbenannt!");
		} else if(args[0].equalsIgnoreCase("add")) {
			if(args.length != 3) {
				sender.sendMessage(Main.getPrefix() + "/varo team add <Player> <Team/TeamID>");
				return;
			}

			VaroPlayer varoplayer = VaroPlayer.getPlayer(args[1]);
			Team team = Team.getTeam(args[2]);

			if(team == null) {
				sender.sendMessage(Main.getPrefix() + "Team nicht gefunden!");
				return;
			}

			if(varoplayer == null) {
				sender.sendMessage(Main.getPrefix() + "Spieler nicht gefunden!");
				return;
			}

			if(varoplayer.getTeam() != null) {
				if(varoplayer.getTeam().equals(team)) {
					sender.sendMessage(Main.getPrefix() + "Dieser Spieler ist bereits in diesem Team!");
					return;
				}

				varoplayer.getTeam().removeMember(varoplayer);
				sender.sendMessage(Main.getPrefix() + Main.getColorCode() + varoplayer.getName() + " �7wurde aus seinem jetzigen Team entfernt!");
			}

			team.addMember(varoplayer);
			sender.sendMessage(Main.getPrefix() + "Spieler " + Main.getColorCode() + varoplayer.getName() + " �7erfolgreich in das Team " + Main.getColorCode() + team.getName() + " �7gesetzt!");
			return;
		} else
			sender.sendMessage(Main.getPrefix() + "�7Kommand nicht gefunden! " + Main.getColorCode() + "/team");
		return;
	}
}