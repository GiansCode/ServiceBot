package me.itsmas.servicebot.listener;

import me.itsmas.servicebot.ServiceBot;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class UserListener extends ListenerAdapter
{
    private final ServiceBot bot;

    public UserListener(ServiceBot bot)
    {
        this.bot = bot;
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event)
    {
        Member member = event.getMember();

        if (!member.getUser().isBot())
        {
            bot.getChannelManager().handleJoin(member);
        }
    }

    @Override
    public void onGuildMemberLeave(GuildMemberLeaveEvent event)
    {
        Member member = event.getMember();

        if (!member.getUser().isBot())
        {
            bot.getChannelManager().handleLeave(member);
        }
    }
}
