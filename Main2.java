
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.managers.GuildController;
import net.dv8tion.jda.core.requests.restaction.AuditableRestAction;

/**
 * 
 * @author Philipp Köbernick
 *
 */


public class Main2 extends ListenerAdapter{
	
	//der Name der Rolle die zugewiesen wird
	public static String[] role_names = {"FISI", "FIAE", "ITK"};

	public static String[] grp_names = {"US_IT-33.1", "US_IT-33.2", "US_IT-34.1", "US_IT-34.2", "US_IT-34.3"};
	
	public static String[] kommandos = {"Name", "Fachrichtung", "Gruppe", "Standort", "Info"};
	
	public static String info = "!Name ... \t-um deinen Namen zu ändern\r\n" + 
								"!Fachrichtung ...\t-um deine Fachrichtung zu ändern(Befehle FISI/FIAE/ITK)\r\n"+
								"!Gruppe ...\t-um deine Gruppe zu ändern(Befehle US_IT-33.1/US_IT-33.2/US_IT-34.1/US_IT-34.2/US_IT-34.3)\r\n" + 
								"!Standort ...\t-um deinen Standort zu ändern\r\n" +
								"!Name Vorname !Fachrichtung !Gruppe !Standort\t-um alles zu ändern.";
	
	

	public static void main(String[] args) throws LoginException {
		
		JDABuilder builder = new JDABuilder(AccountType.BOT);
		// TODO bot token durch discord beziehen -> applications page
		String token = "";
		builder.setToken(token);
		builder.addEventListener(new Main2());
		builder.buildAsync();
		
		
	}
	
	//Rollenzuweiseung bei Nachricht mit Kommando
	public void onMessageReceived(MessageReceivedEvent event) {
		
		GuildController controller = event.getGuild().getController();
		String command = event.getMessage().getContentRaw();
		
		//Test ob Bot Kommando auswerten kann/muss
		if(!command.startsWith("!")) {
			
			return;
		}
		
		//simple Kommando Regex 
		String cmdStr = "(";
		
		for (String cmds : kommandos) {
			
			cmdStr = cmdStr + cmds + "|";
		}
		
		String test = "^!" + cmdStr.substring(0, cmdStr.length()-1) + ")=?(.*)";
		Pattern CMD = Pattern.compile(test);
		Matcher CMDmatcher = CMD.matcher(command.trim());
		
		//Test ob Bot simple Kommando auswerten kann/muss
		if(CMDmatcher.find()) {
		
			//Kommandoauswertung
			switch (CMDmatcher.group(1)) {
			
			case "Name":
				
				controller.setNickname(event.getMember(), CMDmatcher.group(2).trim()).queue();
				
				break;
	
			case "Fachrichtung":
			
			case "Gruppe":
		
				setRole(controller, event, CMDmatcher.group(2).trim());
				
				event.getGuild().getDefaultChannel().sendMessage(event.getAuthor().getName() + " ist nun " + CMDmatcher.group(2).trim()).queue();
				
				break;
	
			case "Standort":
		
				controller.setNickname(event.getMember(), event.getMember().getNickname() + "/" + CMDmatcher.group(2).trim()).queue();
				break;
	
			case "Info":
				
				event.getAuthor().openPrivateChannel().queue((channel) -> 
			 	channel.sendMessage(info).queue() );
				
				break;
				
			default:
				break;
			}
		
		}
		
		//complexe Kommando regex
		String complexCMDreg = "[\\s]*!([A-z]+[\\s]+[A-z]+)[\\s]+!(";
		String role_nameStr = "";
		String group_nameStr = "";
		
		for (String string : role_names) {
			
			role_nameStr = role_nameStr + string + "|";
			
		}
		
		complexCMDreg = complexCMDreg + role_nameStr.substring(0, role_nameStr.length()-1) + ")[\\s]+!(";
				
		for (String string : grp_names) {
			
			group_nameStr = group_nameStr + string + "|";
			
		}
			
		complexCMDreg = complexCMDreg + group_nameStr.substring(0, group_nameStr.length()-1) + ")[\\s]+!([A-z]+)[\\s]*";
		
		Pattern regex = Pattern.compile(complexCMDreg);
		Matcher matcher = regex.matcher(command);
		
		//Test ob Bot komplexe Kommando auswerten kann/muss
		if(!matcher.matches()) {
			
			String[] commands = new String[4];
			
			commands[0] = (matcher.group(1));
			commands[1] = (matcher.group(2));
			commands[2] = (matcher.group(3));
			commands[3] = (matcher.group(4));
			
			System.out.println("Nachricht von " +
					"Name :" + commands[0] + " Ausbildung :" + commands[1] + " Gruppe  :" + commands[2] + " Standort  :" + commands[3] );
			
			//Eigenschaften setzen 
			controller.setNickname(event.getMember(), 
					commands[0] +  "/" + commands[3]).queue();
			System.out.println("Name vergeben");
		
			//Rollen setzen - TODO funktioniert ? zwei Rollenvergaben in einem circle
			setRole(controller, event, commands[1]);
			setRole(controller, event, commands[2]);
			 
			//Allchat Meldung
			 event.getGuild().getDefaultChannel().sendMessage(commands[0] + "ist in der Gruppe " + commands[2] + " lernt " +
					 ((commands[1].equals(role_names[0])) ? "Fachinformatiker für Systemintegration" : (command.equals(role_names[1]) ? "Fachinformatiker für Anwendungsentwicklung" : "IT Systemkaufmann")) +  
					 " am Standort " + commands[3] + 
					 " und ist dem Server gejoined.").queue();
			 
			 //persönliche Rückmeldung
			 event.getAuthor().openPrivateChannel().queue((channel) -> 
			 	channel.sendMessage(event.getAuthor().getName() + ", du wurdest zugewiesen. Viel Spaß!").queue() );
		}
		
		 
	}
	
