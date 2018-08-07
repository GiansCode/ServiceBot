package me.itsmas.servicebot.util;

import java.util.Timer;
import java.util.TimerTask;

public final class Scheduler
{
    private Scheduler() {}

    private static final Timer timer = new Timer();

    public static void runScheduled(Runnable runnable, long delay, long interval)
    {
        timer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                runnable.run();
            }
        }, delay, interval);
    }
}
