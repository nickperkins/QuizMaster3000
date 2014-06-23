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
import org.bukkit.scheduler.BukkitRunnable;

import java.text.MessageFormat;

class RegistrationRunnable implements Runnable {


    private int timer;
    private int id = -1;
    private final QuizMaster3000 plugin;

    public RegistrationRunnable(QuizMaster3000 p) {
        plugin = p;
    }


    public void start() {
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new BukkitRunnable() {
            @Override
            public void run() {
                plugin.getServer().broadcastMessage(plugin.prefixMessage(plugin.getMessages().getString("quiz.registration.start")));
            }
        });
        id = Bukkit.getScheduler().runTaskTimer(plugin, this, 15 * 20, 15 * 20).getTaskId();
        timer = 45;
    }

    @Override
    public void run() {
        if (timer <= 0) {
            if (plugin.getScores().size() == 0) {
                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new BukkitRunnable() {
                            @Override
                            public void run() {
                                plugin.getServer().broadcastMessage(plugin.prefixMessage(plugin.getMessages().getString("quiz.registration.noplayersjoined")));
                            }
                        });

                plugin.setState(QuizState.FINISHED);
                Bukkit.getScheduler().cancelTask(id);
                plugin.setRunning(false);

            } else {
                plugin.sendPlayers(plugin.getMessages().getString("quiz.registration.finished"));
                plugin.setState(QuizState.WAITFORNEXT);
                Bukkit.getScheduler().cancelTask(id);
                plugin.getWaitForNextRunnable().start();
            }
        } else {
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new BukkitRunnable() {
                        @Override
                        public void run() {
                            plugin.getServer().broadcastMessage(plugin.prefixMessage(MessageFormat.format(plugin.getMessages().getString("quiz.registration.timer"), timer)));
                        }
                    });
            timer -= 15;
        }
    }
}

