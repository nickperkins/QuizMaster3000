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
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.Metrics;

import java.io.*;
import java.text.MessageFormat;
import java.util.*;

public class QuizMaster3000 extends JavaPlugin {

    // Localisation
    private ResourceBundle messages;
    private static Locale locale;

    // Configuration
    private FileConfiguration config = null;

    // Listeners
    private final QuizMaster3000CommandExecutor commandExecutor = new QuizMaster3000CommandExecutor(this);
    private final QuizMaster3000Listener listener = new QuizMaster3000Listener(this);

    // Runnables
    private final RegistrationRunnable registrationRunnable = new RegistrationRunnable(this);
    private final AskQuestionRunnable questionRunnable = new AskQuestionRunnable(this);
    private final WaitForNextRunnable waitForNextRunnable = new WaitForNextRunnable(this);
    private final AutoRunRunnable autoRunRunnable = new AutoRunRunnable(this);


    // Quiz status info
    private QuizState state = QuizState.FINISHED;
    private Boolean autoRun = false;
    private Boolean isRunning = false;

    // Quiz game information
    private HashMap<Player, Integer> scores = new HashMap<Player, Integer>();
    private final ArrayList<Question> questions = new ArrayList<Question>();
    private Question currentQuestion = null;
    private Integer lastQuestion = null;

    public QuizMaster3000() {
        setLocale(Locale.getDefault());
    }

    public static Locale getLocale() {
        return locale;
    }

    void setLocale(Locale l) {
        locale = l;
        messages = ResourceBundle.getBundle("Messages", l); //NON-NLS
    }

    public ResourceBundle getMessages() {
        return messages;
    }

    public QuizState getState() {
        return state;
    }

    public void setState(QuizState state) {
        this.state = state;
    }

    public HashMap<Player, Integer> getScores() {
        return scores;
    }

    public AskQuestionRunnable getQuestionRunnable() {
        return questionRunnable;
    }

    public WaitForNextRunnable getWaitForNextRunnable() {
        return waitForNextRunnable;
    }

    public AutoRunRunnable getAutoRunRunnable() {
        return autoRunRunnable;
    }

    public Boolean getRunning() {
        return isRunning;
    }

    public void setRunning(Boolean running) {
        isRunning = running;
    }

    public Question getCurrentQuestion() {
        return currentQuestion;
    }

    public Boolean getAutoRun() {
        return autoRun;
    }

