package store.badger.essentialbot.main;

import com.timvisee.yamlwrapper.YamlConfiguration;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Guild;
import store.badger.essentialbot.api.API;
import store.badger.essentialbot.api.SQLHelper;
import store.badger.essentialbot.commands.*;
import store.badger.essentialbot.handlers.GuildJoinHandler;
import store.badger.essentialbot.handlers.LockdownHandler;
import store.badger.essentialbot.handlers.ShadowHandler;
import store.badger.essentialbot.handlers.StickyHandler;
import store.badger.essentialbot.objects.StickyMessage;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Timer;
import java.util.TimerTask;

public class Main {
    private static Main main = new Main();
    private static JDA jdaPub = null;
    public static Main get() {
        return main;
    }
    public JDA getJDA() {
        return jdaPub;
    }
    public static void main(String[] args) throws LoginException, InterruptedException, SQLException {
        File configFile = new File("config.yml");
        YamlConfiguration config = YamlConfiguration.loadFromFile(configFile);
        String botToken = config.getString("BotToken");
        JDA jda = new JDABuilder(botToken)
                .addEventListener(StickyHandler.get())
                .addEventListener(new StickyCommand())
                .addEventListener(new HelpCommand())
                .addEventListener(new KickCommand())
                .addEventListener(new PermCommand())
                .addEventListener(new BanCommand())
                .addEventListener(new UnbanCommand())
                .addEventListener(new ShadowBanCommand())
                .addEventListener(new UnshadowBanCommand())
                .addEventListener(new ShadowHandler())
                .addEventListener(new LockdownHandler())
                .addEventListener(new LockdownCommand())
                .addEventListener(new SimpleCommands())
                .addEventListener(new ServerListCommand())
                .addEventListener(new ServerBumpCommand())
                .addEventListener(new GuildJoinHandler())
                .build();
        jda.awaitReady();
        jda.getPresence().setPresence(Game.playing(" " + jda.getGuilds().size() + " Guilds | =help"), true);
        jdaPub = jda;
        // LOAD StickyMessages
        SQLHelper helper = API.get().getHelper();
        ResultSet res = helper.runQuery("SELECT * FROM `Stickies`;");
        try {
            while (res.next()) {
                long guildID = res.getLong(2);
                long chanID = res.getLong(3);
                String content = res.getString(4);
                StickyMessage sticky = new StickyMessage(guildID, chanID, content);
                StickyHandler.get().addSticky(sticky);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        // END StickyMessages

        // ENTER servers into ServerList
        java.util.Date date = new Date(new java.util.Date().getTime());
        Object param = new java.sql.Timestamp(date.getTime());
        Guild[] guilds = new Guild[jda.getGuilds().size()];
        PreparedStatement stmt = helper.getConn().prepareStatement("SELECT `GuildID` FROM `ServerList`;");
        try {
            stmt.execute();
            res = stmt.getResultSet();
            int count = 0;
            while (res.next()) {
                long guildID = res.getLong(1);
                guilds[count] = jda.getGuildById(guildID);
                count++;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        for (Guild guild : jda.getGuilds()) {
            boolean inSQL = false;
            for (Guild guildInSQL : guilds) {
                if (guildInSQL != null) {
                    if (guild.getIdLong() == guildInSQL.getIdLong()) inSQL = true;
                }
            }
            try {
                if (!inSQL) {
                    // It's not in SQL, we submit it
                    stmt = helper.getConn().prepareStatement("INSERT INTO `ServerList` VALUES (0, ?, ?, ?, 1);");
                    stmt.setLong(1, guild.getIdLong());
                    stmt.setString(3, guild.getSystemChannel().createInvite().setTemporary(false).setMaxAge(0).submit().get().getCode());
                    stmt.setObject(2, param);
                    try {
                        stmt.execute();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        // END servers into ServerList
        Timer task = new Timer();
        task.scheduleAtFixedRate(new TimerTask() {
            int displayStatus = 0;
            @Override
            public void run() {
                if (displayStatus == 0) {
                    jda.getPresence().setPresence(Game.watching(" " + jda.getGuilds().size() + " Guilds | =help"), true);
                    displayStatus = 1;
                } else {
                    jda.getPresence().setPresence(Game.of(Game.GameType.DEFAULT, "https://github.com/jaredscar"), true);
                    displayStatus = 0;
                }
            }
        }, 0L, 1000*60*10L); // updates every 10 minutes
        System.out.println("BOT IS RUNNING");
    }
}
