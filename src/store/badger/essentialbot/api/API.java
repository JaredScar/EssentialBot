package store.badger.essentialbot.api;

import com.timvisee.yamlwrapper.YamlConfiguration;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.Role;

import java.awt.*;
import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class API {
    private static API api = new API();
    private SQLHelper helper = null;
    private Random rand = new Random();
    public SQLHelper getHelper() {
        if (this.helper == null || !checkConnection()) {
            try {
                File configFile = new File("config.yml");
                YamlConfiguration config = YamlConfiguration.loadFromFile(configFile);
                String host = config.getString("Host");
                int port = config.getInt("Port");
                String db = config.getString("Database_Name");
                String username = config.getString("Username");
                String password = config.getString("Password");
                this.helper = new SQLHelper(host, port, db,
                        username, password);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        return this.helper;
    }
    private boolean checkConnection() {
        SQLHelper helper = this.helper;
        try {
            if(helper.getConn().isValid(10)) {
                return true;
            }
        } catch (SQLException e) {
            //e.printStackTrace();
        }
        return false;
    }
    public boolean isAdmin(Member mem) {
        if (mem != null && mem.getUser() != null && !mem.getUser().isFake() && !mem.getUser().isBot()) {
            if (mem.getPermissions().contains(Permission.ADMINISTRATOR)) {
                return true;
            }
            for (Role role : mem.getRoles()) {
                if (role.getPermissions().contains(Permission.ADMINISTRATOR)) {
                    return true;
                }
            }
        }
        return false;
    }
    public boolean unshadowBan(Member mem) {
        SQLHelper helper = getHelper();
        if (helper.runStatement("DELETE FROM `ShadowBans` WHERE `GuildID` = " +
                mem.getGuild().getIdLong() + " AND `UserID` = " + mem.getUser().getIdLong())) {
            return true;
        }
        return false;
    }
    public boolean shadowBan(Member mem, String reason) {
        SQLHelper helper = getHelper();
        try {
            PreparedStatement stmt = helper.getConn().prepareStatement("INSERT INTO `ShadowBans` VALUES (" +
                    "0, ?, ?, ?);");
            stmt.setLong(1, mem.getGuild().getIdLong());
            stmt.setLong(2, mem.getUser().getIdLong());
            stmt.setString(3, reason);
            stmt.execute();
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }
    public boolean isAlreadyShadowBanned(Member mem) {
        SQLHelper helper = getHelper();
        ResultSet res = helper.runQuery("SELECT COUNT(*) AS total FROM `ShadowBans` WHERE `GuildID` = " +
                mem.getGuild().getIdLong() + " AND `UserID` = " + mem.getUser().getIdLong());
        try {
            if (res.next()) {
                if (res.getInt("total") > 0) {
                    return true;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }
    public void cleanup(long guildID, long roleID) {
        // Check if all permissions are false, if they are, we can remove it all together
        try {
            SQLHelper helper = getHelper();
            ResultSet res = helper.runQuery("SELECT `permissionSticky`, `permissionKick`, `permissionShadowBan`, `permissionBan` " +
                    "FROM `RolePermissions` WHERE `GuildID` = " + guildID + " AND `RoleID` = " + roleID);
            if (res.next()) {
                boolean sticky = res.getBoolean(1);
                boolean kick = res.getBoolean(2);
                boolean shadowBan = res.getBoolean(3);
                boolean ban = res.getBoolean(4);
                if (!sticky && !kick && !shadowBan && !ban) {
                    // They are all false, remove it from the RolePermissions
                    helper.runStatement("DELETE FROM `RolePermissions` WHERE `GuildID` = " + guildID + " AND " +
                            "`RoleID` = " + roleID);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    public boolean[] togglePermission(long guildID, long roleID, String permission) {
        SQLHelper helper = getHelper();
        try {
            PreparedStatement stmt = helper.getConn().prepareStatement("SELECT `" + permission
                    + "` FROM `RolePermissions` WHERE " +
                    "`GuildID` = ? AND `RoleID` = ?");
            stmt.setLong(1, guildID);
            stmt.setLong(2, roleID);
            stmt.execute();
            ResultSet res = stmt.getResultSet();
            boolean contains = false;
            boolean currentPerm = false;
            if (res.next()) {
                currentPerm = res.getBoolean(permission);
                contains = true;
            }
            currentPerm = !currentPerm; // Inverse it
            if (contains) {
                // It already contains it, we need to update it
                stmt = helper.getConn().prepareStatement("UPDATE `RolePermissions` SET `" + permission + "` = ?" +
                        " WHERE `GuildID` = ? AND `RoleID` = ?;");
                stmt.setBoolean(1, currentPerm);
                stmt.setLong(2, guildID);
                stmt.setLong(3, roleID);
                if (!stmt.execute()) {
                    cleanup(guildID, roleID);
                    return new boolean[]{true, currentPerm};
                }
            } else {
                // Doesn't contain it, insert it
                stmt = helper.getConn().prepareStatement("INSERT INTO `RolePermissions` VALUES (0, ?, ?, ?, ?, ?, ?);");
                stmt.setLong(1, guildID);
                stmt.setLong(2, roleID);
                if (permission.equals("permissionSticky")) {
                    stmt.setBoolean(3, currentPerm);
                } else {
                    stmt.setBoolean(3, false);
                }
                if (permission.equals("permissionKick")) {
                    stmt.setBoolean(4, currentPerm);
                } else {
                    stmt.setBoolean(4, false);
                }
                if (permission.equals("permissionShadowBan")) {
                    stmt.setBoolean(5, currentPerm);
                } else {
                    stmt.setBoolean(5, false);
                }
                if (permission.equals("permissionBan")) {
                    stmt.setBoolean(6, currentPerm);
                } else {
                    stmt.setBoolean(6, false);
                }
                if (!stmt.execute()) {
                    return new boolean[]{true, currentPerm};
                }
            }
        } catch (SQLException e) {
            //e.printStackTrace();
        }
        return new boolean[] {false, false};
    }
    public boolean hasPermission(long guildID, Member mem, String permission) {
        List<Role> roles = mem.getRoles();
        SQLHelper helper = getHelper();
        ResultSet res = helper.runQuery("SELECT `RoleID` FROM `RolePermissions` WHERE `" + permission + "` = 1 AND `GuildID` = " + guildID);
        try {
            while (res.next()) {
                for (Role role : roles) {
                    if (res.getLong("RoleID") == role.getIdLong()) {
                        return true;
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }
    public static API get() {
        return api;
    }
    public EmbedBuilder getEmbed(Color color, String title, String desc,
                                 ArrayList<MessageEmbed.Field> fields, String footer, String footerImg, String thumbnail) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(color);
        if (title !=null) {
            embed.setTitle(title);
        }
        if(desc !=null) {
            embed.setDescription(desc);
        }
        if(fields !=null) {
            for (MessageEmbed.Field field : fields) {
                embed.addField(field);
            }
        }
        if(footer !=null) {
            embed.setFooter(footer, footerImg);
        }
        if(thumbnail !=null) {
            embed.setThumbnail(thumbnail);
        }
        return embed;
    }
    public EmbedBuilder getErrorEmbed(String errorMsg, Member mem) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(Color.RED);
        embed.setFooter(mem.getUser().getAsTag(), mem.getUser().getAvatarUrl());
        embed.setTitle("ERROR ENCOUNTERED");
        embed.addField("", "**" + errorMsg + "**", false);
        embed.setThumbnail("https://i.gyazo.com/2100c92bd7ac6040afdace35b1d0446e.png");
        return embed;
    }
    public EmbedBuilder getSuccessEmbed(String successMsg, Member mem) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(Color.GREEN);
        embed.setFooter(mem.getUser().getAsTag(), mem.getUser().getAvatarUrl());
        embed.setTitle("SUCCESS");
        embed.addField("", "**" + successMsg + "**", false);
        embed.setThumbnail("https://i.gyazo.com/2100c92bd7ac6040afdace35b1d0446e.png");
        return embed;
    }
    public EmbedBuilder getEssentialEmbed(String title, String desc, ArrayList<MessageEmbed.Field> fields,
                                          String footer, String footerImg) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(Color.ORANGE);
        if (title !=null) {
            embed.setTitle(title);
        }
        if(desc !=null) {
            embed.setDescription(desc);
        }
        if(fields !=null) {
            for (MessageEmbed.Field field : fields) {
                embed.addField(field);
            }
        }
        if(footer !=null) {
            embed.setFooter(footer, footerImg);
        }
        embed.setThumbnail("https://i.gyazo.com/2100c92bd7ac6040afdace35b1d0446e.png");
        return embed;
    }
}
