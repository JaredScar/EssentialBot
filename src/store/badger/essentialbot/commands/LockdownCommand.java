package store.badger.essentialbot.commands;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import store.badger.essentialbot.api.API;

import java.awt.*;

public class LockdownCommand extends ListenerAdapter {
    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent evt) {
        Member mem = evt.getMember();
        long guildID = evt.getGuild().getIdLong();
        String command = evt.getMessage().getContentRaw().split(" ")[0];
        String[] args = evt.getMessage().getContentRaw().split(" ");
        String argsString = evt.getMessage().getContentRaw().replace(command + " ", "");
        if (command.equalsIgnoreCase("=lockdown")) {
            evt.getMessage().delete().submit();
            if (mem.isOwner() || API.get().hasPermission(guildID, mem, "permissionLockdown")) {
                if (args.length > 1) {
                    // =lockdown <reason>
                    if (API.get().isLockedDown(guildID)) {
                        // It was locked down, unlock
                        evt.getChannel().sendMessage(API.get().getCustomEmbed(
                                "**SERVER UNLOCKED**", "The server has been unlocked for reason: `" + argsString + "`",
                                Color.BLUE, mem).build()).submit();
                    } else {
                        // Lock down the server
                        evt.getChannel().sendMessage(API.get().getCustomEmbed(
                                "**SERVER LOCKED**", "The server has been locked for reason: `" + argsString + "`",
                                Color.RED, mem).build()).submit();
                    }
                    API.get().toggleLockdown(guildID, argsString);
                } else {
                    evt.getChannel().sendMessage(API.get().getErrorEmbed("You must supply enough arguments. Usage: `=lockdown <reason>`", mem).build()).submit();
                }
            } else {
                // Does not have proper permissions
                evt.getChannel().sendMessage(API.get().getErrorEmbed("You do not have proper permissions to utilize this.", mem).build()).submit();
            }
        } else
            if (command.equalsIgnoreCase("=lockstatus")) {
                String reason = API.get().getLockdownReason(guildID);
                if (API.get().isLockedDown(guildID)) {
                    // It was locked down
                    evt.getChannel().sendMessage(API.get().getSuccessEmbed(
                            "The server is currently locked for reason: `" + reason + "`", mem).build()).submit();
                } else {
                    // It's not locked
                    evt.getChannel().sendMessage(API.get().getSuccessEmbed(
                            "The server is currently unlocked! Everyone should be able to join...", mem).build()).submit();
                }
            }
    }
}
