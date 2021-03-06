package net.nperkins.quizmaster3000;

import org.bukkit.entity.Player;

import java.util.concurrent.Callable;

class CallableCheckAnswer implements Callable<Object> {

    private final Player player;
    private final QuizMaster3000 plugin;
    private final String message;


    CallableCheckAnswer(Player p, QuizMaster3000 pl, String m) {
        player = p;
        plugin = pl;
        message = m;
    }


    @Override
    public Object call() throws Exception {
        // Confirm we are still waiting for an answer
        if (plugin.getState() == QuizState.GETANSWER) {
           plugin.checkAnswer(player, message);
        }
        return null;
    }
}
