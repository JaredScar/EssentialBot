package store.badger.essentialbot.commands;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import store.badger.essentialbot.api.API;
import store.badger.essentialbot.main.Main;

import java.util.*;

public class SimpleCommands extends ListenerAdapter {
    private ArrayList<String> pages = new ArrayList<>();
    private HashMap<Long, Integer> pageTracker = new HashMap<>();
    private HashMap<Long, Integer> menuActivityTimer = new HashMap<>();
    private HashMap<Long, Long> menuPlayerTrack = new HashMap<>();
    private HashMap<Long, Long> guildTrack = new HashMap<>();
    private HashMap<Long, Long> channelTrack = new HashMap<>();
    private String SERVER_TITLE = "EssentialBot Servers - Page {PAGE}";
    private String SERVER_FOOTER = "© 2020 Badger, All Rights Reserved";
    private int GUILDS_PER_PAGE = 6;
    private int delay_delete = 60; // Delete the menu after 60 seconds of inactivity
    public SimpleCommands() {
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
        String[] args = evt.getMessage().getContentRaw().replace(command, "").split(" ");
        String argsString = evt.getMessage().getContentRaw().replace(command, "");
        if (command.equalsIgnoreCase("=servercount")) {
            chan.sendMessage(API.get().getEssentialEmbed("EssentialBot's Server Count", "EssentialBot is on `"
                            + evt.getJDA().getGuilds().size() + "` servers in total...",
                    null, SERVER_FOOTER, Main.get().getJDA().getSelfUser().getAvatarUrl()).build()).submit();
        } else if (command.equalsIgnoreCase("=servers")) {
            if (mem != null && mem.getUser() != null && !mem.getUser().isFake() && !mem.getUser().isBot()) {
                evt.getMessage().delete().submit();
                int curCount = 0;
                Guild[] guildArr = new Guild[GUILDS_PER_PAGE];
                for (Guild guild : evt.getJDA().getGuilds()) {
                    // Looping through their guilds
                    if (curCount < (GUILDS_PER_PAGE - 1)) {
                        guildArr[curCount] = guild;
                    } else {
                        guildArr[curCount] = guild;
                        curCount = -1;
                        String addStr = "";
                        for (Guild gadd : guildArr) {
                            if (gadd != null) {
                                addStr += "**" + gadd.getName() + "** - Owner: `" + gadd.getMemberById(gadd.getOwnerId()).getUser().getName()
                                        + "#" + gadd.getMemberById(gadd.getOwnerId()).getUser().getDiscriminator() + "`" + "\n\n";
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
                            addStr += "**" + gadd.getName() + "** - Owner: `" + gadd.getMemberById(gadd.getOwnerId()).getUser().getName()
                                    + "#" + gadd.getMemberById(gadd.getOwnerId()).getUser().getDiscriminator() + "`" + "\n\n";
                        }
                    }
                    pages.add(addStr);
                }
                try {
                    Message msg = chan.sendMessage(API.get().getEssentialEmbed(SERVER_TITLE.replace("{PAGE}", "1"), pages.get(0),
                            null, SERVER_FOOTER, Main.get().getJDA().getSelfUser().getAvatarUrl()).build()).submit().get();
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
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
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
                                    msg.editMessage(API.get().getEssentialEmbed(SERVER_TITLE.replace("{PAGE}", "1"), pages.get(0),
                                            null, SERVER_FOOTER, Main.get().getJDA().getSelfUser().getAvatarUrl()).build()).submit();
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
                                        msg.editMessage(API.get().getEssentialEmbed(SERVER_TITLE.replace("{PAGE}", String.valueOf(backPage)), pages.get(sendpage),
                                                null, SERVER_FOOTER, Main.get().getJDA().getSelfUser().getAvatarUrl()).build()).submit();
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
                                        msg.editMessage(API.get().getEssentialEmbed(SERVER_TITLE.replace("{PAGE}", String.valueOf(nextPage)), pages.get(sendpage),
                                                null, SERVER_FOOTER, Main.get().getJDA().getSelfUser().getAvatarUrl()).build()).submit();
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }
                                }
                                menuActivityTimer.put(mid, 0);
                                break;
                            case "⏩":
                                pageTracker.put(mid, pages.size() - 1);
                                try {
                                    Message msg = Main.get().getJDA().getGuildById(g.getId()).getTextChannelById(chan.getId()).getMessageById(msgID).submit().get();
                                    msg.editMessage(API.get().getEssentialEmbed(SERVER_TITLE.replace("{PAGE}", String.valueOf((pages.size()))), pages.get((pages.size() - 1)),
                                            null, SERVER_FOOTER, Main.get().getJDA().getSelfUser().getAvatarUrl()).build()).submit();
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
