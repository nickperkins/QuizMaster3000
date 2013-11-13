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

public class AsyncBroadcast implements Runnable {

    private String message;
    private QuizMaster3000 plugin;
    private Object[] args;

    public AsyncBroadcast(QuizMaster3000 p, String m, Object... o) {
        plugin = p;
        message = m;
        args = o;

    }

    @Override
    public void run() {
        plugin.getServer().broadcastMessage(plugin.formatMessage(message, args));
    }
}
