package store.badger.essentialbot.main;

import com.timvisee.yamlwrapper.YamlConfiguration;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Game;
import store.badger.essentialbot.api.API;
import store.badger.essentialbot.api.SQLHelper;
import store.badger.essentialbot.commands.*;
import store.badger.essentialbot.handlers.StickyHandler;
import store.badger.essentialbot.objects.StickyMessage;

import javax.security.auth.login.LoginException;
import java.io.File;
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
