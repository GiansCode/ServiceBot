package me.itsmas.servicebot.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.dv8tion.jda.core.entities.TextChannel;

public enum Message
{
    CREATED_CHANNEL,
    ADD_USAGE,
    REMOVE_USAGE,
    INVALID_USER,
    NO_BOTS,
    NO_SELF,
    ALREADY_IN,
    NOT_IN,
    ADDED_USER,
    REMOVED_USER,
    CREATED_WELCOME,
    CHANNEL_BECAME_INACTIVE,
    CHANNEL_NO_LONGER_INACTIVE,
    BYPASS_USER,
    EMBED_TITLE,
    EMBED_MESSAGE;

    private String value = name();

    private void setValue(String value)
    {
        this.value = value;
    }

    public String value()
    {
        return value;
    }

    public void send(TextChannel channel, Object... args)
    {
        if (!value.isEmpty())
        {
            channel.sendMessage(format(args)).queue();
        }
    }

    public String format(Object... args)
    {
        return String.format(value, args);
    }

    public static void load(JsonObject data)
    {
        data.entrySet().forEach(entry ->
        {
            String key = entry.getKey();
            JsonElement element = entry.getValue();

            String value = null;

            if (element.isJsonArray())
            {
                StringBuilder builder = new StringBuilder();

                element.getAsJsonArray().forEach(arrElement -> builder.append(arrElement.getAsJsonPrimitive().getAsString()).append("\n"));

                value = builder.toString();
            }
            else if (element.isJsonPrimitive())
            {
                value = element.getAsJsonPrimitive().getAsString();
            }

            if (value == null)
            {
                Logs.error("Error parsing message '" + key + "'");
                return;
            }

            try
            {
                valueOf(key.toUpperCase()).setValue(value);
            }
            catch (IllegalArgumentException ex)
            {
                Logs.error("No value found for message '" + key + "'");
            }
        });
    }
}
