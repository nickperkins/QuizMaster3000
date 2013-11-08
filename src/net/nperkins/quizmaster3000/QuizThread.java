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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class QuizThread implements Runnable {

	private Thread t = null;

	private QuizMaster3000 plugin = null;

	QuizThread(QuizMaster3000 plugin) {
		this.plugin = plugin;
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
		t.interrupt();
		t = null;

	}

	public boolean isRunning() {
		if (t == null) {
			return false;
		}
		return true;
	}

	@Override
	public void run() {
		Thread thisThread = Thread.currentThread();
		Integer lastNumber = null;

		while (t == thisThread) {

			switch (plugin.state) {
			case FINISHED:
				if (!(plugin.scores.size() == 0)) {
					Map<Player, Integer> sortedScores = Util.sortScores(plugin.scores);
					plugin.getServer().broadcastMessage(Util.formatMessage("%sScores!", ChatColor.GOLD));
					for (Entry<Player, Integer> score : sortedScores.entrySet()) {
						plugin.getServer().broadcastMessage(Util.formatMessage("%s%s: %d points!", ChatColor.GOLD, score.getKey().getName(), score.getValue()));
					}
				}
				plugin.scores = new HashMap<>();
				this.stop();
				break;
			case REGISTRATION:
				plugin.getServer().broadcastMessage(Util.formatMessage("%sA new game of quiz has started. Type /quiz join to play! We start in 1 minute.", ChatColor.GOLD));
				try {
					Thread.sleep(1000 * 30);
					plugin.getServer().broadcastMessage(Util.formatMessage("%s30 seconds until we start. Type /quiz join to play!", ChatColor.GOLD));
					Thread.sleep(1000 * 20);
					plugin.getServer().broadcastMessage(Util.formatMessage("%s10 seconds until we start. Type /quiz join to play!", ChatColor.GOLD));
					Thread.sleep(1000 * 10);
					plugin.getServer().broadcastMessage(Util.formatMessage("%sJust type your answers into chat. Ready? Let's play!", ChatColor.GOLD));
					Thread.sleep(1000 * 5);
				} catch (InterruptedException e) {
					break;
				}
				plugin.state = QuizState.ASKQUESTION;
				break;
			case ASKQUESTION:
				Random ran = new Random();
				Integer thisNumber;
				do {
					thisNumber = ran.nextInt(plugin.questions.size());
				} while (thisNumber.equals(lastNumber));
				lastNumber = thisNumber;
				plugin.currentQuestion = plugin.questions.get(ran.nextInt(thisNumber));
				plugin.getServer().broadcastMessage(Util.formatMessage("%sQuestion: " + plugin.currentQuestion.getQuestion(), ChatColor.GOLD));
				plugin.state = QuizState.GETANSWER;
				try {
					Thread.sleep(1000 * 30);
					plugin.getServer().broadcastMessage(Util.formatMessage("%s30 seconds left...", ChatColor.GOLD));
					plugin.getServer().broadcastMessage(Util.formatMessage("%ssQuestion: " + plugin.currentQuestion.getQuestion(), ChatColor.GOLD));
					Thread.sleep(1000 * 20);
					plugin.getServer().broadcastMessage(Util.formatMessage("%s10 seconds left...", ChatColor.GOLD));
					plugin.getServer().broadcastMessage(Util.formatMessage("%sQuestion: " + plugin.currentQuestion.getQuestion(), ChatColor.GOLD));
					Thread.sleep(1000 * 10);
				} catch (InterruptedException e) {
					break;
				}
				break;
			case GETANSWER:
				plugin.getServer().broadcastMessage(Util.formatMessage("%sTime's up!", ChatColor.RED));
				plugin.getServer().broadcastMessage(Util.formatMessage("%sThe correct answer was: %s", ChatColor.GOLD, StringUtils.join(Arrays.copyOfRange(plugin.currentQuestion.getAnswer(), 0, plugin.currentQuestion.getAnswer().length), " or ")));
				plugin.state = QuizState.WAITFORNEXT;
				break;
			case WAITFORNEXT:
				plugin.getServer().broadcastMessage(Util.formatMessage("%sOK, Next Question...", ChatColor.GOLD));
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
