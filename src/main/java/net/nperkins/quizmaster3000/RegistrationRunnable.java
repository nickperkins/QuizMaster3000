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

import org.bukkit.Bukkit;

public class RegistrationRunnable implements Runnable {


    private int timer = 45;
    private int id = -1;
    private QuizMaster3000 plugin;

    public RegistrationRunnable(QuizMaster3000 p) {
        plugin = p;
    }


    public void start() {
        plugin.getServer().broadcastMessage(plugin.formatMessage("A new game of quiz has started. Type /quiz join to play! We start in 1 minute."));
        id = Bukkit.getScheduler().runTaskTimer(plugin, this, 15 * 20, 15 * 20).getTaskId();
    }

    @Override
    public void run() {
        if (timer <= 0) {
            if (plugin.scores.size() == 0) {
                plugin.getServer().broadcastMessage(plugin.formatMessage("No players joined the game. Maybe next time?"));
                plugin.state = QuizState.FINISHED;
                Bukkit.getScheduler().cancelTask(id);
                plugin.setRunning(false);
            } else {
                plugin.getServer().broadcastMessage(plugin.formatMessage("Just type your answers into chat. Ready? Let's play!"));
                plugin.state = QuizState.WAITFORNEXT;
                Bukkit.getScheduler().cancelTask(id);
                plugin.getWaitForNextRunnable().start();
            }
        } else {
            plugin.getServer().broadcastMessage(plugin.formatMessage("%d seconds until we start. Type /quiz join to play", timer));
            timer -= 15;
        }
    }
}

