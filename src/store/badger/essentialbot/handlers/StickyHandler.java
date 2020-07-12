package store.badger.essentialbot.handlers;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import store.badger.essentialbot.main.Main;
import store.badger.essentialbot.objects.StickyMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

public class StickyHandler extends ListenerAdapter {
    private ArrayList<StickyMessage> stickies = new ArrayList<>();
    private static StickyHandler handler = new StickyHandler();
    public static StickyHandler get() {
        return handler;
    }
    public void addSticky(StickyMessage sticky) {
        if (hasSticky(sticky.getGuildID(), sticky.getChanID())) {
            // Has it, we update it
            StickyMessage toEdit = null;
            for (StickyMessage stickyy : stickies) {
                if (stickyy.getChanID() == sticky.getChanID()) {
                    if (stickyy.getGuildID() == sticky.getGuildID()) {
                        toEdit = sticky;
                        break;
                    }
                }
            }
            if (toEdit != null) {
                toEdit.setContent(sticky.getContent());
                toEdit.save();
            }
        } else {
            // Doesn't have it, add it
            stickies.add(sticky);
            sticky.save();
        }
    }
    public void removeSticky(long guildID, long chanID) {
        StickyMessage toRemove = null;
        for (StickyMessage sticky : stickies) {
            if (sticky.getChanID() == chanID) {
                if (sticky.getGuildID() == guildID) {
                    toRemove = sticky;
                    break;
                }
            }
        }
        if (toRemove != null) {
            if (lastMessage.containsKey(toRemove)) {
                Main.get().getJDA().getGuildById(guildID).getTextChannelById(chanID)
                        .deleteMessageById(
                        lastMessage.get(toRemove)
                ).submit();
                lastMessage.remove(toRemove);
            }
            stickies.remove(toRemove);
            toRemove.delete();
        }
    }
    public boolean hasSticky(long guildID, long chanID) {
        for (StickyMessage sticky : stickies) {
            if (sticky.getChanID() == chanID) {
                if (sticky.getGuildID() == guildID) {
                    return true;
                }
            }
        }
        return false;
    }
    private HashMap<StickyMessage, Long> lastMessage = new HashMap<>();
    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent evt) {
        Member mem = evt.getMember();
        TextChannel chan = evt.getChannel();
        long chanID = evt.getChannel().getIdLong();
        long guildID = evt.getGuild().getIdLong();
        for (StickyMessage sticky : stickies) {
            if (sticky.getChanID() == chanID) {
                if (sticky.getGuildID() == guildID) {
                    if (mem != null && mem.getUser() != null) {
                        if (mem.getUser().getIdLong() != Main.get().getJDA().getSelfUser().getIdLong()) {
                            try {
                                if (lastMessage.containsKey(sticky)) {
                                    Main.get().getJDA().getGuildById(guildID).getTextChannelById(chanID)
                                            .deleteMessageById(
                                                    lastMessage.get(sticky)
                                            ).submit();
                                }
                                Message msg = chan.sendMessage("**__Stickied Message:__**\n" + sticky.getContent()).submit().get();
                                lastMessage.put(sticky, msg.getIdLong());
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
    }
}
