package me.itsmas.servicebot.channel;

import me.itsmas.servicebot.Config;
import me.itsmas.servicebot.ServiceBot;
import me.itsmas.servicebot.util.Message;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Category;
import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.PermissionOverride;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

import java.awt.*;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

public class ChannelManager
{
    private final ServiceBot bot;

    private final Guild guild;
    private final Config config;

    public ChannelManager(ServiceBot bot)
    {
        this.bot = bot;

        this.guild = bot.getGuild();
        this.config = bot.getConfig();

        this.everyone = guild.getRoles().stream().filter(role -> role.getName().equals("@everyone")).findFirst().orElse(null);
    }

    private final Role everyone;

    /* */
    public void handleJoin(Member member)
    {
        TextChannel channel = getTextChannel(member);

        if (channel != null)
        {
            handleRejoin(member, channel);
            return;
        }

        if (isTeamMember(member))
        {
            return;
        }

        createChannel(member, config.getChannelCategory(), memberChannel ->
        {
            TextChannel textChannel = (TextChannel) memberChannel;

            // Deny from everyone
            textChannel.createPermissionOverride(everyone).setDeny(Permission.VIEW_CHANNEL).queue();

            // Allow channel owner and bypass role to see
            addOverride(textChannel, member);
            textChannel.createPermissionOverride(config.getBypassRole()).setAllow(allowPerms).queue();

            // Message stuff
            Message.CREATED_CHANNEL.send(config.getAlertChannel(), member.getAsMention(), textChannel.getAsMention());

            MessageEmbed embed = new EmbedBuilder().setColor(randomColour()).setTitle(Message.EMBED_TITLE.value()).setDescription(Message.EMBED_MESSAGE.value()).build();
            net.dv8tion.jda.core.entities.Message message = new MessageBuilder().append(Message.CREATED_WELCOME.format(member.getAsMention())).setEmbed(embed).build();

            textChannel.sendMessage(message).queue(msg -> textChannel.pinMessageById(msg.getId()).queue());
        });
    }

    public boolean isTeamMember(Member member)
    {
        return member.getRoles().contains(bot.getConfig().getBypassRole());
    }

    private Color randomColour()
    {
        Random random = ThreadLocalRandom.current();

        return new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
    }

    private Permission[] allowPerms = {Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_HISTORY, Permission.MESSAGE_WRITE};

    public void addOverride(TextChannel channel, Member member)
    {
        channel.createPermissionOverride(member).setAllow(allowPerms).queue();
    }

    public void removeOverride(TextChannel channel, Member member)
    {
        channel.getPermissionOverride(member).getManager().deny(Permission.VIEW_CHANNEL).queue(v ->
            channel.getPermissionOverride(member).delete().queue()
        );
    }

    private void allowViewChannel(TextChannel channel)
    {
        List<PermissionOverride> overrides = channel.getMemberPermissionOverrides();

        overrides.forEach(override ->
            override.getManager().grant(Permission.VIEW_CHANNEL, Permission.MESSAGE_READ, Permission.MESSAGE_HISTORY, Permission.MESSAGE_WRITE).queue()
        );
    }

    private void handleRejoin(Member member, TextChannel channel)
    {
        channel.createPermissionOverride(member).setAllow(allowPerms).queue(o ->
            channel.getManager().setParent(config.getChannelCategory()).queue(v -> allowViewChannel(channel))
        );
    }

    /* */
    public void handleLeave(Member member)
    {
        muteRoleAndMakeChannelInactive(member.getUser());
    }

    private void muteRoleAndMakeChannelInactive(User user)
    {
        if (user.isBot())
        {
            return;
        }

        TextChannel channel = getTextChannel(user);

        if (channel == null)
        {
            // Shouldn't happen
            return;
        }

        // Move channel to inactive category, make channel invisible
        channel.getManager().setParent(config.getInactiveCategory()).queue();

        channel.getMemberPermissionOverrides().forEach(override ->
            override.getManager().deny(Permission.VIEW_CHANNEL).queue()
        );
    }

    /* */
    public TextChannel getTextChannel(User user)
    {
        String wanted = formatChannelName(user).toLowerCase();

        for (TextChannel channel : guild.getTextChannels())
        {
            if (channel.getName().equals(wanted))
            {
                return channel;
            }
        }

        return null;
    }

    public TextChannel getTextChannel(Member member)
    {
        return getTextChannel(member.getUser());
    }

    private void createChannel(Member member, Category category, Consumer<Channel> consumer)
    {
        category.createTextChannel(formatChannelName(member)).queue(consumer);
    }

    private String formatChannelName(User user)
    {
        return (user.getName() + "-" + user.getDiscriminator()).replace(" ", "-");
    }

    private String formatChannelName(Member member)
    {
        return formatChannelName(member.getUser());
    }
}
