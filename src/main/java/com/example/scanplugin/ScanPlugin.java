package com.example.scanplugin;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.common.client.WrapperCommonClientSettings;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.InetSocketAddress;
import java.util.List;

public class ScanPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("ScanPlugin enabled (PacketEvents mode)");
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

        // ===== Bukkit API =====
        String name = target.getName();
        String uuid = target.getUniqueId().toString();
        int ping = target.getPing();
        String ip = "§7(недоступен)";
        if (target.getAddress() instanceof InetSocketAddress isa) {
            ip = isa.getAddress().getHostAddress();
        }

        // ===== PacketEvents =====
        String clientVersion = "§7неизвестно";
        String brand = "§7неизвестно";
        String locale = "§7неизвестно";
        int viewDistance = -1;
        String chatMode = "§7неизвестно";
        boolean chatColors = false;
        String mainHand = "§7неизвестно";
        String skinParts = "§7неизвестно";
        List<String> mods = List.of();

        User user = PacketEvents.getAPI().getPlayerManager().getUser(target);
        if (user != null) {
            ClientVersion v = user.getClientVersion();
            if (v != null) {
                clientVersion = v.getReleaseName(); // например "26.2" или "1.21.4"
            }

            String b = user.getClientBrand();
            if (b != null && !b.isEmpty()) {
                brand = b;
            }

            WrapperCommonClientSettings settings = user.getClientSettings();
            if (settings != null) {
                locale = settings.getLocale();
                viewDistance = settings.getViewDistance();
                chatMode = settings.getChatVisibility().toString();
                chatColors = settings.isChatColors();
                mainHand = settings.getMainHand().toString();
                skinParts = settings.getSkinMask().toString();
            }

            // Моды (если клиент шлёт каналы)
            try {
                var channels = user.getChannels();
                if (channels != null) {
                    mods = channels.stream()
                            .map(Object::toString)
                            .filter(s -> !s.startsWith("minecraft:"))
                            .toList();
                }
            } catch (Exception ignored) {
                // некоторые версии PacketEvents не дают getChannels()
            }
        }

        // ===== Вывод =====
        sender.sendMessage("§6=== §fScan: " + name + " §6===");
        sender.sendMessage("§eНик: §f" + name);
        sender.sendMessage("§eUUID: §f" + uuid);
        sender.sendMessage("§eIP: §f" + ip);
        sender.sendMessage("§eПинг: §f" + ping + " ms");
        sender.sendMessage("§eВерсия клиента: §f" + clientVersion);
        sender.sendMessage("§eClient Brand: §f" + brand);
        sender.sendMessage("§eЯзык: §f" + locale);
        sender.sendMessage("§eView distance: §f" + (viewDistance == -1 ? "неизвестно" : viewDistance + " чанков"));
        sender.sendMessage("§eChat mode: §f" + chatMode);
        sender.sendMessage("§eChat colors: §f" + (chatColors ? "вкл" : "выкл"));
        sender.sendMessage("§eMain hand: §f" + mainHand);
        sender.sendMessage("§eSkin parts: §f" + skinParts);
        if (!mods.isEmpty()) {
            sender.sendMessage("§eМоды (" + mods.size() + "): §f" + String.join(", ", mods));
        } else {
            sender.sendMessage("§eМоды: §7не обнаружены (vanilla или не шлёт)");
        }
        return true;
    }
}
