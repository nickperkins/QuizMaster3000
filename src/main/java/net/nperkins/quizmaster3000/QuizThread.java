package net.nperkins.quizmaster3000;

/* This file is part of QuizMaster3000.

 QuizMaster3000 is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 QuizMaster3000 is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with QuizMaster3000.  If not, see <http://www.gnu.org/licenses/>.
 */

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

public class QuizThread implements Runnable {

    private Thread t = null;
    private QuizMaster3000 plugin = null;
    private Boolean autoRun = false;


    QuizThread(QuizMaster3000 plugin) {
        this.plugin = plugin;

    }

    public void setAutoRun(Boolean autoRun) {
        this.autoRun = autoRun;
    }

    public void start() {
        t = new Thread(this);
        t.start();
    }

    public void nextQuestion() {
        plugin.state = QuizState.WAITFORNEXT;
        t.interrupt();

    }

    public void endQuiz() {
        plugin.state = QuizState.FINISHED;
        t.interrupt();
    }

    public void stop() {
        setAutoRun(false);
        t.interrupt();
        t = null;

    }

    public boolean isRunning() {
        return t != null;
    }

    public boolean isAutoRun() {
        return autoRun;
    }

    @Override
    public void run() {
        Thread thisThread = Thread.currentThread();
        Integer lastNumber = null;

        while (t == thisThread) {

            switch (plugin.state) {
                case FINISHED:
                    synchronized (plugin.scores) {
                        if (plugin.scores.size() != 0) {
                            Map<Player, Integer> sortedScores = Util.sortScores(plugin.scores);
                            plugin.getServer().broadcastMessage(plugin.formatMessage("%sScores!", ChatColor.GOLD));
                            for (Entry<Player, Integer> score : sortedScores.entrySet()) {
                                plugin.getServer().broadcastMessage(plugin.formatMessage("%s%s: %d points!", ChatColor.GOLD, score.getKey().getName(), score.getValue()));
                            }
                        } else {
                            plugin.scores = new HashMap<Player, Integer>();
                        }
                    }

                    if (this.isAutoRun()) {
                        try {
                            plugin.getServer().getScheduler().runTask(plugin, new AsyncBroadcast(plugin, "%sWe'll be back soon!", ChatColor.GOLD));
                            Thread.sleep(plugin.config.getInt("autorun.delay") * 1000);
                            plugin.state = QuizState.REGISTRATION;
                            break;
                        } catch (InterruptedException e) {
                            break;
                        }
                    } else {
                        t = null;
                    }
                    break;
                case REGISTRATION:
                    plugin.getServer().getScheduler().runTask(plugin, new AsyncBroadcast(plugin, "%sA new game of quiz has started. Type /quiz join to play! We start in 1 minute.", ChatColor.GOLD));
                    try {
                        Thread.sleep(1000 * 30);
                        plugin.getServer().getScheduler().runTask(plugin, new AsyncBroadcast(plugin, "%s30 seconds until we start. Type /quiz join to play!", ChatColor.GOLD));
                        Thread.sleep(1000 * 20);
                        plugin.getServer().getScheduler().runTask(plugin, new AsyncBroadcast(plugin, "%s10 seconds until we start. Type /quiz join to play!", ChatColor.GOLD));
                        Thread.sleep(1000 * 10);
                        if (plugin.scores.size() == 0) {
                            plugin.getServer().getScheduler().runTask(plugin, new AsyncBroadcast(plugin, "%sNo players joined the game. Maybe next time?", ChatColor.GOLD));
                            plugin.state = QuizState.FINISHED;
                        } else {
                            plugin.getServer().getScheduler().runTask(plugin, new AsyncBroadcast(plugin, "%sJust type your answers into chat. Ready? Let's play!", ChatColor.GOLD));
                            Thread.sleep(1000 * 5);
                            plugin.state = QuizState.ASKQUESTION;
                        }
                    } catch (InterruptedException e) {
                        break;
                    }
                    break;
                case ASKQUESTION:
                    Random ran = new Random();
                    synchronized (plugin.questions) {
                        Integer thisNumber;
                        do {
                            thisNumber = ran.nextInt(plugin.questions.size());
                        } while (thisNumber.equals(lastNumber));
                        lastNumber = thisNumber;
                        plugin.currentQuestion = plugin.questions.get(thisNumber);
                    }
                    plugin.getServer().getScheduler().runTask(plugin, new AsyncBroadcast(plugin, "%sQuestion: " + plugin.currentQuestion.getQuestion(), ChatColor.GOLD));
                    plugin.state = QuizState.GETANSWER;
                    try {
                        Thread.sleep(1000 * 15);
                        plugin.getServer().getScheduler().runTask(plugin, new Runnable() {
                            public void run() {
                                plugin.getServer().broadcastMessage(plugin.formatMessage("%s45 seconds left...", ChatColor.GOLD));
                                plugin.getServer().broadcastMessage(plugin.formatMessage("%sHint: %s", ChatColor.GOLD, QuizMaster3000.createHint(plugin.currentQuestion.getAnswer(), 90)));
                            }

                        });
                        Thread.sleep(1000 * 15);
                        plugin.getServer().getScheduler().runTask(plugin, new Runnable() {
                            public void run() {
                                plugin.getServer().broadcastMessage(plugin.formatMessage("%s30 seconds left...", ChatColor.GOLD));
                                plugin.getServer().broadcastMessage(plugin.formatMessage("%sHint: %s", ChatColor.GOLD, QuizMaster3000.createHint(plugin.currentQuestion.getAnswer(), 60)));
                            }

                        });
                        Thread.sleep(1000 * 15);
                        plugin.getServer().getScheduler().runTask(plugin, new Runnable() {
                            public void run() {
                                plugin.getServer().broadcastMessage(plugin.formatMessage("%s15 seconds left...", ChatColor.GOLD));
                                plugin.getServer().broadcastMessage(plugin.formatMessage("%sHint: %s", ChatColor.GOLD, QuizMaster3000.createHint(plugin.currentQuestion.getAnswer(), 30)));
                            }

                        });
                        Thread.sleep(1000 * 15);
                    } catch (InterruptedException e) {
                        break;
                    }
                    break;
                case GETANSWER:
                    plugin.getServer().getScheduler().runTask(plugin, new AsyncBroadcast(plugin, "%sTime's up!", ChatColor.RED));
                    plugin.getServer().getScheduler().runTask(plugin, new AsyncBroadcast(plugin, "%sThe correct answer was: %s", ChatColor.GOLD, StringUtils.join(Arrays.copyOfRange(plugin.currentQuestion.getAnswer(), 0, plugin.currentQuestion.getAnswer().length), " or ")));
                    if (plugin.scores.size() == 0) {
                        plugin.getServer().getScheduler().runTask(plugin, new AsyncBroadcast(plugin, "%sSeems all the players from this round have left. No point going on, is there?", ChatColor.GOLD));
                        plugin.state = QuizState.FINISHED;
                    } else {
                        plugin.state = QuizState.WAITFORNEXT;
                    }

                    break;
                case WAITFORNEXT:
                    plugin.getServer().getScheduler().runTask(plugin, new AsyncBroadcast(plugin, "%sOK, Next Question...", ChatColor.GOLD));
                    plugin.state = QuizState.ASKQUESTION;
                    try {
                        Thread.sleep(1000 * 5);
                    } catch (InterruptedException e) {
                        break;
                    }
                default:
                    break;
            }

        }

    }


}
