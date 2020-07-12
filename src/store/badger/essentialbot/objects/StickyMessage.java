package store.badger.essentialbot.objects;

import store.badger.essentialbot.api.API;
import store.badger.essentialbot.api.SQLHelper;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class StickyMessage {
    long guildID;
    long chanID;
    String content;
    public StickyMessage(long guildID, long chanID, String content) {
        this.guildID = guildID;
        this.chanID = chanID;
        this.content = content;
    }
    public long getGuildID() {
        return guildID;
    }
    public long getChanID() {
        return chanID;
    }
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
    public void save() {
        SQLHelper helper = API.get().getHelper();
        boolean update = false;
        try {
            PreparedStatement stmt = helper.getConn().prepareStatement("SELECT COUNT(*) AS total FROM `Stickies` WHERE `GuildID` = ? AND `ChannelID` = ?");
            stmt.setLong(1, guildID);
            stmt.setLong(2, chanID);
            stmt.execute();
            ResultSet res = stmt.getResultSet();
            if (res.next()) {
                int count = res.getInt("total");
                if (count > 0) {
                    update = true;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            if (update) {
                // Update it
                PreparedStatement stmt = helper.getConn().prepareStatement("UPDATE `Stickies` SET `Content` = ? WHERE `GuildID` = ? AND `ChannelID` = ?");
                stmt.setString(1, content);
                stmt.setLong(2, guildID);
                stmt.setLong(3, chanID);
                stmt.execute();
            } else {
                // Insert it
                PreparedStatement stmt = helper.getConn().prepareStatement("INSERT INTO `Stickies` VALUES (0, ?, ?, ?);");
                stmt.setLong(1, guildID);
                stmt.setLong(2, chanID);
                stmt.setString(3, content);
                stmt.execute();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    public void delete() {
        SQLHelper helper = API.get().getHelper();
        helper.runStatement("DELETE FROM `Stickies` WHERE `ChannelID` = " + chanID + " AND `GuildID` = " + guildID);
    }
}
