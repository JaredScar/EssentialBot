package store.badger.essentialbot.handlers;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.events.guild.GuildJoinEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import store.badger.essentialbot.api.API;
import store.badger.essentialbot.api.SQLHelper;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GuildJoinHandler extends ListenerAdapter {
    @Override
    public void onGuildJoin(GuildJoinEvent evt) {
        // ENTER servers into ServerList
        java.util.Date date = new Date(new java.util.Date().getTime());
        Object param = new java.sql.Timestamp(date.getTime());
        JDA jda = evt.getJDA();
        Guild[] guilds = new Guild[jda.getGuilds().size()];
        ResultSet res = null;
        SQLHelper helper = API.get().getHelper();
        try {
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
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        // END servers into ServerList
    }
}
