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

public class HelpCommand extends ListenerAdapter {
    ArrayList<String> pages = new ArrayList<>();
    HashMap<Long, Integer> pageTracker = new HashMap<>();
    HashMap<Long, Integer> menuActivityTimer = new HashMap<>();
    HashMap<Long, Long> menuPlayerTrack = new HashMap<>();
    HashMap<Long, Long> guildTrack = new HashMap<>();
    HashMap<Long, Long> channelTrack = new HashMap<>();
    // TODO Eventually just make this into a sub-class called "Help-Menu" that tracks it all ^^^
    String HELP_TITLE = "EssentialBot Help - Page {PAGE}";
    String HELP_FOOTER = "© 2020 Badger, All Rights Reserved";
    int delay_delete = 60; // Delete the menu after 60 seconds of inactivity
    public HelpCommand() {
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
        if (command.equalsIgnoreCase("=help")) {
            if (mem != null && mem.getUser() != null && !mem.getUser().isFake() && !mem.getUser().isBot()) {
                evt.getMessage().delete().submit();
                if (pages.size() == 0) {
                    // It needs the pages set up
                    pages.add("EssentialBot is well, an essential bot for your discord server. All of " +
                            "EssentialBot's commands can be configured to be used by discord roles on your discord " +
                            "server. This is a much more advanced permission system than discord's system " +
                            "considering you can set up permissions for each command and/or action the bot can " +
                            "perform." +
                            "\n\n" +
                            "**Name:** " + Main.get().getJDA().getSelfUser().getName() + "\n" +
                            "**Creation Date:** " + "July 12th, 2020" +
                            "\n\n" +
                            "React below to change the page."
                    );
                    pages.add(
                            "`=perm toggle <permission> <roleID>`: Toggle permissions for the " +
                                    "role specified. A role tag or role ID can go in the `<roleID>` parameter. \n[Requires `ADMINISTRATOR`]" +
                                    "\n\n\n" +
                                    "`=help`: Shows the help menu for EssentialBot\n" +
                                    "\n\n" +
                                    "`=sticky <msg>`: Sticky a message to the bottom of the screen, this will also turn the " +
                                    "channel into a 15 second slowmode whilst a sticky message is active (by default). \n[Requires `permissionSticky`] \n" +
                                    "\n\n" +
                                    "`=unsticky`: This will unsticky a message from a channel (if there is an sticky " +
                                    "message active within the channel it is ran in). \n[Requires `permissionSticky`] \n" +
                                    "\n\n" +
                                    "`=kick @User <reason>`: This will kick a user from the discord. \n[Requires `permissionKick`]"
                    );
                    pages.add(
                            "`=shadowb @User <reason>`: This will shadow ban a user from the discord. Basically they will be " +
                                    "denied access to every discord channel within the discord. (Even if they join back!) \n[Requires `permissionShadowBan`] \n" +
                                    "\n\n" +
                                    "`=unshadowb @User`: This will unshadow ban a user from the discord. \n[Requires `permissionShadowBan`]\n" +
                                    "\n\n" +
                                    "`=ban @User <reason>`: This will ban the user from the discord permanently. \n[Requires `permissionBan`]\n" +
                                    "\n\n" +
                                    "`=unban @User`: This will unban the user from the discord. \n[Requires `permissionBan`]"
                    );
                }
                try {
                    Message msg = chan.sendMessage(API.get().getEssentialEmbed(HELP_TITLE.replace("{PAGE}", "1"), pages.get(0),
                            null, HELP_FOOTER, Main.get().getJDA().getSelfUser().getAvatarUrl()).build()).submit().get();
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
                                    msg.editMessage(API.get().getEssentialEmbed(HELP_TITLE.replace("{PAGE}", "1"), pages.get(0),
                                            null, HELP_FOOTER, Main.get().getJDA().getSelfUser().getAvatarUrl()).build()).submit();
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
                                        msg.editMessage(API.get().getEssentialEmbed(HELP_TITLE.replace("{PAGE}", String.valueOf(backPage)), pages.get(sendpage),
                                                null, HELP_FOOTER, Main.get().getJDA().getSelfUser().getAvatarUrl()).build()).submit();
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
                                        msg.editMessage(API.get().getEssentialEmbed(HELP_TITLE.replace("{PAGE}", String.valueOf(nextPage)), pages.get(sendpage),
                                                null, HELP_FOOTER, Main.get().getJDA().getSelfUser().getAvatarUrl()).build()).submit();
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
                                    msg.editMessage(API.get().getEssentialEmbed(HELP_TITLE.replace("{PAGE}", String.valueOf((pages.size()))), pages.get((pages.size() - 1)),
                                            null, HELP_FOOTER, Main.get().getJDA().getSelfUser().getAvatarUrl()).build()).submit();
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
