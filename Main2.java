
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
	public static String fisi_role_name = "FISI";
	public static String fiae_role_name = "FIAE";
	public static String itk_role_name = "ITK";
	public static String grp1_name = "US_IT-33.1";
	public static String grp2_name = "US_IT-33.2";
	public static String grp3_name = "US_IT-34.1";
	public static String grp4_name = "US_IT-34.2";
	public static String grp5_name = "US_IT-34.3";
	
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
		
		String cmdStr = "(";
		
		for (String cmds : kommandos) {
			
			cmdStr = cmdStr + cmds + "|";
		}
		
		String test = "^!" + cmdStr.substring(0, cmdStr.length()-1) + ")=?(.*)";
		
		System.out.print(test);
		
		
		Pattern CMD = Pattern.compile(test);
		
		Matcher CMDmatcher = CMD.matcher(command.trim());
		
		//Test ob Bot Kommando auswerten kann/muss
		if(CMDmatcher.find()) {
		
			switch (CMDmatcher.group(1)) {
			
			case "Name":
				
				controller.setNickname(event.getMember(), CMDmatcher.group(2).trim()).queue();
				
				break;
	
			case "Fachrichtung":
			
			case "Gruppe":
		
				setRole(controller, event, CMDmatcher.group(2).trim());
				
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
		
		Pattern regex = Pattern.compile("[\\s]*!([A-z]+[\\s]+[A-z]+)[\\s]+!(" + 
				fisi_role_name + "|" + 
				fiae_role_name + "|" +
				itk_role_name +
				")[\\s]+!(" + 
				grp1_name + "|" + 
				grp2_name + "|" +
				grp3_name + "|" +
				grp4_name + "|" +
				grp5_name +
				")[\\s]+!([A-z]+)[\\s]*");
		
		Matcher matcher = regex.matcher(command);
		
		if(!matcher.matches()) {
			
			System.out.println("Nachricht von " +
					event.getAuthor().getName() + ", " + command );
			
			return;
		}
		
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
				 ((commands[1].equals(fisi_role_name)) ? "Fachinformatiker für Systemintegration" : (command.equals(fiae_role_name) ? "Fachinformatiker für Anwendungsentwicklung" : "IT Systemkaufmann")) +  
				 " am Standort " + commands[3] + 
				 " und ist dem Server gejoined.").queue();
		 
		 //persönliche Rückmeldung
		 event.getAuthor().openPrivateChannel().queue((channel) -> 
		 	channel.sendMessage(event.getAuthor().getName() + ", du wurdest zugewiesen. Viel Spaß!").queue() );
		 
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
	 
	public void setRole( GuildController controller, MessageReceivedEvent event, String role_cmd) {
		
		net.dv8tion.jda.core.entities.Member member = event.getMember();
		List<Role> memberRoles = member.getRoles();
		
		AuditableRestAction<Void> request = null;
		
		if(role_cmd.equals("FISI") || role_cmd.equals("FIAE") || role_cmd.equals("ITK")) {
		
			for (Role role : memberRoles) {
				
				String name = role.getName();
				
				if(name.equals(role_cmd)) return;
				
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
	
	public Role getRolebyCMD( MessageReceivedEvent event, String cmd) {

		List<Role> serverRoles = event.getGuild().getRoles();
		
		Hashtable<String, Role> serverrollenbyName = new Hashtable<String, Role>();
		
		//Rollen aus Rollennamen beziehen
		for (Role i : serverRoles) {
			
			System.out.println(i.getName());
			
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
