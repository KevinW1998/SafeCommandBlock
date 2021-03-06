package me.KevinW1998.SafeCommandBlock;

import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CommandBlock;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;

import me.KevinW1998.SafeCommandBlock.utils.CommonCommandBridge;

public class SafeCommandBlock extends JavaPlugin {
	/*
	 * Permissions: SafeCommandBlock.access SafeCommandBlock.bypass
	 * 
	 * Features: - Whitelist or Blacklist Commands - World Whitelist (optional)
	 * - Commands can be set from players even if they not op and in creative
	 * mode - Set the name of a command block - Reset the command of a command
	 * block - With permission limitation
	 */

	public static final Logger MC_LOGGER = Logger.getLogger("Minecraft");
	public static SafeCommandBlock plugin;
	public ProtocolManager pm;
	public final ConfigManager ConfigSafeCommandBlock = new ConfigManager(this);
	public final SafeCommandBlockCommands commandExecuter = new SafeCommandBlockCommands(ConfigSafeCommandBlock);
	public final CommonCommandBridge commandBridge = new CommonCommandBridge(commandExecuter);
	
	public void onLoad() {
		pm = ProtocolLibrary.getProtocolManager();
				
		pm.addPacketListener(new PacketAdapter(this, PacketType.Play.Client.CUSTOM_PAYLOAD) {		
			@Override
			public void onPacketReceiving(PacketEvent event) {
				PacketContainer pack = event.getPacket();
				Player p = event.getPlayer();
				if (CommandPacketParser.isCommandBlockPacket(pack)) {
					event.setCancelled(true);
					if (p.hasPermission("SafeCommandBlock.access") || p.isOp()) {
						String cmd = CommandPacketParser.getCmd(pack);
						commandExecuter.setCommandSafe(p, cmd.split(" "));
					} else {
						p.sendMessage(ChatColor.RED + "You do not have permissions to access command blocks!");
					}
				}
			}
		});
		pm.addPacketListener(new PacketAdapter(this, PacketType.Play.Server.OPEN_WINDOW) {
			@Override
			public void onPacketSending(PacketEvent event) {
				PacketContainer container = event.getPacket();
				if(container.getType() == PacketType.Play.Server.OPEN_WINDOW){
					System.out.println("Packet sending: " + container.toString());
				}
			}
			
			@Override
			public void onPacketReceiving(PacketEvent event) {
				PacketContainer container = event.getPacket();
				if(container.getType() == PacketType.Play.Server.OPEN_WINDOW){
					System.out.println("Packet receiving: " + container.toString());
				}
			}
		});
	}

	public void onEnable() {
		PluginDescriptionFile pdfFile = this.getDescription();
		MC_LOGGER.info(pdfFile.getName() + " Version " + pdfFile.getVersion() + " Has Been Enabled!");
		ConfigSafeCommandBlock.rl();
	}

	public void onDisable() {
		PluginDescriptionFile pdfFile = this.getDescription();
		MC_LOGGER.info(pdfFile.getName() + " Has Been Disabled!");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		commandBridge.findAndExecuteCommand(sender, label, args);
		return false;
	}

}
