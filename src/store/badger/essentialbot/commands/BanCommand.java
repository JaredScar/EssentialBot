package store.badger.essentialbot.commands;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import store.badger.essentialbot.api.API;

public class BanCommand extends ListenerAdapter {
    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent evt) {
        Member mem = evt.getMember();
        long guildID = evt.getGuild().getIdLong();
        String command = evt.getMessage().getContentRaw().split(" ")[0];
        String[] args = evt.getMessage().getContentRaw().replace(command + " ", "").split(" ");
        String argsString = evt.getMessage().getContentRaw().replace(command + " ", "");
        if (command.equalsIgnoreCase("=ban")) {
            evt.getMessage().delete().submit();
            if (mem.isOwner() || API.get().hasPermission(guildID, mem, "permissionBan")) {
                if (args.length > 1) {
                    String user = args[0].replace("<", "").replace("!", "")
                            .replace("@", "").replace(">", "");
                    if (isLong(user)) {
                        // It's a valid user
                        long userID = Long.parseLong(user);
                        if (userID != evt.getJDA().getSelfUser().getIdLong()) {
                            // Is a valid user to ban
                            User toBan = evt.getJDA().getUserById(userID);
                            String reason = argsString.replace(args[0] + " ", "");
                            if (toBan !=null) {
                                evt.getChannel().sendMessage(API.get().getSuccessEmbed(
                                        "You have banned `" + toBan.getAsTag() +
                                                "` for reason: `" + reason + "`", mem).build()).submit();
                                evt.getGuild().getController().ban(toBan, 0, reason).submit();
                            } else {
                                evt.getChannel().sendMessage(API.get().getSuccessEmbed(
                                        "You have banned `" + userID +
                                                "` for reason: `" + reason + "`", mem).build()).submit();
                                evt.getGuild().getController().ban(user, 0, reason).submit();
                            }
                        } else {
                            // Not a valid user
                            evt.getChannel().sendMessage(API.get().getErrorEmbed("That is not a valid user to be banned...", mem).build()).submit();
                        }
                    } else {
                        // Not a valid user
                        evt.getChannel().sendMessage(API.get().getErrorEmbed("That is not a valid user to be banned...", mem).build()).submit();
                    }
                } else {
                    // Not enough args
                    evt.getChannel().sendMessage(API.get().getErrorEmbed("You must supply enough arguments. Usage: `=ban @User <reason>`", mem).build()).submit();
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
