package me.itsmas.servicebot.channel;

import me.itsmas.servicebot.ServiceBot;
import me.itsmas.servicebot.util.Scheduler;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class InactivityListener extends ListenerAdapter
{
    private final ServiceBot bot;

    private final int threshold;

    public InactivityListener(ServiceBot bot)
    {
        this.bot = bot;

        this.threshold = bot.getConfig().getInt("inactivityThreshold");

        Scheduler.runScheduled(this::checkInactive, 1000L, 10_000L);
        Scheduler.runScheduled(this::deleteTeamMemberChannels, 1000L * 60L * 60L, 10_000L);
    }

    private void checkInactive()
    {
        for (TextChannel channel : bot.getConfig().getChannelCategory().getTextChannels())
        {
            channel.getHistory().retrievePast(1).queue(messages -> handleLastMessage(channel, messages.get(0)));
        }
    }

    private void handleLastMessage(TextChannel channel, Message message)
    {
        if (overThreshold(message))
        {
            // Move to inactive and alert

            channel.getManager().setParent(bot.getConfig().getInactiveCategory()).queue();
            me.itsmas.servicebot.util.Message.CHANNEL_BECAME_INACTIVE.send(bot.getConfig().getAlertChannel(), channel.getAsMention());
        }
    }

    private boolean overThreshold(Message message)
    {
        return message.getCreationTime().toEpochSecond() < ((System.currentTimeMillis() - threshold) / 1000);
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event)
    {
        TextChannel channel = event.getTextChannel();

        if (channel.getParent() == bot.getConfig().getInactiveCategory())
        {
            channel.getManager().setParent(bot.getConfig().getChannelCategory()).queue();
            me.itsmas.servicebot.util.Message.CHANNEL_NO_LONGER_INACTIVE.send(bot.getConfig().getAlertChannel(), channel.getAsMention());
        }
    }

    private void deleteTeamMemberChannels()
    {
        for (Member member : bot.getGuild().getMembers())
        {
            if (bot.getChannelManager().isTeamMember(member))
            {
                TextChannel channel = bot.getChannelManager().getTextChannel(member);

                if (channel != null)
                {
                    channel.delete().queue();
                }
            }
        }
    }
}
