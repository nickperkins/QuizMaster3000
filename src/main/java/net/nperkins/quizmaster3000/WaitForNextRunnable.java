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

import java.util.logging.Level;

class WaitForNextRunnable implements Runnable {

    private final QuizMaster3000 plugin;

    public WaitForNextRunnable(QuizMaster3000 p) {
        plugin = p;
    }

    public void start() {
        Bukkit.getScheduler().runTaskLater(plugin, this, 5 * 20);
    }

    @Override
    public void run() {
        plugin.getLogger().log(Level.INFO,plugin.prefixMessage("Waiting to ask next question"));
        plugin.setState(QuizState.ASKQUESTION);
        plugin.getQuestionRunnable().start();
    }


}