	public void onGuildMemberJoin(GuildMemberJoinEvent event) {
		
		String text = "Hallo, " + event.getUser().getName() + 
				" bitte schreib im allgemeinen Chat " + 
				"deinen Namen,Ausbildungsrichtung und Standort in der Form" + 
				"\"!Name !FISI !Gruppe !Standort\" wenn du ein Fachinformatiker für Systemintegration bist" + 
				" , \"!Name !FIAE !Gruppe !Standort\" wenn du ein Fachinformatiker für Anwendungsentwickler bist" +
				" oder \"!Name !ITK !Gruppe !Standort\" wenn du ein IT Systemkaufmann bist." +
				"für einzelne Änderungen bitte schreib !info.";
		
		event.getUser().openPrivateChannel().queue((channel) -> channel.sendMessage(text).queue() );
		
	}
	 
	/**
	 * spezielle Rollenvergabe um mehrere Rollen pro User sinnvoll zu handlen
	 * 
	 * @param controller
	 * @param event
	 * @param role_cmd
	 */
	public void setRole( GuildController controller, MessageReceivedEvent event, String role_cmd) {
		
		net.dv8tion.jda.core.entities.Member member = event.getMember();
		List<Role> memberRoles = member.getRoles();
		
		AuditableRestAction<Void> request = null;
		
		//Test auf Rollen in Besitz des Users
		if(role_cmd.equals("FISI") || role_cmd.equals("FIAE") || role_cmd.equals("ITK")) {
		
			for (Role role : memberRoles) {
				
				String name = role.getName();
				
				if(name.equals(role_cmd)) return;
				
				//Rollenänderung bei verwandter Rolle
				if(name.equals("Systemintegrator") || name.equals("Anwendungsentwickler") || name.equals("IT Systemkaufmann")) {
					
					request = controller.modifyMemberRoles(member, getRolebyCMD(event, role_cmd), role);
					
					break;
				}
				
			}
			
			if(request == null) {
				
				request = controller.addRolesToMember(member, getRolebyCMD(event, role_cmd));
			}
			
		} else {
			
			for (Role role : memberRoles) {
				
				String name = role.getName();
				
				if(name.equals(role_cmd)) return;
				
				//Rollenänderung bei verwandter Rolle
				if(name.equals("US_IT-33.1") || name.equals("US_IT-33.2") || name.equals("US_IT-34.1") || name.equals("US_IT-34.2") || name.equals("US_IT-34.3")) {
					
					request = controller.modifyMemberRoles(member, getRolebyCMD(event, role_cmd), role);
					
					break;
				}
			}
			
			if(request == null) {
				
				request = controller.addRolesToMember(member, getRolebyCMD(event, role_cmd));
			}
		}
		
		request.queue();
		
		System.out.println("Rolle vergeben");
		
	}
	
	/**
	 * Workaround für die Tatsache das nicht jeder Befehl seiner Rolle entspricht (z.B. FISI für Systemintegrator)
	 * 
	 * @param event
	 * @param cmd
	 * @return
	 */
	public Role getRolebyCMD( MessageReceivedEvent event, String cmd) {

		List<Role> serverRoles = event.getGuild().getRoles();
		
		Hashtable<String, Role> serverrollenbyName = new Hashtable<String, Role>();
		
		//Rollen aus Rollennamen beziehen und unter ihrem Befehl(Key) erhalten
		for (Role i : serverRoles) {
			
			switch (i.getName()) {
			
			case "Systemintegrator":
				
				serverrollenbyName.put("FISI", i);
				break;

			case "Anwendungsentwickler":
				
				serverrollenbyName.put("FIAE", i);
				break;
				
			case "IT Systemkaufmann":
				
				serverrollenbyName.put("ITK", i);
				break;
				
			default:
				
				serverrollenbyName.put(i.getName(), i);
				break;
			}
			
		}
		
		return serverrollenbyName.get(cmd);
	}
}
