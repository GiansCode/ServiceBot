package me.itsmas.servicebot;

import com.google.gson.JsonObject;
import me.itsmas.servicebot.util.Logs;
import me.itsmas.servicebot.util.Message;
import me.itsmas.servicebot.util.UtilJson;
import net.dv8tion.jda.core.entities.Category;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Config
{
    private final ServiceBot bot;

    Config(ServiceBot bot)
    {
        this.bot = bot;

        loadConfig();
    }

    String getBotToken()
    {
        return getString("botToken");
    }

    private Category channelCategory;

    public Category getChannelCategory()
    {
        return channelCategory;
    }

    private Category inactiveCategory;

    public Category getInactiveCategory()
    {
        return inactiveCategory;
    }

    private TextChannel alertChannel;

    public TextChannel getAlertChannel()
    {
        return alertChannel;
    }

    private Role bypassRole;

    public Role getBypassRole()
    {
        return bypassRole;
    }

    private Role defaultRole;

    public Role getDefaultRole()
    {
        return defaultRole;
    }

    boolean loadData()
    {
        channelCategory = bot.getGuild().getCategoryById(getString("channelCategoryId"));
        inactiveCategory = bot.getGuild().getCategoryById(getString("inactiveCategoryId"));

        alertChannel = bot.getGuild().getTextChannelById(getString("alertChannelId"));

        bypassRole = bot.getGuild().getRoleById(getString("bypassRoleId"));
        defaultRole = bot.getGuild().getRoleById(getString("defaultRoleId"));

        bot.getJda().getPresence().setGame(Game.playing(getString("gamePresence")));

        Logs.checkNonNull(channelCategory, "Could not find category from value of \"channelCategoryId\"");
        Logs.checkNonNull(inactiveCategory, "Could not find category from value of \"inactiveCategoryId\"");
        Logs.checkNonNull(bypassRole, "Could not find role from value of \"bypassRoleId\"");

        Message.load(configObject.getAsJsonObject("messages"));

        return (channelCategory != null && inactiveCategory != null && alertChannel != null && bypassRole != null);
    }

    /* */
    private JsonObject configObject;

    public String getString(String key)
    {
        return configObject.get(key).getAsString();
    }

    public int getInt(String key)
    {
        return configObject.get(key).getAsInt();
    }

    boolean isLoaded()
    {
        return configObject != null;
    }

    /* */
    private void loadConfig()
    {
        File file = new File("config.json");

        if (!file.exists())
        {
            copyConfigResource(file);
            Logs.info("Created config file - please enter information and re-run the program");

            return;
        }

        configObject = UtilJson.parse(file);

        if (configObject == null)
        {
            Logs.error("Error parsing config from file");
        }
    }

    private void copyConfigResource(File destination)
    {
        InputStream stream = ServiceBot.class.getClassLoader().getResourceAsStream("config.json");

        try
        {
            Files.copy(stream, Paths.get(destination.getAbsolutePath()));
        }
        catch (IOException ex)
        {
            Logs.error("Unable to copy JAR resource to disk");
        }
    }
}
