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

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class QuizMaster3000Listener implements Listener {

	private QuizMaster3000 plugin;

	public QuizMaster3000Listener(QuizMaster3000 plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();

		if (plugin.state == QuizState.GETANSWER) {
			if (plugin.scores.containsKey(player)) {
				for (String a: plugin.currentQuestion.getAnswer()) {
					if (event.getMessage().equalsIgnoreCase(a)) {
						plugin.getServer().broadcastMessage(String.format("%sCorrect, %s!", ChatColor.GREEN, player.getName()));
						plugin.scores.put(player, plugin.scores.get(player) + 1);
						if (plugin.scores.get(player) == plugin.config.getInt("winningScore")) {
							plugin.thread.endQuiz();
						} else {
							plugin.thread.nextQuestion();
						}
					}		
				}


			}
		}
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		if (plugin.scores.containsKey(player)) {
			plugin.scores.remove(player);
		}
	}
}
