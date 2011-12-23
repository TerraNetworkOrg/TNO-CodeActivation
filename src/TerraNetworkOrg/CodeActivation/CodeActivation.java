package TerraNetworkOrg.CodeActivation;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;

import TerraNetworkOrg.CodeActivation.CodeActivationUtility;

@SuppressWarnings("deprecation")
public class CodeActivation extends JavaPlugin {
	
	public final static Logger log = Logger.getLogger("Minecraft");
	public static final String logprefix = "[CodeActivation 1.0.0]";
	
	private final CodeActivationUtility utils = new CodeActivationUtility(this);
	
	File configFile;
    File languageFile;
    File playerFile;
    Configuration config;
    Configuration language;
    Configuration cplayer;
	
    public Permission permission = null;
	public Economy economy = null;
	
public static void LogInfo(String Message) {
		
		log.info(logprefix + " " + Message);
		
	}
	
	public static void LogError(String Message) {
		
		log.log(Level.SEVERE, logprefix + " " + Message);
		
	}
	
	public static void LogWarning(String Message) {
		
		log.log(Level.WARNING, logprefix + " " + Message);
		
	}
	
	private Boolean setupPermissions()
    {
        RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider != null) {
            permission = permissionProvider.getProvider();
            LogInfo("succesfully connected permissions support with Vault");
        }
        return (permission != null);
    }
	
	private Boolean setupEconomy()
    {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
            LogInfo("succesfully connected economy support with Vault");
        }

        return (economy != null);
    }
	
	private void firstTimeCheck() {
        if(!getDataFolder().exists()){
            getDataFolder().mkdirs();
        }
        if(!configFile.exists()){
            InputStream inputThis = getClassLoader().getResourceAsStream("config.yml");
            try{
                utils.copy(inputThis, configFile);
                LogInfo("config.yml was created");
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        if(!languageFile.exists()){
            InputStream inputThis = getClassLoader().getResourceAsStream("language.yml");
            try{
                utils.copy(inputThis, languageFile);
                LogInfo("language.yml was created");
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        if(!playerFile.exists()){
            InputStream inputThis = getClassLoader().getResourceAsStream("player.yml");
            try{
                utils.copy(inputThis, playerFile);
                LogInfo("player.yml was created");
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }
	
	private Boolean loadConfig()
	{
		configFile = new File(getDataFolder().getPath()+"/config.yml");
        languageFile = new File(getDataFolder().getPath()+"/language.yml");
        playerFile = new File(getDataFolder().getPath()+"/player.yml");
        firstTimeCheck();
        config = new Configuration(configFile);
        language = new Configuration(languageFile);
        cplayer = new Configuration(playerFile);
        config.load();
        language.load();
        cplayer.load();

        if(config.getString("General.DefaultActivationCommand") == null)
        	config.setProperty("General.DefaultActivationCommand", "/pex user * group set Default");
        if(config.getString("General.DefaultState") == null)
        	config.setProperty("General.DefaultState", true);
        if(config.getString("General.DefaultUnique") == null)
        	config.setProperty("General.DefaultUnique", true);
        
        if (config.getString("Activation_Keys.Key") == null){
	        for (int i = 0; i < 5; i++) {
	        	String code = CodeActivationCodeGen.getNext();
	        	config.setProperty("Activation_Keys.Key." + code + ".active", config.getString("General.DefaultState"));
	        	config.setProperty("Activation_Keys.Key." + code + ".unique", config.getString("General.DefaultUnique"));
	        	config.setProperty("Activation_Keys.Key." + code + ".command", config.getString("General.DefaultActivationCommand"));
	        }
	        // Christmas special
        	config.setProperty("Activation_Keys.Key.christmasgift.active", true);
        	config.setProperty("Activation_Keys.Key.christmasgift.unique", false);
        	config.setProperty("Activation_Keys.Key.christmasgift.command", "/give * 354 1;/give * 371 1;/give * 357 1");
        }
        
        config.save();
		      
		return true;
	}

	private Boolean reloadConfigFile(Player player){
		
		config.load();
		return true;
		
	}
	
	private Boolean reloadLanguageFile(Player player){
		
		language.load();
		return true;
		
	}
	
	private Boolean reloadPlayerFile(Player player){
		
		cplayer.load();
		return true;
		
	}
	
	private Boolean reloadConfigs(Player player){
		
		reloadConfigFile(player);
		reloadLanguageFile(player);
		reloadPlayerFile(player);
		player.sendMessage(ChatColor.GREEN + "Config files successfully reloaded.");
		return true;
		
	}
	
	@SuppressWarnings("unused")
	private void reloadKeyGenerate(Player player){
		
		String code = CodeActivationCodeGen.getNext();
    	config.setProperty("Activation_Keys.Key." + code + ".active", config.getBoolean("General.DefaultState", true));
    	config.setProperty("Activation_Keys.Key." + code + ".unique", config.getBoolean("General.DefaultUnique", true));
    	config.setProperty("Activation_Keys.Key." + code + ".command", config.getString("General.DefaultActivationCommand"));
    	config.save();
		config.load();
		
	}
	
	private void showPluginInfo(Player player){
		player.sendMessage(ChatColor.GREEN + logprefix);
	}
	
	private void showActivationKeys(Player player){
		List<String> keyList = config.getKeys("Activation_Keys.Key");
		for (int i = 0; i < keyList.size(); i++) {
			player.sendMessage(ChatColor.GOLD + "" + keyList.get(i));
        }
	}
	
	private void regenerateActivationKeys(Player player){
		for (int i = 0; i < 10; i++) {
        	String code = CodeActivationCodeGen.getNext();
        	config.setProperty("Activation_Keys.Key." + code + ".active", config.getBoolean("General.DefaultState", true));
        	config.setProperty("Activation_Keys.Key." + code + ".unique", config.getBoolean("General.DefaultUnique", true));
        	config.setProperty("Activation_Keys.Key." + code + ".command", config.getString("General.DefaultActivationCommand"));
        	player.sendMessage(ChatColor.GOLD + code);
        }
        player.sendMessage(ChatColor.BLUE+"10 keys generated!");
        config.save();
        reloadConfigFile(player);
	}
	
	public void onEnable() {
		
		loadConfig();
		
		if (!setupPermissions()) {
			System.out.println("Null perm");
	        //use these if you require econ
	        //getServer().getPluginManager().disablePlugin(this);
	        //return;
	    }
		
		if (!setupEconomy()) {
			LogError("Was not able to find an economy plugin!");
           //use these if you require econ
          getServer().getPluginManager().disablePlugin(this);
          return;
        }
		
	}
	
	public void onDisable() {
		
		LogInfo("Plugin Disabled");
		
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		
		final Player player = (Player)sender;
		
		if(sender instanceof Player) {
			
			if(cmd.getName().equalsIgnoreCase("codeactivation")){
				if(args.length==0){
                    showPluginInfo(player);
                    return true;
				}else if(args.length==1){
					if(args[0].equalsIgnoreCase("activate") && permission.has(player, "codeactivation.user.activate")){
						
						player.sendMessage(ChatColor.RED+"Use /codeactivation activate KEY");
						return true;
						
					} else if(args[0].equalsIgnoreCase("list") && permission.has(player, "codeactivation.admin.list")){
						
						showActivationKeys(player);
						return true;
						
					} else if(args[0].equalsIgnoreCase("regenerate") && permission.has(player, "codeactivation.admin.regenerate")){
						
						regenerateActivationKeys(player);
						return true;
						
					} else if(args[0].equalsIgnoreCase("reload") && permission.has(player, "codeactivation.admin.reload")){
						
						reloadConfigs(player);
						return true;
						
					} else {
						
						player.sendMessage(ChatColor.RED+"Use a valid parameter: activate, list");
						return true;
						
					}
				}else if(args.length==2){
					if(args[0].equalsIgnoreCase("activate") && permission.has(player, "codeactivation.user.activate")){
						if(config.getKeys("Activation_Keys.Key").contains(args[1])){
							if (config.getString("Activation_Keys.Key." + args[1] + ".active") == "true" || config.getString("Activation_Keys.Key." + args[1] + ".active") == "'true'"){
								if (config.getString("Activation_Keys.Key." + args[1] + ".unique") == "false" || config.getString("Activation_Keys.Key." + args[1] + ".unique") == "'false'"){
									if (cplayer.getString("PlayerLog." + player.getName() + "." + args[1]) == null){
										String command = config.getString("Activation_Keys.Key." + args[1] + ".command");							
										handleCommand(this, player, command);
										player.sendMessage(ChatColor.YELLOW+"You were successfully activated!");
										cplayer.setProperty("PlayerLog." + player.getName() + "." + args[1], "true");
										//reloadKeyGenerate(player);
										cplayer.save();
										reloadPlayerFile(player);
										return true;
									}else{
										player.sendMessage(ChatColor.RED+"You already used this code!");
										return true;
									}
								}
								else{
									String command = config.getString("Activation_Keys.Key." + args[1] + ".command");							
									handleCommand(this, player, command);
									player.sendMessage(ChatColor.YELLOW+"You were successfully activated!");
									config.removeProperty("Activation_Keys.Key." + args[1]);
									//reloadKeyGenerate(player);
									config.save();
									reloadConfigFile(player);
									return true;
								}
							} else{
								player.sendMessage(ChatColor.RED+"This code is not active anymore!");
								return true;
							}
						} else{
							player.sendMessage(ChatColor.RED+"It's no a valid activation key!");
							return true;
						}
						
						
					} else {
						
						player.sendMessage(ChatColor.RED+"Use a valid parameter: activate, list");
						return true;
						
					}
				}
			}
			
		}
		
		return false;
	}
	
	protected static boolean handleCommand(CodeActivation plugin, Player player, String s) {
		if(s.length()<=1) return false;
		String[] commandList = s.split(";");
		CraftServer cs = (CraftServer)plugin.getServer();
		String command = "";
		
		for (int i=0; i < commandList.length; i++){
			String[] com = commandList[i].split(" ");
			for(String arg : com) {
				if(command.length()>0) {
					if("*".equals(arg)) arg = player.getName();
					command += " " + arg;
				}
				else {
					command += arg.substring(1);	
				}
			}
			LogError("executing Console-Command '"+command+"'");
			if (!cs.dispatchCommand(cs.getServer().console, command)) {
				LogError("Could not execute Console-Command '"+command+"'");
				return false;
			}
			command = "";
		}
		return true;
	}
	
}