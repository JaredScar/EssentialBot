package store.badger.essentialbot.commands;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import store.badger.essentialbot.api.API;

public class UnshadowBanCommand extends ListenerAdapter {
    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent evt) {
        Member mem = evt.getMember();
        long guildID = evt.getGuild().getIdLong();
        String command = evt.getMessage().getContentRaw().split(" ")[0];
        String[] args = evt.getMessage().getContentRaw().replace(command + " ", "").split(" ");
        if (command.equalsIgnoreCase("=unshadowb")) {
            evt.getMessage().delete().submit();
            if (mem.isOwner() || API.get().hasPermission(guildID, mem, "permissionShadowBan")) {
                if (args.length > 0) {
                    String user = args[0].replace("<", "").replace("!", "")
                            .replace("@", "").replace(">", "");
                    if (isLong(user)) {
                        // It's a valid user
                        long userID = Long.parseLong(user);
                        if (evt.getGuild().getMemberById(userID) !=null && evt.getGuild().getMemberById(userID).getUser()
                                .getIdLong() != evt.getJDA().getSelfUser().getIdLong() && !evt.getGuild().getMemberById(userID).isOwner()) {
                            // Is a valid user to ShadowBan
                            Member toShadowBan = evt.getGuild().getMemberById(userID);
                            if (API.get().isAlreadyShadowBanned(toShadowBan)) {
                                if (API.get().unshadowBan(toShadowBan)) {
                                    evt.getChannel().sendMessage(API.get().getSuccessEmbed(
                                            "You have Unshadow-Banned `" + toShadowBan.getUser().getAsTag() +
                                                    "`", mem).build()).submit();
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
                                                                // Shouldn't need this
                                                            } else {
                                                                PermissionOverride po = chann.getPermissionOverride(toShadowBan);
                                                                po.delete().queue();
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
                                                            // Shouldn't need this
                                                        } else {
                                                            PermissionOverride po = chann.getPermissionOverride(toShadowBan);
                                                            po.delete().queue();
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    // The shadowBan went wrong
                                    evt.getChannel().sendMessage(API.get().getErrorEmbed("Something went wrong on our end :(", mem).build()).submit();
                                }
                            } else {
                                // They are not Shadow-Banned
                                evt.getChannel().sendMessage(API.get().getErrorEmbed("They are not Shadow-Banned!", mem).build()).submit();
                            }
                        } else {
                            // Not a valid user to Shadow-Ban
                            evt.getChannel().sendMessage(API.get().getErrorEmbed("That is not a valid member to be Unshadow-Banned...", mem).build()).submit();
                        }
                    } else {
                        // Not a valid user supplied
                        evt.getChannel().sendMessage(API.get().getErrorEmbed("That is not a valid member to be Unshadow-Banned...", mem).build()).submit();
                    }
                } else {
                    // Not enough arguments
                    evt.getChannel().sendMessage(API.get().getErrorEmbed("You must supply enough arguments. Usage: `=unshadowb @User`", mem).build()).submit();
                }
            } else {
                // Does not have permission
                evt.getChannel().sendMessage(API.get().getErrorEmbed("You do not have proper permissions to utilize this.", mem).build()).submit();
            }
        }
    }
    private boolean isLong(String l) {
        try {
            Long.parseLong(l);
        } catch (NumberFormatException ex) {
            return false;
        }
        return true;
    }
}
