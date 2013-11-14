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

public class QuizMaster3000CommandExecutor implements CommandExecutor {

    private QuizMaster3000 plugin;

    public QuizMaster3000CommandExecutor(QuizMaster3000 plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("quiz")) {
            return commandQuiz(sender, args);
        }
        if (cmd.getName().equalsIgnoreCase("quizadmin")) {
            return commandQuizAdmin(sender, args);
        }
        return false;
    }

    public boolean commandQuiz(CommandSender sender, String[] args) {
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("join")) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(plugin.formatMessage("This is for players only!"));
                } else {
                    if (plugin.state == QuizState.REGISTRATION) {
                        if (plugin.scores.containsKey((Player) sender)) {
                            sender.sendMessage(plugin.formatMessage("You have already joined this quiz round!"));
                            return true;
                        } else {
                            plugin.scores.put((Player) sender, 0);
                            sender.sendMessage(plugin.formatMessage("You have been added to the quiz!"));
                            return true;
                        }
                    } else if (plugin.state != QuizState.FINISHED) {
                        sender.sendMessage(plugin.formatMessage("The current quiz has already started."));
                        return true;
                    } else {
                        sender.sendMessage(plugin.formatMessage("There is no quiz thread running."));
                    }
                    return true;
                }
            }
            if (args[0].equalsIgnoreCase("help")) {
                String[] help = {
                        "---------------  QuizMaster3000 Help  ---------------",
                        "/quiz join - Join the currently running quiz"//,
                        //"/quiz scores - Show the scores for the current quiz round"
                };
                sender.sendMessage(help);
                return true;
            }
        }
        return false;
    }

    public boolean commandQuizAdmin(CommandSender sender, String[] args) {
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("start")) {
                if (plugin.state == QuizState.FINISHED) {
                    plugin.startQuiz();
                } else {
                    sender.sendMessage(plugin.formatMessage("There is already a quiz game started."));
                }
                return true;
            }
            if (args[0].equalsIgnoreCase("stop")) {
                if (plugin.state != QuizState.FINISHED || plugin.getRunning()) {
                    plugin.stopQuiz();
                } else {
                    sender.sendMessage(plugin.formatMessage("There is no quiz thread running."));
                }
                return true;
            }
            if (args[0].equalsIgnoreCase("autorun")) {
                if (plugin.state == QuizState.FINISHED) {
                    plugin.startAutoQuiz();
                } else {
                    sender.sendMessage(plugin.formatMessage("There is already a quiz game started."));
                }
                return true;
            }
            if (args[0].equalsIgnoreCase("help")) {
                String[] help = {
                        "---------------  QuizMaster3000 Admin Help  ---------------",
                        "/quizadmin start - start a quiz",
                        "/quizadmin autorun - start a quiz, then keep running them after a delay",
                        "/quizadmin start - stop the currently running quiz (including during autorun delay)"

                };
                sender.sendMessage(help);
                return true;
            }

        }

        return false;
    }
}
