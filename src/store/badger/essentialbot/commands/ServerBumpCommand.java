package store.badger.essentialbot.commands;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import store.badger.essentialbot.api.API;

import java.awt.*;

public class ServerBumpCommand extends ListenerAdapter {
    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent evt) {
        Member mem = evt.getMember();
        long guildID = evt.getGuild().getIdLong();
        String command = evt.getMessage().getContentRaw().split(" ")[0];
        String[] args = evt.getMessage().getContentRaw().replace(command + " ", "").split(" ");
        String argsString = evt.getMessage().getContentRaw().replace(command + " ", "");
        if (command.equalsIgnoreCase("=bump")) {
            evt.getMessage().delete().submit();
            evt.getChannel().sendMessage(API.get().getCustomEmbed(
                    "**Coming Soon**", "This feature is coming to EssentialBot very shortly :)",
                    Color.ORANGE, mem).build()).submit();
            // TODO Still have to do this
        }
    }
}
