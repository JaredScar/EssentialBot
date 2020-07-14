package store.badger.essentialbot.commands;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import store.badger.essentialbot.api.API;

public class UnbanCommand extends ListenerAdapter {
    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent evt) {
        Member mem = evt.getMember();
        long guildID = evt.getGuild().getIdLong();
        String command = evt.getMessage().getContentRaw().split(" ")[0];
        String[] args = evt.getMessage().getContentRaw().replace(command + " ", "").split(" ");
        if (command.equalsIgnoreCase("=unban")) {
            evt.getMessage().delete().submit();
            if (mem.isOwner() || API.get().hasPermission(guildID, mem, "permissionBan")) {
                if (args.length > 0) {
                    String user = args[0].replace("<", "").replace("!", "")
                            .replace("@", "").replace(">", "");
                    if (isLong(user)) {
                        // It's a valid user
                        long userID = Long.parseLong(user);
                        if (userID != evt.getJDA().getSelfUser().getIdLong()) {
                            // Is a valid user to ban
                            User unbanUser = evt.getJDA().getUserById(userID);
                            if (unbanUser != null) {
                                evt.getChannel().sendMessage(API.get().getSuccessEmbed(
                                        "You have unbanned `" + unbanUser.getAsTag() +
                                                "`", mem).build()).submit();
                                evt.getGuild().getController().unban(unbanUser).submit();
                            } else {
                                evt.getChannel().sendMessage(API.get().getSuccessEmbed(
                                        "You have unbanned `" + userID +
                                                "`", mem).build()).submit();
                                evt.getGuild().getController().unban(user).submit();
                            }
                        } else {
                            // Not a valid user
                            evt.getChannel().sendMessage(API.get().getErrorEmbed("That is not a valid user to be unbanned...", mem).build()).submit();
                        }
                    } else {
                        // Not a valid user
                        evt.getChannel().sendMessage(API.get().getErrorEmbed("That is not a valid user to be unbanned...", mem).build()).submit();
                    }
                } else {
                    // Not enough args
                    evt.getChannel().sendMessage(API.get().getErrorEmbed("You must supply enough arguments. Usage: `=unban @User`", mem).build()).submit();
                }
            } else {
                // Not proper permissions
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