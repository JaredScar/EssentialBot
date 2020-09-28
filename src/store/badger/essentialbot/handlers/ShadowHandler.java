package store.badger.essentialbot.handlers;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import store.badger.essentialbot.api.API;

public class ShadowHandler extends ListenerAdapter {
    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent evt) {
        Member mem = evt.getMember();
        if (mem != null && mem.getUser() != null && API.get().isAlreadyShadowBanned(mem)) {
            // Needs to be shadow banned again
            // Is a valid user to ShadowBan
            Member toShadowBan = mem;
            boolean channelAlreadyDone = false;
            for (TextChannel chann : evt.getJDA().getTextChannels()) {
                channelAlreadyDone = false;
                for (Role role : toShadowBan.getRoles()) {
                    if (chann.getPermissionOverride(role) != null) {
                        if (chann.getPermissionOverride(role).getAllowed().contains(Permission.VIEW_CHANNEL)) {
                            if (chann.getPermissionOverride(role).getAllowed().contains(Permission.MESSAGE_READ)) {
                                if (!channelAlreadyDone) {
                                    channelAlreadyDone = true;
                                    if (chann.getPermissionOverride(toShadowBan) == null) {
                                        chann.createPermissionOverride(toShadowBan).setDeny(Permission.VIEW_CHANNEL).submit().thenRun(
                                                () -> {
                                                    PermissionOverride po = chann.getPermissionOverride(toShadowBan);
                                                    po.getManager().deny(Permission.MESSAGE_READ)
                                                            .deny(Permission.MESSAGE_WRITE).queue();
                                                }
                                        );
                                    } else {
                                        PermissionOverride po = chann.getPermissionOverride(toShadowBan);
                                        po.getManager().deny(Permission.VIEW_CHANNEL)
                                                .deny(Permission.MESSAGE_READ)
                                                .deny(Permission.MESSAGE_WRITE).queue();
                                    }
                                }
                            }
                        }
                    }
                }
            }
            for (VoiceChannel chann : evt.getJDA().getVoiceChannels()) {
                channelAlreadyDone = false;
                for (Role role : toShadowBan.getRoles()) {
                    if (chann.getPermissionOverride(role) != null) {
                        if (chann.getPermissionOverride(role).getAllowed().contains(Permission.VIEW_CHANNEL)) {
                            if (!channelAlreadyDone) {
                                channelAlreadyDone = true;
                                if (chann.getPermissionOverride(toShadowBan) == null) {
                                    chann.createPermissionOverride(toShadowBan).setDeny(Permission.VOICE_CONNECT)
                                            .queue();
                                } else {
                                    PermissionOverride po = chann.getPermissionOverride(toShadowBan);
                                    po.getManager().deny(Permission.VIEW_CHANNEL)
                                            .queue();
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
