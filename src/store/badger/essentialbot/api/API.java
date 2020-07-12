package store.badger.essentialbot.api;

import com.timvisee.yamlwrapper.YamlConfiguration;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.Role;

import java.awt.*;
import java.io.File;
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
