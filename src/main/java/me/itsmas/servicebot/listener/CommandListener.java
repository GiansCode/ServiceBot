package me.itsmas.servicebot.listener;

import me.itsmas.servicebot.ServiceBot;
import me.itsmas.servicebot.util.Message;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.util.ArrayList;
import java.util.Arrays;

public class CommandListener extends ListenerAdapter
{
    private final ServiceBot bot;

    private final Guild guild;
    private final String prefix;

    public CommandListener(ServiceBot bot)
    {
        this.bot = bot;

        this.guild = bot.getGuild();
        this.prefix = bot.getConfig().getString("commandPrefix");
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event)
    {
        if (event.getGuild() != guild || event.getAuthor().isBot())
        {
            return;
        }


        Member member = event.getMember();
        TextChannel channel = event.getTextChannel();

        if (channel != bot.getChannelManager().getTextChannel(member))
        {
            return;
        }

        String msg = event.getMessage().getContentRaw();

        ArrayList<String> split = new ArrayList<>(Arrays.asList(msg.split(" ")));

        String label = split.remove(0).substring(prefix.length()).toLowerCase();
        String[] args = split.toArray(new String[0]);

        switch (label)
        {
            case "adduser":
                handleAddUser(member, channel, event.getMessage(), args);
                break;

            case "removeuser":
                handleRemoveUser(member, channel, event.getMessage(), args);
                break;

            default:
                break;
        }
    }

    private void handleAddUser(Member member, TextChannel channel, net.dv8tion.jda.core.entities.Message message, String[] args)
    {
        if (args.length != 1)
        {
            Message.ADD_USAGE.send(channel);
            return;
        }

        Member target = parseMember(args[0], message);

        if (nullCheck(target, channel) && selfCheck(channel, member, target,"add") && checkBypass(target, channel, "add"))
        {
            if (target.getUser().isBot())
            {
                Message.NO_BOTS.send(channel);
                return;
            }

            if (channel.getPermissionOverride(target) != null)
            {
                Message.ALREADY_IN.send(channel);
                return;
            }

            bot.getChannelManager().addOverride(channel, member);
            Message.ADDED_USER.send(channel, target.getAsMention());
        }
    }

    private void handleRemoveUser(Member member, TextChannel channel, net.dv8tion.jda.core.entities.Message message, String[] args)
    {
        if (args.length != 1)
        {
            Message.REMOVE_USAGE.send(channel);
            return;
        }

        Member target = parseMember(args[0], message);

        if (nullCheck(target, channel) && selfCheck(channel, member, target, "remove") && checkBypass(target, channel, "remove"))
        {
            if (channel.getPermissionOverride(target) == null)
            {
                Message.NOT_IN.send(channel);
                return;
            }

            bot.getChannelManager().removeOverride(channel, member);
            Message.REMOVED_USER.send(channel, target.getAsMention());
        }
    }

    private boolean nullCheck(Member member, TextChannel channel)
    {
        if (member == null)
        {
            Message.INVALID_USER.send(channel);
            return false;
        }

        return true;
    }

    private boolean checkBypass(Member target, TextChannel channel, String action)
    {
        if (bot.getChannelManager().isTeamMember(target))
        {
            Message.BYPASS_USER.send(channel, action);
            return false;
        }

        return true;
    }

    private boolean selfCheck(TextChannel channel, Member sender, Member target, String action)
    {
        if (sender == target)
        {
            Message.NO_SELF.send(channel, action);
            return false;
        }

        return true;
    }

    private Member parseMember(String input, net.dv8tion.jda.core.entities.Message message)
    {
        if (message.getMentionedUsers().size() == 1)
        {
            return guild.getMember(message.getMentionedUsers().get(0));
        }

        try
        {
            return guild.getMemberById(input);
        }
        catch (NumberFormatException ex)
        {
            try
            {
                return guild.getMembersByName(input, true).get(0);
            }
            catch (IndexOutOfBoundsException ex1)
            {
                return null;
            }
        }
    }
}
