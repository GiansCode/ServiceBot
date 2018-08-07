package me.itsmas.servicebot.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public final class UtilJson
{
    private UtilJson() {}

    private final static JsonParser PARSER = new JsonParser();

    public static JsonObject parse(File file)
    {
        try
        {
            return PARSER.parse(new FileReader(file)).getAsJsonObject();
        }
        catch (IOException ex)
        {
            return null;
        }
    }
}
