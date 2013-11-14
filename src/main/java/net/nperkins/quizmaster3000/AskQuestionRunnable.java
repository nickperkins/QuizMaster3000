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

import java.util.Arrays;

public class AskQuestionRunnable implements Runnable {

    private int timer;
    private int id = -1;
    private QuizMaster3000 plugin;

    public AskQuestionRunnable(QuizMaster3000 p) {
        plugin = p;
    }


    public void start() {
        timer = 45;
        plugin.askQuestion();
        id = Bukkit.getScheduler().runTaskTimer(plugin, this, 15 * 20, 15 * 20).getTaskId();
    }

    @Override
    public void run() {
        if (timer <= 0) {
            Bukkit.getScheduler().cancelTask(id);
            plugin.getServer().broadcastMessage(plugin.formatMessage("Time's up!"));
            plugin.getServer().broadcastMessage(plugin.formatMessage("The correct answer was: %s", StringUtils.join(Arrays.copyOfRange(plugin.getCurrentQuestion().getAnswer(), 0, plugin.getCurrentQuestion().getAnswer().length), " or ")));
            if (plugin.scores.size() == 0) {
                plugin.getServer().broadcastMessage(plugin.formatMessage("Seems all the players from this round have left. No point going on, is there?"));
                Bukkit.getScheduler().cancelTask(id);
                plugin.state = QuizState.FINISHED;
            } else {
                plugin.getServer().broadcastMessage(plugin.formatMessage("OK, Next Question..."));
                plugin.state = QuizState.WAITFORNEXT;
                Bukkit.getScheduler().cancelTask(id);
                plugin.getWaitForNextRunnable().start();
            }
        } else {
            plugin.getServer().broadcastMessage(plugin.formatMessage("You have %d seconds left", timer));
            plugin.getServer().broadcastMessage(plugin.formatMessage("Hint: %s", plugin.getHint(timer * 2)));
            timer -= 15;
        }
    }

    public int getID() {
        return id;
    }
}