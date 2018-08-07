package me.itsmas.servicebot;

import me.itsmas.servicebot.channel.ChannelManager;
import me.itsmas.servicebot.channel.InactivityListener;
import me.itsmas.servicebot.listener.CommandListener;
import me.itsmas.servicebot.listener.UserListener;
import me.itsmas.servicebot.util.Logs;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Guild;

public class ServiceBot
{
    /* */
    public static void main(String[] args)
    {
        new ServiceBot();
    }
    /* */

    private Config config; public Config getConfig() { return  config; }

    private JDA jda; public JDA getJda() { return jda; }
    private Guild guild; public Guild getGuild() { return guild; }

    private ChannelManager channelManager; public ChannelManager getChannelManager() { return channelManager; }

    private ServiceBot()
    {
        if (loadConfig() && connectJda() && loadGuild())
        {
            channelManager = new ChannelManager(this);

            registerListener(new UserListener(this), new CommandListener(this), new InactivityListener(this));
        }
    }

    private void registerListener(Object... objects)
    {
        for (Object object : objects)
        {
            jda.addEventListener(object);
        }
    }

    private boolean loadGuild()
    {
        Logs.info("Connected to Discord successfully");

        guild = jda.getGuildById(config.getString("guildId"));

        if (guild == null)
        {
            Logs.error("Invalid guild ID in config");
            return false;
        }

        Logs.info("Connected to guild " + guild.getName());

        if (config.loadData())
        {
            Logs.info("Data loaded successfully");
            return true;
        }

        return false;
    }

    private boolean loadConfig()
    {
        config = new Config(this);

        if (!config.isLoaded())
        {
            Logs.error("Error loading configuration");
            return false;
        }

        return true;
    }

    private boolean connectJda()
    {
        Logs.info("Attempting to connect to Discord...");

        try
        {
            jda = new JDABuilder(AccountType.BOT).setToken(config.getBotToken()).buildBlocking();
            Logs.info("Connected to bot " + jda.getSelfUser().getName());

            return true;
        }
        catch (Exception ex)
        {
            Logs.error("Error connecting to Discord:");
            ex.printStackTrace();
        }

        return false;
    }
}
