package store.badger.essentialbot.commands;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import store.badger.essentialbot.api.API;

public class PermCommand extends ListenerAdapter {
    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent evt) {
        Member mem = evt.getMember();
        long guildID = evt.getGuild().getIdLong();
        String command = evt.getMessage().getContentRaw().split(" ")[0];
        String[] args = evt.getMessage().getContentRaw().replace(command + " ", "").split(" ");
        // `=perm toggle <permission> <roleID>`
        if (command.equalsIgnoreCase("=perm")) {
            evt.getMessage().delete().submit();
            if (mem.isOwner() || API.get().isAdmin(mem)) {
                // They have permissions to run this command
                if (args.length > 2) {
                    // Proper args supplied
                    String arg0 = args[0];
                    String permission = args[1];
                    String roleID = args[2].replace("<", "").replace("@", "")
                            .replace("&", "").replace(">", "");
                    switch (arg0.toLowerCase()) {
                        case "toggle":
                            if (isLong(roleID)) {
                                // Valid roleID
                                long roleLong = Long.parseLong(roleID);
                                boolean guildContainsRole = false;
                                String roleName = null;
                                for (Role role : evt.getGuild().getRoles()) {
                                    if (role.getIdLong() == roleLong) {
                                        guildContainsRole = true;
                                        roleName = role.getName();
                                    }
                                }
                                if (guildContainsRole) {
                                    // It contains the role, it's valid to give permissions
                                    boolean[] toggledPerms = API.get().togglePermission(guildID, roleLong, permission);
                                    if (toggledPerms[0]) {
                                        // Toggled the permission successfully
                                        boolean permToggleVal = toggledPerms[1];
                                        if (permToggleVal) {
                                            evt.getChannel().sendMessage(API.get().getSuccessEmbed(
                                                    "You have set permission `" + permission + "` to " +
                                                            "`ALLOWED` for role group `" + roleName + "`", mem).build()).submit();
                                        } else {
                                            evt.getChannel().sendMessage(API.get().getSuccessEmbed(
                                                    "You have set permission `" + permission + "` to " +
                                                            "`DENIED` for role group `" + roleName + "`", mem).build()).submit();
                                        }
                                    } else {
                                        // Did not toggle the permission, wrong permission supplied
                                        evt.getChannel().sendMessage(API.get().getErrorEmbed(
                                                "You did not supply a valid permission node...", mem).build()).submit();
                                    }
                                } else {
                                    // Not a valid role ID supplied
                                    evt.getChannel().sendMessage(API.get().getErrorEmbed(
                                            "That is not a valid role supplied.", mem).build()).submit();
                                }
                            } else {
                                // Not a valid roleID supplied
                                evt.getChannel().sendMessage(API.get().getErrorEmbed(
                                        "That is not a valid role supplied.", mem).build()).submit();
                            }
                            break;
                        default:
                            // Invalid supplied
                            evt.getChannel().sendMessage(API.get().getErrorEmbed(
                                    "Improper usage. Try `=perm toggle <permission> @Role`", mem).build()).submit();
                    }
                } else {
                    // Not enough arguments supplied
                    evt.getChannel().sendMessage(API.get().getErrorEmbed(
                            "Improper usage. Try `=perm toggle <permission> @Role`", mem).build()).submit();
                }
            } else {
                // Don't have permission
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
