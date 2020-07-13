package store.badger.essentialbot.commands;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import store.badger.essentialbot.api.API;
import store.badger.essentialbot.handlers.StickyHandler;
import store.badger.essentialbot.objects.StickyMessage;

public class StickyCommand extends ListenerAdapter {
    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent evt) {
        Member mem = evt.getMember();
        TextChannel chan = evt.getChannel();
        long chanID = evt.getChannel().getIdLong();
        long guildID = evt.getGuild().getIdLong();
        String command = evt.getMessage().getContentRaw().split(" ")[0];
        String[] args = evt.getMessage().getContentRaw().replace(command + " ", "").split(" ");
        String argsString = evt.getMessage().getContentRaw().replace(command + " ", "");
        if (command.equalsIgnoreCase("=sticky")) {
            evt.getMessage().delete().submit();
            // They want to sticky a message here
            if (mem != null && mem.getUser() != null && !mem.getUser().isFake() && !mem.getUser().isBot()) {
                if (mem.isOwner() || API.get().hasPermission(guildID, mem, "permissionSticky")) {
                    // They have permission to do it
                    if (argsString.length() > 0) {
                        // It's a valid message, we can use it
                        evt.getChannel().sendMessage(API.get().getSuccessEmbed("You have set the stickied message!", mem).build()).submit();
                        evt.getChannel().getManager().setSlowmode(15).submit();
                        chan.sendMessage("**__Stickied Message:__**\n" + argsString).submit();
                        StickyHandler.get().addSticky(new StickyMessage(guildID, chanID, argsString));
                    } else {
                        // No argument supplied, error
                        evt.getChannel().sendMessage(API.get().getErrorEmbed("You must supply a message with this command.", mem).build()).submit();
                    }
                } else {
                    // They don't have permission to do it
                    evt.getChannel().sendMessage(API.get().getErrorEmbed("You do not have proper permissions to utilize this.", mem).build()).submit();
                }
            }
        } else if (command.equalsIgnoreCase("=unsticky")) {
            evt.getMessage().delete().submit();
            // They want to unsticky the message here
            if (mem != null && mem.getUser() != null && !mem.getUser().isFake() && !mem.getUser().isBot()) {
                if (mem.isOwner() || API.get().hasPermission(guildID, mem, "permissionSticky")) {
                    // They have permission to do it
                    StickyHandler.get().removeSticky(guildID, chanID);
                    evt.getChannel().sendMessage(API.get().getSuccessEmbed("You have removed the stickied message!", mem).build()).submit();
                    evt.getChannel().getManager().setSlowmode(0).submit();
                } else {
                    // They don't have permission to do it
                    evt.getChannel().sendMessage(API.get().getErrorEmbed("You do not have proper permissions to utilize this.", mem).build()).submit();
                }
            }
        }
    }
}
