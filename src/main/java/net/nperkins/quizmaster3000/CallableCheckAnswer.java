package net.nperkins.quizmaster3000;

import org.bukkit.entity.Player;

import java.text.MessageFormat;
import java.util.concurrent.Callable;
import java.util.logging.Level;

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
        if (plugin.getState() == QuizState.ASKQUESTION) {
            plugin.getLogger().log(Level.FINE, MessageFormat.format("Checking answer from {0}", player.getName()));
            plugin.checkAnswer(player, message);
        }
        return null;
    }
}
