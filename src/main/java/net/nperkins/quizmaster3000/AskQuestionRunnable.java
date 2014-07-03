package net.nperkins.quizmaster3000;

/*
 *  This file is part of QuizMaster3000.
 *
 *  QuizMaster3000 is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  QuizMaster3000 is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with QuizMaster3000.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.logging.Level;

class AskQuestionRunnable implements Runnable {

    private int timer;
    private int id = -1;
    private final QuizMaster3000 plugin;

    public AskQuestionRunnable(QuizMaster3000 p) {
        plugin = p;
    }


    public void start() {
        plugin.getLogger().log(Level.INFO,"Asking next question.");
        timer = 45;
        plugin.askQuestion();
        id = Bukkit.getScheduler().runTaskTimer(plugin, this, 15 * 20, 15 * 20).getTaskId();
    }

    @Override
    public void run() {
        if (timer <= 0) {
            Bukkit.getScheduler().cancelTask(id);
            plugin.sendPlayers(plugin.getMessages().getString("quiz.question.timeup"));
            plugin.sendPlayers(MessageFormat.format(plugin.getMessages().getString("quiz.question.answer"), StringUtils.join(Arrays.copyOfRange(plugin.getCurrentQuestion().getAnswer(), 0, plugin.getCurrentQuestion().getAnswer().length), plugin.getMessages().getString("quiz.answer.joiner"))));
            if (plugin.getScores().size() == 0) {
                plugin.sendPlayers(plugin.prefixMessage(plugin.getMessages().getString("error.allplayersleft")));
                Bukkit.getScheduler().cancelTask(id);
                plugin.setState(QuizState.FINISHED);
                plugin.setRunning(false);
                plugin.setState(QuizState.WAITFORNEXT);
                Bukkit.getScheduler().cancelTask(id);
                plugin.sendPlayers(plugin.getMessages().getString("quiz.question.next"));
                plugin.getWaitForNextRunnable().start();
            }
        } else {
            plugin.sendPlayers(MessageFormat.format(plugin.getMessages().getString("quiz.question.timeleft"), timer));
            if (plugin.getConfig().getBoolean("quiz.hints"))
                plugin.sendPlayers(MessageFormat.format(plugin.getMessages().getString("quiz.question.hint"), plugin.getHint(timer * 2)));
            timer -= 15;
        }
    }

    public int getID() {
        return id;
    }
}
