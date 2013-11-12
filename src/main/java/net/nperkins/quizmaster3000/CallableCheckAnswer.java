package net.nperkins.quizmaster3000;

import org.bukkit.entity.Player;

import java.util.concurrent.Callable;

public class CallableCheckAnswer implements Callable {

    private Player player;
    private QuizMaster3000 plugin;
    private String message;


    CallableCheckAnswer(Player p, QuizMaster3000 pl, String m)
    {
        player = p;
        plugin = pl;
        message = m;
    }


    @Override
    public Object call() throws Exception {
        plugin.checkAnswer(player, message);
        return null;
    }
}
