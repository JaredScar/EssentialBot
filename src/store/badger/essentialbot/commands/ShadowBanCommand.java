package store.badger.essentialbot.commands;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class ShadowBanCommand extends ListenerAdapter {
    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent evt) {
        Member mem = evt.getMember();
        TextChannel chan = evt.getChannel();
        long chanID = evt.getChannel().getIdLong();
        long guildID = evt.getGuild().getIdLong();
        String command = evt.getMessage().getContentRaw().split(" ")[0];
        String[] args = evt.getMessage().getContentRaw().replace(command + " ", "").split(" ");
        String argsString = evt.getMessage().getContentRaw().replace(command + " ", "");
        if (command.equalsIgnoreCase("=shadowb")) {
            //
        }
    }
}
