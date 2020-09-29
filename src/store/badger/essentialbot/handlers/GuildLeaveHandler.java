package store.badger.essentialbot.handlers;

import net.dv8tion.jda.core.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import store.badger.essentialbot.api.API;
import store.badger.essentialbot.api.SQLHelper;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class GuildLeaveHandler extends ListenerAdapter {
    @Override
    public void onGuildLeave(GuildLeaveEvent evt) {
        SQLHelper helper = API.get().getHelper();
        long guildID = evt.getGuild().getIdLong();
        try {
            PreparedStatement stmt = helper.getConn().prepareStatement("DELETE FROM `ServerList` WHERE `guildID` = ?;");
            stmt.setLong(1, guildID);
            stmt.execute();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}
