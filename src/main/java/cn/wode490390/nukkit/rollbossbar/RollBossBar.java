package cn.wode490390.nukkit.rollbossbar;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.scheduler.NukkitRunnable;
import cn.nukkit.utils.DummyBossBar;
import cn.nukkit.utils.TextFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RollBossBar extends PluginBase implements Listener {

    private List<String> texts = new ArrayList<>();
    private int delay = 10;

    private final Map<Player, Long> bb = new HashMap<>();
    private int timer = 0;
    private int index = 0;

    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(this, this);
        this.saveDefaultConfig();
        String cfg = "delay-sec";
        try {
            this.delay = this.getConfig().getInt(cfg);
        } catch (Exception e) {
            this.logLoadException(cfg);
        }
        if (this.delay < 1) {
            this.delay = 1;
        }
        cfg = "texts";
        try {
            this.texts = this.getConfig().getStringList(cfg);
        } catch (Exception e) {
            this.logLoadException(cfg);
        }
        if (this.texts.isEmpty()) {
            this.texts.add("&l&3Welcome, &o%NAME%");
        }
        for (int i = 0; i < this.texts.size(); i++) {
            this.texts.set(i, TextFormat.colorize(this.texts.get(i)));
        }
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
                for (Player p : getServer().getOnlinePlayers().values()) {
                    Map<Long, DummyBossBar> dbb = p.getDummyBossBars();
                    long id = bb.containsKey(p) ? bb.get(p) : -1;
                    if (id == -1) {

                    } else if (dbb.containsKey(id)) {
                        p.updateBossBar(texts.get(index)
                                .replace("%NAME%", p.getName())
                                .replace("%X%", Integer.toString(p.getFloorX()))
                                .replace("%Y%", Integer.toString(p.getFloorY()))
                                .replace("%Z%", Integer.toString(p.getFloorZ()))
                                .replace("%PING%", Integer.toString(p.getPing()))
                                .replace("%HEALTH%", Float.toString(p.getHealth()))
                                .replace("%EXP%", Integer.toString(p.getExperience()))
                                .replace("%LEVEL%", Integer.toString(p.getExperienceLevel()))
                                .replace("%WORLD%", p.getLevel().getName())
                                .replace("%MAXPLAYERS%", Integer.toString(getServer().getMaxPlayers()))
                                .replace("%TPS%", Float.toString(getServer().getTicksPerSecond())),
                        percentage, id);
                    } else {
                        bb.put(p, createBossBar(p));
                    }
                }
            }
        }.runTaskTimerAsynchronously(this, 0, 20);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        this.bb.put(p, this.createBossBar(p));
    }

    private long createBossBar(Player p) {
        return p.createBossBar(TextFormat.colorize("&l&3Welcome, &o" + p.getName()), 50);
    }

    private void logLoadException(String text) {
        this.getLogger().alert("An error occurred while reading the configuration '" + text + "'. Use the default value.");
    }
}
