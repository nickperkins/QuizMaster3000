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

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

class QuizMaster3000Listener implements Listener {

    private final QuizMaster3000 plugin;

    public QuizMaster3000Listener(QuizMaster3000 plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (plugin.getState() != QuizState.FINISHED) {
            if (plugin.getScores().containsKey(event.getPlayer())) {
                event.setCancelled(true);
                if (event.isAsynchronous())
                    plugin.getServer().getScheduler().callSyncMethod(plugin, new CallableSendMessage(plugin, String.format("<%s> %s", event.getPlayer().getName(), event.getMessage())));
                else
                    plugin.sendPlayers(String.format("<%s> %s", event.getPlayer().getName(), event.getMessage()));

            }
        }
        if (plugin.getState() == QuizState.GETANSWER) {
            if (plugin.getScores().containsKey(event.getPlayer())) {
                if (event.isAsynchronous())
                    plugin.getServer().getScheduler().callSyncMethod(plugin, new CallableCheckAnswer(event.getPlayer(), plugin, event.getMessage()));
                else
                    plugin.checkAnswer(event.getPlayer(), event.getMessage());

            }

        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (plugin.getScores().containsKey(player)) {
            plugin.getScores().remove(player);
        }
    }

}
