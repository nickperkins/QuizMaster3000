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

import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class QuizMaster3000 extends JavaPlugin {

    private final QuizMaster3000CommandExecutor commandExecutor = new QuizMaster3000CommandExecutor(this);
    private final QuizMaster3000Listener listener = new QuizMaster3000Listener(this);

    public QuizThread thread = new QuizThread(this);
    volatile public QuizState state = QuizState.FINISHED;
    volatile HashMap<Player, Integer> scores = new HashMap<Player, Integer>();

    public ArrayList<Question> questions = new ArrayList<Question>();
    public Question currentQuestion = null;

    public FileConfiguration config = null;

    @Override
    public void onEnable() {

        this.saveDefaultConfig();

        this.config = this.getConfig();

        String quizfilepath = this.getDataFolder() + File.separator + "questions.dat";

        this.checkQuestions(quizfilepath);
        try {
            this.loadQuestions(quizfilepath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Register CommandExecutor
        getCommand("quiz").setExecutor(commandExecutor);
        getCommand("quizadmin").setExecutor(commandExecutor);

        // Register Listener
        getServer().getPluginManager().registerEvents(listener, this);
    }

    @Override
    public void onDisable() {

        // If quiz thread is running, better stop it
        if (thread.isRunning()) {
            stopQuiz();
        }
    }

    public void startQuiz() {
        if (!thread.isRunning()) {
            state = QuizState.REGISTRATION;
            thread.start();
        }
    }

    public void startAutoQuiz() {
        if (!thread.isRunning()) {
            thread.setAutoRun(true);
            state = QuizState.REGISTRATION;
            thread.start();
        }

    }

    public void stopQuiz() {
        state = QuizState.FINISHED;
        thread.endQuiz();
        getServer().broadcastMessage(formatMessage("%sQuiz Ended!", ChatColor.GOLD));

    }

    private void checkQuestions(String filePath) {

        File quizFile = new File(filePath);
        if (!quizFile.exists()) {
            Bukkit.getLogger().info(formatMessage("%sQuiz data file does not exist - providing default questions.", ChatColor.GOLD));

            if (!quizFile.exists()) {
                try {
                    quizFile.createNewFile();
                    InputStream in = (getClass().getResourceAsStream("/questions.dat"));
                    FileOutputStream out = new FileOutputStream(quizFile);
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = in.read(buffer)) != -1) {
                        out.write(buffer, 0, len);
                    }
                    out.close();
                } catch (IOException e) {
                    // no quiz
                }
            }
        }
    }

    private void loadQuestions(String quizfilepath) throws IOException {

        BufferedReader br = new BufferedReader(new FileReader(quizfilepath));
        String line;
        while ((line = br.readLine()) != null) {
            String[] splitLine = line.split("\\|");
            Question thisLine = new Question();
            thisLine.setQuestion(splitLine[0]);
            thisLine.setAnswer((String[]) ArrayUtils.subarray(splitLine, 1, splitLine.length));
            questions.add(thisLine);
        }
        br.close();
    }


    /**
     * Returns a formatted string using the specified string format prefixed with the configured prefix.
     *
     * @param message A format string
     * @param args    Arguments referenced by the format specifiers in the format string. If there are more arguments than format specifiers, the extra arguments are ignored. The number of arguments is variable and may be zero.
     * @return A formatted string
     */
    public String formatMessage(String message, Object... args) {
        message = ChatColor.translateAlternateColorCodes('&', config.getString("prefix") + " " + String.format(message, args));
        return message;
    }

    public void checkAnswer(Player player, String message) {

        if (state == QuizState.GETANSWER) {
            if (scores.containsKey(player)) {
                for (String a : currentQuestion.getAnswer()) {
                    if (message.equalsIgnoreCase(a)) {
                        getServer().broadcastMessage(String.format("%sCorrect, %s!", ChatColor.GREEN, player.getName()));
                        scores.put(player, scores.get(player) + 1);
                        if (scores.get(player) == config.getInt("winningScore")) {
                            thread.endQuiz();
                        } else {
                            thread.nextQuestion();
                        }
                    }
                }


            }
        }
    }
}