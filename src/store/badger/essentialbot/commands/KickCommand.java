package store.badger.essentialbot.commands;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import store.badger.essentialbot.api.API;

public class KickCommand extends ListenerAdapter {
    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent evt) {
        Member mem = evt.getMember();
        long guildID = evt.getGuild().getIdLong();
        String command = evt.getMessage().getContentRaw().split(" ")[0];
        String[] args = evt.getMessage().getContentRaw().replace(command + " ", "").split(" ");
        String argsString = evt.getMessage().getContentRaw().replace(command + " ", "");
        if (command.equalsIgnoreCase("=kick")) {
            evt.getMessage().delete().submit();
            if (mem.isOwner() || API.get().hasPermission(guildID, mem, "permissionKick")) {
                if (args.length > 1) {
                    String user = args[0].replace("<", "").replace("!", "")
                            .replace("@", "").replace(">", "");
                    if (isLong(user)) {
                        // It's a valid user
                        long userID = Long.parseLong(user);
                        if (evt.getGuild().getMemberById(userID) !=null && evt.getGuild().getMemberById(userID).getUser()
                                .getIdLong() != evt.getJDA().getSelfUser().getIdLong() && !evt.getGuild().getMemberById(userID).isOwner()) {
                            // Is a valid user to kick
                            Member toKick = evt.getGuild().getMemberById(userID);
                            String reason = argsString.replace(args[0] + " ", "");
                            evt.getChannel().sendMessage(API.get().getSuccessEmbed(
                                    "You have kicked `" + toKick.getUser().getAsTag() +
                                    "` for reason: `" + reason + "`", mem).build()).submit();
                            evt.getGuild().getController().kick(toKick).submit();
                        } else {
                            // Not a valid user to kick
                            evt.getChannel().sendMessage(API.get().getErrorEmbed("That is not a valid user to be kicked...", mem).build()).submit();
                        }
                    } else {
                        // Not a valid user supplied
                        evt.getChannel().sendMessage(API.get().getErrorEmbed("That is not a valid user to be kicked...", mem).build()).submit();
                    }
                } else {
                    // Not enough arguments
                    evt.getChannel().sendMessage(API.get().getErrorEmbed("You must supply enough arguments. Usage: `=kick @User <reason>`", mem).build()).submit();
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
