package store.badger.essentialbot.handlers;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import store.badger.essentialbot.api.API;

import java.awt.*;

public class LockdownHandler extends ListenerAdapter {
        @Override
        public void onGuildMemberJoin(GuildMemberJoinEvent evt) {
            Member mem = evt.getMember();
            if (mem != null && mem.getUser() != null && API.get().isLockedDown(evt.getGuild().getIdLong())) {
                // Mem is not null
                String reason = API.get().getLockdownReason(evt.getGuild().getIdLong());
                evt.getMember().getUser().openPrivateChannel().submit().join().sendMessage(API.get().getCustomEmbed(
                        "**THIS SERVER IS LOCKED DOWN**", "The server has been locked down for reason: `" + reason + "`",
                        Color.RED, mem).build()).submit();
                evt.getGuild().getController().kick(mem, reason).submit();
            }
        }
}
