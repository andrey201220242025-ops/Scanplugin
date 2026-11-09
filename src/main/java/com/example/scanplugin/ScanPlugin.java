package com.example.scanplugin;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.User;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class ScanPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("ScanPlugin enabled");
        if (Bukkit.getPluginManager().getPlugin("packetevents") == null) {
            getLogger().severe("PacketEvents не найден! /scan не будет работать.");
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("scan")) return false;
        if (!sender.hasPermission("scan.use")) {
            sender.sendMessage("§cНет прав.");
            return true;
        }
        if (args.length < 1) {
            sender.sendMessage("§cИспользование: /scan <ник>");
            return true;
        }
        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            sender.sendMessage("§cИгрок §f" + args[0] + " §cне найден.");
            return true;
        }

        String name = target.getName();
        String uuid = target.getUniqueId().toString();
        int ping = target.getPing();

        String ip = "недоступен";
        if (target.getAddress() instanceof InetSocketAddress isa && isa.getAddress() != null) {
            ip = isa.getAddress().getHostAddress();
        }

        String clientVersion = "неизвестно";
        try {
            User user = PacketEvents.getAPI().getPlayerManager().getUser(target);
            if (user != null) {
                ClientVersion v = user.getClientVersion();
                if (v != null) clientVersion = v.getReleaseName();
            }
        } catch (Throwable ignored) {
        }

        String brand = target.getClientBrandName();
        if (brand == null || brand.isEmpty()) brand = "неизвестно";

        String locale = String.valueOf(target.locale());
        int viewDistance = target.getClientViewDistance();
        String mainHand = String.valueOf(target.getMainHand());

        List<String> mods = new ArrayList<>();
        for (String ch : target.getListeningPluginChannels()) {
            if (!ch.startsWith("minecraft:")) mods.add(ch);
        }

        sender.sendMessage("§6=== §fScan: " + name + " §6===");
        sender.sendMessage("§eНик: §f" + name);
        sender.sendMessage("§eUUID: §f" + uuid);
        sender.sendMessage("§eIP: §f" + ip);
        sender.sendMessage("§eПинг: §f" + ping + " ms");
        sender.sendMessage("§eВерсия клиента: §f" + clientVersion);
        sender.sendMessage("§eClient Brand: §f" + brand);
        sender.sendMessage("§eЯзык: §f" + locale);
        sender.sendMessage("§eView distance: §f" + viewDistance + " чанков");
        sender.sendMessage("§eMain hand: §f" + mainHand);
        if (!mods.isEmpty()) {
            sender.sendMessage("§eКаналы модов (" + mods.size() + "): §f" + String.join(", ", mods));
        } else {
            sender.sendMessage("§eКаналы модов: §7не обнаружены");
        }
        return true;
    }
}
