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

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

class QuizMaster3000CommandExecutor implements CommandExecutor {

    private final QuizMaster3000 plugin;

    public QuizMaster3000CommandExecutor(QuizMaster3000 plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("quiz")) { //NON-NLS
            return commandQuiz(sender, args);
        }
        if (cmd.getName().equalsIgnoreCase("quizadmin")) { //NON-NLS
            return commandQuizAdmin(sender, args);
        }
        return false;
    }

    boolean commandQuiz(CommandSender sender, String[] args) {
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("join")) { //NON-NLS
                if (!(sender instanceof Player)) {
                    sender.sendMessage(plugin.prefixMessage(plugin.getMessages().getString("error.notplayer")));
                } else {
                    if (plugin.getState() == QuizState.REGISTRATION) {
                        if (plugin.getScores().containsKey((Player) sender)) {
                            sender.sendMessage(plugin.prefixMessage(plugin.getMessages().getString("error.alreadyjoined")));
                            return true;
                        } else {
                            plugin.getScores().put((Player) sender, 0);
                            sender.sendMessage(plugin.prefixMessage(plugin.getMessages().getString("quiz.registration.playerjoined")));
                            return true;
                        }
                    } else if (plugin.getState() != QuizState.FINISHED) {
                        sender.sendMessage(plugin.prefixMessage(plugin.getMessages().getString("error.alreadystarted")));
                        return true;
                    } else {
                        sender.sendMessage(plugin.prefixMessage(plugin.getMessages().getString("error.noquizrunning")));
                    }
                    return true;
                }
            }
            if (args[0].equalsIgnoreCase("scores")) { //NON-NLS
                plugin.displayScores((Player) sender);
                return true;
            }
            if (args[0].equalsIgnoreCase("help")) { //NON-NLS
                String[] help = plugin.getMessages().getString("quiz.help").split(",");
                sender.sendMessage(help);
                return true;
            }
        }
        return false;
    }

    boolean commandQuizAdmin(CommandSender sender, String[] args) {
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("start")) { //NON-NLS
                if (plugin.getState() == QuizState.FINISHED) {
                    plugin.startQuiz();
                } else {
                    sender.sendMessage(plugin.prefixMessage(plugin.getMessages().getString("error.alreadystarted")));
                }
                return true;
            }
            if (args[0].equalsIgnoreCase("stop")) { //NON-NLS
                if (plugin.getState() != QuizState.FINISHED || plugin.getRunning()) {
                    plugin.stopQuiz();
                } else {
                    sender.sendMessage(plugin.prefixMessage(plugin.getMessages().getString("error.noquizrunning")));
                }
                return true;
            }
            if (args[0].equalsIgnoreCase("autorun")) { //NON-NLS
                if (plugin.getState() == QuizState.FINISHED) {
                    plugin.startAutoQuiz();
                } else {
                    sender.sendMessage(plugin.prefixMessage(plugin.getMessages().getString("error.alreadystarted")));
                }
                return true;
            }
            if (args[0].equalsIgnoreCase("help")) { //NON-NLS
                String[] help = plugin.getMessages().getString("quizadmin.help").split(",");
                sender.sendMessage(help);
                return true;
            }

        }

        return false;
    }
}
