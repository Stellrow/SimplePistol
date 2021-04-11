package ro.Stellrow.SimplePistol;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class SimplePistolCommand implements CommandExecutor {
    private final SimplePistol pl;

    public SimplePistolCommand(SimplePistol pl) {
        this.pl = pl;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (commandSender.hasPermission("simplepistol.reload")){
            if (args.length==1&&args[0].equalsIgnoreCase("reload")) {
                pl.reloadData();
                commandSender.sendMessage(ChatColor.GREEN + "Config reloaded for SimplePistol");
                return true;
            }
            commandSender.sendMessage(ChatColor.GREEN+"Available commands: ");
            commandSender.sendMessage(ChatColor.GREEN+"/simplepistol reload");
        }
        return true;
    }
}
