package store.badger.essentialbot.commands;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import store.badger.essentialbot.api.API;

import java.awt.*;
import java.util.*;

public class ServerBumpCommand extends ListenerAdapter {
    private HashMap<Long, Integer> alreadyBumpedToday = new HashMap<>();
    private int BUMP_DELAY = 24; // In hours
    public ServerBumpCommand() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Iterator it = alreadyBumpedToday.entrySet().iterator();

                while (it.hasNext()) {
                    Map.Entry ele = (Map.Entry) it.next();
                    int time = (int) ele.getValue() - 1;
                    alreadyBumpedToday.put((long) ele.getKey(), time);
                    if (time <= 0) {
                        // Remove them
                        alreadyBumpedToday.put((long) ele.getKey(), null);
                    }
                }
            }
        }, 0L, (1000 * 60 * 60)); // Every hour
    }
    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent evt) {
        Member mem = evt.getMember();
        long guildID = evt.getGuild().getIdLong();
        String command = evt.getMessage().getContentRaw().split(" ")[0];
        String[] args = evt.getMessage().getContentRaw().replace(command + " ", "").split(" ");
        String argsString = evt.getMessage().getContentRaw().replace(command + " ", "");
        if (command.equalsIgnoreCase("=bump")) {
            evt.getMessage().delete().submit();
            if (API.get().isServerListOn(guildID)) {
                if (!alreadyBumpedToday.containsKey(mem.getUser().getIdLong())) {
                    if (API.get().bumpServer(guildID)) {
                        // Successfully bumped the server
                        evt.getChannel().sendMessage(API.get().getCustomEmbed(
                                "**Server Bump**", "This server has been bumped to the top of the Server Listing (`=list`)",
                                Color.BLUE, mem).build()).submit();
                        alreadyBumpedToday.put(mem.getUser().getIdLong(), BUMP_DELAY);
                    } else {
                        // Encountered a problem
                        evt.getChannel().sendMessage(API.get().getCustomEmbed(
                                "**ERROR**", "Encountered a problem when trying to bump... Please try again later.",
                                Color.RED, mem).build()).submit();
                    }
                } else {
                    // They already bumped a server within 24 hours!
                    evt.getChannel().sendMessage(API.get().getErrorEmbed("You already have bumped a server today...", mem).build()).submit();
                }
            } else {
                // Server Listing feature is disabled
                // The ServerList is not enabled on this server...
                evt.getChannel().sendMessage(API.get().getCustomEmbed(
                        "**Server Listing disabled...**", "The Server Listing feature has been disabled. Using `=bump` will have no use... An " +
                                "administrator can type `=list on` to turn the feature back on though!",
                        Color.RED, mem).build()).submit();
            }
        }
    }
}