    @Override
    public void onEnable() {

        //Metrics
        try {
            Metrics metrics = new Metrics(this);
            metrics.start();
        } catch (IOException e) {
            // Failed to submit the stats :-(
        }

        processConfig();

        String quizfilepath = this.getDataFolder() + File.separator + "questions.dat"; //NON-NLS

        this.checkQuestions(quizfilepath);
        try {
            this.loadQuestions(quizfilepath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Register CommandExecutor
        getCommand("quiz").setExecutor(commandExecutor); //NON-NLS
        getCommand("quizadmin").setExecutor(commandExecutor); //NON-NLS

        // Register Listener
        getServer().getPluginManager().registerEvents(listener, this);
    }

    @Override
    public void onDisable() {

        // If quiz thread is running, better stop it
        if (isRunning) {
            stopQuiz();
        }
    }

    void processConfig() {
        final Map<String, Object> defParams = new LinkedHashMap<String, Object>();

        this.config = this.getConfig();
        config.options().copyDefaults(true);

        // This is the default configuration
        defParams.put("general.prefix", "&d[Quiz]&f"); //NON-NLS NON-NLS
        defParams.put("quiz.winningScore", 5); //NON-NLS
        defParams.put("quiz.hints", true); //NON-NLS
        defParams.put("quiz.autorun.default", false); //NON-NLS
        defParams.put("quiz.autorun.delay", 300); //NON-NLS

        // If config does not include a default parameter, add it
        for (final Map.Entry<String, Object> e : defParams.entrySet()) {
            if (!config.contains(e.getKey())) {
                config.set(e.getKey(), e.getValue());
                getLogger().info(MessageFormat.format(messages.getString("plugin.configadd"), e.getKey()));
            }
        }
        // Save default values to config.yml in data directory
        this.saveConfig();
    }

    public void startQuiz() {
        if (!isRunning) {
            if (config.getBoolean("quiz.autorun.default")) autoRun = true; //NON-NLS
            state = QuizState.REGISTRATION;
            isRunning = true;
            registrationRunnable.start();
        }
    }


    public void startAutoQuiz() {
        if (!isRunning) {
            autoRun = true;
            state = QuizState.REGISTRATION;
            isRunning = true;
            registrationRunnable.start();
        }
    }

    public void stopQuiz() {
        state = QuizState.FINISHED;
        autoRun = false;  // In case it is auto running
        getServer().getScheduler().cancelTasks(this);
        scores = new HashMap<Player, Integer>();
        getServer().broadcastMessage(messages.getString("quiz.stopped"));
        isRunning = false;

    }

    private void checkQuestions(String filePath) {

        File quizFile = new File(filePath);
        if (!quizFile.exists()) {
            Bukkit.getLogger().info(prefixMessage(messages.getString("error.noquizdata")));

            if (!quizFile.exists()) {
                try {
                    quizFile.createNewFile();
                    InputStream in = (getClass().getResourceAsStream("/questions.dat")); //NON-NLS
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
        Question thisLine = new Question();
        while ((line = br.readLine()) != null) {
            String[] splitLine = line.split("\\|");
            thisLine.setQuestion(splitLine[0]);
            thisLine.setAnswer((String[]) ArrayUtils.subarray(splitLine, 1, splitLine.length));
            questions.add(thisLine);
        }
        br.close();
    }

    public String prefixMessage(String message) {
        message = ChatColor.translateAlternateColorCodes('&', config.getString("general.prefix") + ' ' + message); //NON-NLS
        return message;
    }

    public void askQuestion() {
        Random ran = new Random();
        Integer thisNumber;
        do {
            thisNumber = ran.nextInt(questions.size());
        } while (thisNumber.equals(lastQuestion));
        lastQuestion = thisNumber;
        currentQuestion = questions.get(thisNumber);

        getServer().broadcastMessage(prefixMessage(MessageFormat.format(messages.getString("quiz.question.question"), currentQuestion.getQuestion())));
        state = QuizState.GETANSWER;
    }

    public void checkAnswer(Player player, String message) {

        if (state == QuizState.GETANSWER) {
            if (scores.containsKey(player)) {
                for (String a : currentQuestion.getAnswer()) {
                    if (message.equalsIgnoreCase(a)) {
                        getServer().getScheduler().cancelTask(questionRunnable.getID());
                        getServer().broadcastMessage(MessageFormat.format(messages.getString("quiz.question.playercorrect"), ChatColor.GREEN, player.getName()));
                        scores.put(player, scores.get(player) + 1);
                        if (scores.get(player) == config.getInt("quiz.winningScore")) { //NON-NLS
                            finishQuiz();
                        } else {
                            waitForNextRunnable.start();
                        }
                    }
                }
            } else {
                getServer().broadcastMessage(messages.getString("error.notplaying"));
            }
        }
    }

    void finishQuiz() {
        if (scores.size() != 0) {
            Map<Player, Integer> sortedScores = Util.sortScores(scores);
            getServer().broadcastMessage(prefixMessage(prefixMessage("---------- " + messages.getString("quiz.scores.final") + " ----------")));
            for (Map.Entry<Player, Integer> score : sortedScores.entrySet()) {
                getServer().broadcastMessage(prefixMessage(MessageFormat.format(messages.getString("quiz.scores.points"), score.getKey().getName(), score.getValue())));
            }
            scores = new HashMap<Player, Integer>();
            isRunning = false;
        }
        if (autoRun) {
            getServer().broadcastMessage(prefixMessage(messages.getString("quiz.autorun.nextgame")));
            autoRunRunnable.start();
        }
    }

    public void displayScores(Player p) {
        if (scores.size() != 0) {
            Map<Player, Integer> sortedScores = Util.sortScores(scores);
            p.sendMessage(prefixMessage("---------- " + messages.getString("quiz.scores.interim") + " ----------"));
            for (Map.Entry<Player, Integer> score : sortedScores.entrySet()) {
                p.sendMessage(prefixMessage(prefixMessage(MessageFormat.format(messages.getString("quiz.scores.points"), score.getKey().getName(), score.getValue()))));
            }
        } else {
            p.sendMessage(prefixMessage(messages.getString("error.noscores")));
        }
    }

    public String getHint(Integer p) throws IllegalArgumentException {

        if (p > 100) {
            throw new IllegalArgumentException();
        }

        List<String> hint = new ArrayList<String>();

        Random r = new Random();
        List<Integer> replacedIndexes = new ArrayList<Integer>();
        for (String s : this.currentQuestion.getAnswer()) {

            char[] a = s.toCharArray();

            Integer charToReplace = Math.round((float) a.length * (p / 100f));

            while (replacedIndexes.size() < charToReplace) {
                Integer index;
                do {
                    index = r.nextInt(a.length);
                } while (replacedIndexes.contains(index));
                if (a[index] != ' ') a[index] = '*';
                replacedIndexes.add(index);
            }
            hint.add(new String(a));
        }

        return StringUtils.join(hint, messages.getString("quiz.answer.joiner"));
    }


}

