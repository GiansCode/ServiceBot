package me.itsmas.servicebot.util;

import java.util.Date;
import java.util.logging.Level;

public final class Logs
{
    private Logs() {}

    public static void info(String msg)
    {
        log(msg, Level.INFO);
    }

    public static void error(String msg)
    {
        log(msg, Level.SEVERE);
    }

    public static void checkNonNull(Object object, String error)
    {
        if (object == null)
        {
            error(error);
        }
    }

    private static void log(String msg, Level level)
    {
        System.out.println(String.format("[%s : %s] %s", new Date(), level, msg));
    }
}
