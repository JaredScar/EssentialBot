package store.badger.essentialbot.commands;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import store.badger.essentialbot.api.API;
import store.badger.essentialbot.main.Main;

import java.awt.*;
import java.util.*;

public class ServerListCommand extends ListenerAdapter {
    private ArrayList<String> pages = new ArrayList<>();
    private HashMap<Long, Integer> pageTracker = new HashMap<>();
    private HashMap<Long, Integer> menuActivityTimer = new HashMap<>();
    private HashMap<Long, Long> menuPlayerTrack = new HashMap<>();
    private HashMap<Long, Long> guildTrack = new HashMap<>();
    private HashMap<Long, Long> channelTrack = new HashMap<>();
    private String SERVER_LIST_TITLE = "EssentialBot Recently Bumped Servers - Page {PAGE}";
    private String SERVER_LIST_FOOTER = "© 2020 Badger, All Rights Reserved";
    private int GUILDS_PER_PAGE = 10;
    private int delay_delete = 60; // Delete the menu after 60 seconds of inactivity
    public ServerListCommand() {
        // Start timer for activityTimer
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Iterator it = menuActivityTimer.entrySet().iterator();

                while (it.hasNext()) {
                    Map.Entry ele = (Map.Entry) it.next();
                    long msgID = (long) ele.getKey();
                    int activity = (int) ele.getValue();
                    int newActivity = activity + 1;
                    menuActivityTimer.put(msgID, newActivity);
                    if (newActivity >= delay_delete) {
                        // We delete it
                        long guildID = guildTrack.get(msgID);
                        long chanID = channelTrack.get(msgID);
                        try {
                            Message msg = Main.get().getJDA().getGuildById(guildID).getTextChannelById(chanID).getMessageById(msgID).submit().get();
                            msg.delete().submit();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        pageTracker.remove(msgID);
                        menuActivityTimer.remove(msgID);
                        menuPlayerTrack.remove(msgID);
                        guildTrack.remove(msgID);
                        channelTrack.remove(msgID);
                    }
                }
            }
        }, 0L, 1000L); // Every second
    }
    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent evt) {
        Member mem = evt.getMember();
        TextChannel chan = evt.getChannel();
        long chanID = evt.getChannel().getIdLong();
        long guildID = evt.getGuild().getIdLong();
        String command = evt.getMessage().getContentRaw().split(" ")[0];
        String[] args = evt.getMessage().getContentRaw().split(" ");
        if (command.equalsIgnoreCase("=list")) {
            evt.getMessage().delete().submit();
            if (args.length > 1) {
                // They want to turn it on or off
                if (API.get().isAdmin(mem)) {
                    // They are allowed to turn it on and off
                    if (args[1].equalsIgnoreCase("off")) {
                        // Turn off the ServerList
                        API.get().toggleServerList(guildID, false);
                        evt.getChannel().sendMessage(API.get().getSuccessEmbed(
                                "The Server Listing for this server has been `DISABLED`", mem).build()).submit();
                    } else if (args[1].equalsIgnoreCase("on")) {
                        // Turn on the ServerList
                        API.get().toggleServerList(guildID, true);
                        evt.getChannel().sendMessage(API.get().getSuccessEmbed(
                                "The Server Listing for this server has been `ENABLED`", mem).build()).submit();
                    }
                }
                return;
            }
            if (API.get().isServerListOn(guildID)) {
                pages = new ArrayList<>();
                if (mem != null && mem.getUser() != null && !mem.getUser().isFake() && !mem.getUser().isBot()) {
                    evt.getMessage().delete().submit();
                    Message loading = null;
                    try {
                        loading = evt.getChannel().sendMessage(API.get().getCustomEmbed(
                                "**Fetching Data...**", "Please wait whilst we fetch invites from the most recently bumped servers...",
                                Color.BLUE, mem).build()).submit().get();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    int curCount = 0;
                    Guild[] guildArr = new Guild[GUILDS_PER_PAGE];
                    for (long gid : API.get().getServerListingGuilds()) {
                        // Looping through their guilds
                        Guild guild = evt.getJDA().getGuildById(gid);
                        if (curCount < (GUILDS_PER_PAGE - 1)) {
                            guildArr[curCount] = guild;
                        } else {
                            guildArr[curCount] = guild;
                            curCount = -1;
                            String addStr = "";
                            for (Guild gadd : guildArr) {
                                if (gadd != null) {
                                    String inv = API.get().getServerInvite(gadd.getIdLong());
                                    if (inv != null) {
                                        addStr += "**" + gadd.getName() + "** - JOIN: `discord.gg/" + inv + "`" + "\n\n";
                                    }
                                }
                            }
                            guildArr = new Guild[GUILDS_PER_PAGE];
                            pages.add(addStr);
                        }
                        curCount++;
                    }
                    if (curCount != 0) {
                        String addStr = "";
                        for (Guild gadd : guildArr) {
                            if (gadd != null) {
                                long gid = gadd.getIdLong();
                                String inv = API.get().getServerInvite(gid);
                                if (inv != null) {
                                    addStr += "**" + gadd.getName() + "** - JOIN: `discord.gg/" + inv + "`" + "\n\n";
                                }
                            }
                        }
                        pages.add(addStr);
                    }
                    try {
                        Message msg = chan.sendMessage(API.get().getEssentialEmbed(SERVER_LIST_TITLE.replace("{PAGE}", "1"), pages.get(0),
                                null, SERVER_LIST_FOOTER, Main.get().getJDA().getSelfUser().getAvatarUrl()).build()).submit().get();
                        // "⏪"
                        // "◀️"
                        // "▶️"
                        // "⏩"
                        msg.addReaction("⏪").submit();
                        msg.addReaction("◀️").submit();
                        msg.addReaction("▶️").submit();
                        msg.addReaction("⏩").submit();
                        pageTracker.put(msg.getIdLong(), 1);
                        menuActivityTimer.put(msg.getIdLong(), 0);
                        guildTrack.put(msg.getIdLong(), guildID);
                        channelTrack.put(msg.getIdLong(), chanID);
                        menuPlayerTrack.put(msg.getIdLong(), mem.getUser().getIdLong());
                        if (loading != null) {
                            loading.delete().submit();
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            } else {
                // The ServerList is not enabled on this server...
                evt.getChannel().sendMessage(API.get().getCustomEmbed(
                        "**Server Listing disabled...**", "The Server Listing feature has been disabled. Using `=bump` will have no use either... An " +
                                "administrator can type `=list on` to turn the feature back on though!",
                        Color.RED, mem).build()).submit();
            }
        }
    }

    @Override
    public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent evt) {
        Member mem = evt.getMember();
        TextChannel chan = evt.getChannel();
        Guild g = evt.getGuild();
        String reaction = evt.getReaction().getReactionEmote().getName();
        long msgID = evt.getMessageIdLong();
        if (mem != null && mem.getUser() != null && !mem.getUser().isFake() && !mem.getUser().isBot()) {
            for (Long mid : pageTracker.keySet()) {
                if (mid == msgID) {
                    long userID = menuPlayerTrack.get(mid);
                    if (userID == mem.getUser().getIdLong()) {
                        // It's their menu, do stuff
                        int currentPage = pageTracker.get(mid);
                        switch (reaction) {
                            case "⏪":
                                pageTracker.put(mid, 1);
                                try {
                                    Message msg = Main.get().getJDA().getGuildById(g.getId()).getTextChannelById(chan.getId()).getMessageById(msgID).submit().get();
                                    msg.editMessage(API.get().getEssentialEmbed(SERVER_LIST_TITLE.replace("{PAGE}", "1"), pages.get(0),
                                            null, SERVER_LIST_FOOTER, Main.get().getJDA().getSelfUser().getAvatarUrl()).build()).submit();
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                                menuActivityTimer.put(mid, 0);
                                break;
                            case "◀️":
                                int backPage = currentPage - 1;
                                if (backPage >= 1) {
                                    int sendpage = backPage - 1;
                                    pageTracker.put(mid, backPage);
                                    try {
                                        Message msg = Main.get().getJDA().getGuildById(g.getId()).getTextChannelById(chan.getId()).getMessageById(msgID).submit().get();
                                        msg.editMessage(API.get().getEssentialEmbed(SERVER_LIST_TITLE.replace("{PAGE}", String.valueOf(backPage)), pages.get(sendpage),
                                                null, SERVER_LIST_FOOTER, Main.get().getJDA().getSelfUser().getAvatarUrl()).build()).submit();
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }
                                }
                                menuActivityTimer.put(mid, 0);
                                break;
                            case "▶️":
                                int nextPage = currentPage + 1;
                                if (nextPage <= pages.size()) {
                                    int sendpage = nextPage - 1;
                                    pageTracker.put(mid, nextPage);
                                    try {
                                        Message msg = Main.get().getJDA().getGuildById(g.getId()).getTextChannelById(chan.getId()).getMessageById(msgID).submit().get();
                                        msg.editMessage(API.get().getEssentialEmbed(SERVER_LIST_TITLE.replace("{PAGE}", String.valueOf(nextPage)), pages.get(sendpage),
                                                null, SERVER_LIST_FOOTER, Main.get().getJDA().getSelfUser().getAvatarUrl()).build()).submit();
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }
                                }
                                menuActivityTimer.put(mid, 0);
                                break;
                            case "⏩":
                                pageTracker.put(mid, pages.size());
                                try {
                                    Message msg = Main.get().getJDA().getGuildById(g.getId()).getTextChannelById(chan.getId()).getMessageById(msgID).submit().get();
                                    msg.editMessage(API.get().getEssentialEmbed(SERVER_LIST_TITLE.replace("{PAGE}", String.valueOf((pages.size()))), pages.get((pages.size() - 1)),
                                            null, SERVER_LIST_FOOTER, Main.get().getJDA().getSelfUser().getAvatarUrl()).build()).submit();
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                                menuActivityTimer.put(mid, 0);
                                break;
                        }
                        evt.getReaction().removeReaction(mem.getUser()).submit();
                    } else {
                        // Remove it, it's not valid
                        evt.getReaction().removeReaction(mem.getUser()).submit();
                    }
                }
            }
        }
    }
}
