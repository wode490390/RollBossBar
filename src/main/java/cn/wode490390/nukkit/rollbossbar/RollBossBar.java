package cn.wode490390.nukkit.rollbossbar;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.event.server.DataPacketReceiveEvent;
import cn.nukkit.network.protocol.SetLocalPlayerAsInitializedPacket;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.scheduler.NukkitRunnable;
import cn.nukkit.utils.DummyBossBar;
import cn.nukkit.utils.TextFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RollBossBar extends PluginBase implements Listener {

    private static final String CONFIG_DELAY = "delay-sec";
    private static final String CONFIG_TEXTS = "texts";

    private List<String> texts;
    private int delay;

    private final Map<Player, Long> bossbar = new HashMap<>();
    private int timer = 0;
    private int index = 0;

    private final Set<Player> initialized = new HashSet<>();

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        String cfg = CONFIG_DELAY;
        try {
            this.delay = this.getConfig().getInt(cfg);
        } catch (Exception e) {
            this.delay = 10;
            this.logLoadException(cfg);
        }
        if (this.delay < 1) {
            this.delay = 1;
        }
        cfg = CONFIG_TEXTS;
        try {
            this.texts = this.getConfig().getStringList(cfg);
        } catch (Exception e) {
            this.texts = new ArrayList<>();
            this.logLoadException(cfg);
        }
        if (this.texts.isEmpty()) {
            this.texts.add("&l&3Welcome, &o%NAME%");
        }
        for (int i = 0; i < this.texts.size(); i++) {
            this.texts.set(i, TextFormat.colorize(this.texts.get(i)
                    .replace("%MAX_PLAYERS%", Integer.toString(getServer().getMaxPlayers()))
                    .replace("%NEWLINE%", "\n")
            ));
        }

        this.getServer().getPluginManager().registerEvents(this, this);
        new NukkitRunnable() {
            @Override
            public void run() {
                if (++timer >= delay) {
                    timer = 0;
                    if (++index >= texts.size()) {
                        index = 0;
                    }
                }
                int percentage = 100 - Math.round((float) timer / delay * 100);
                getServer().getOnlinePlayers().values().forEach((player) -> {
                    Map<Long, DummyBossBar> bb = player.getDummyBossBars();
                    Long id = bossbar.get(player);
                    if (id == null) {

                    } else if (bb.containsKey(id)) {
                        player.updateBossBar(texts.get(index)
                                .replace("%NAME%", player.getName())
                                .replace("%X%", Integer.toString(player.getFloorX()))
                                .replace("%Y%", Integer.toString(player.getFloorY()))
                                .replace("%Z%", Integer.toString(player.getFloorZ()))
                                .replace("%DIRECTION%", player.getDirection().getName())
                                .replace("%HEALTH%", Float.toString(player.getHealth()))
                                .replace("%MAX_HEALTH%", Integer.toString(player.getMaxHealth()))
                                .replace("%FOOD_LEVEL%", Integer.toString(player.getFoodData().getLevel()))
                                .replace("%MAX_FOOD_LEVEL%", Integer.toString(player.getFoodData().getMaxLevel()))
                                .replace("%SATURATION_LEVEL%", Float.toString(player.getFoodData().getFoodSaturationLevel()))
                                .replace("%EXP%", Integer.toString(player.getExperience()))
                                .replace("%LEVEL%", Integer.toString(player.getExperienceLevel()))
                                .replace("%PING%", Integer.toString(player.getPing()))
                                .replace("%WORLD%", player.getLevel().getName())
                                .replace("%WORLD_TIME%", Integer.toString(player.getLevel().getTime()))
                                .replace("%PLAYERS%", Integer.toString(getServer().getOnlinePlayers().size()))
                                .replace("%TPS%", Float.toString(getServer().getTicksPerSecond()))
                                .replace("%DATE%", LocalDate.now().toString())
                                .replace("%TIME%", LocalTime.now().withNano(0).toString()),
                        percentage, id);
                    } else if (initialized.contains(player)) {
                        bossbar.put(player, createBossBar(player));
                    }
                });
            }
        }.runTaskTimerAsynchronously(this, 0, 20);
        new MetricsLite(this);
    }

    @EventHandler
    public void onDataPacketReceive(DataPacketReceiveEvent event) {
        if (event.getPacket() instanceof SetLocalPlayerAsInitializedPacket) {
            Player player = event.getPlayer();
            this.bossbar.put(player, this.createBossBar(player));
            this.initialized.add(player);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        this.initialized.remove(event.getPlayer());
    }

    private long createBossBar(Player player) {
        return player.createBossBar(TextFormat.colorize("&l&3Welcome, &o" + player.getName()), 50);
    }

    private void logLoadException(String node) {
        this.getLogger().alert("An error occurred while reading the configuration '" + node + "'. Use the default value.");
    }
}
