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
import org.apache.commons.lang.LocaleUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.Metrics;

import java.io.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Level;

public class QuizMaster3000 extends JavaPlugin {

    // Localisation
    private ResourceBundle messages;

    // Configuration
    private FileConfiguration config = null;

    // Listeners
    private final QuizMaster3000CommandExecutor commandExecutor = new QuizMaster3000CommandExecutor(this);
    private final QuizMaster3000Listener listener = new QuizMaster3000Listener(this);

    // Runnables
    private final RegistrationRunnable registrationRunnable = new RegistrationRunnable(this);
    private final AskQuestionRunnable questionRunnable = new AskQuestionRunnable(this);
    private final WaitForNextRunnable waitForNextRunnable = new WaitForNextRunnable(this);


    // Quiz status info
    private QuizState state = QuizState.FINISHED;
    private Boolean isRunning = false;

    // Quiz game information
    private HashMap<Player, Integer> scores = new HashMap<Player, Integer>();
    private final ArrayList<Question> questions = new ArrayList<Question>();
    private Question currentQuestion = null;
    private Integer lastQuestion = null;

    public QuizMaster3000() {
        setLocale(Locale.getDefault());
    }

    /**
     * Set the locale to be used
     *
     * @param l a Locale
     */
    private void setLocale(Locale l) {
        messages = ResourceBundle.getBundle("Messages", l); //NON-NLS
        if (!messages.getLocale().toString().isEmpty())
            getLogger().info(MessageFormat.format(messages.getString("plugin.localeloaded"), messages.getLocale()));
    }

    /**
     * Gets the currently loaded ResourceBundle
     *
     * @return a ResourceBundle
     */
    public ResourceBundle getMessages() {
        return messages;
    }

    /**
     * Get the current state of the quiz
     *
     * @return a {@link QuizState}
     */
    public QuizState getState() {
        return state;
    }

    /**
     * Set the state of the quiz
     *
     * @param state a {@link QuizState}
     */
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

    public Boolean getRunning() {
        return isRunning;
    }

    public void setRunning(Boolean running) {
        isRunning = running;
    }

    public Question getCurrentQuestion() {
        return currentQuestion;
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

        // Set locale if available
        if (config.isSet("general.locale")) {
            setLocale(LocaleUtils.toLocale(config.getString("general.locale")));
        }

        String quizfilepath = this.getDataFolder() + File.separator + "questions.dat"; //NON-NLS

        this.checkQuestions(quizfilepath);
        try {
            this.loadQuestions(quizfilepath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Set default state
        state = QuizState.FINISHED;

        // Register CommandExecutor
        getCommand("qm").setExecutor(commandExecutor); //NON-NLS
        getCommand("qmadmin").setExecutor(commandExecutor); //NON-NLS

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

    /**
     * Process the plugin configuration
     */
    void processConfig() {
        final Map<String, Object> defParams = new LinkedHashMap<String, Object>();

        this.config = this.getConfig();
        config.options().copyDefaults(true);

        // This is the default configuration
        defParams.put("general.prefix", "&d[Quiz]&f"); //NON-NLS NON-NLS
        defParams.put("quiz.winningScore", 5); //NON-NLS
        defParams.put("quiz.hints", true); //NON-NLS

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

    /**
     * Start a quiz
     */
    public void startQuiz() {
        if (!isRunning) {
            state = QuizState.REGISTRATION;
            isRunning = true;
            registrationRunnable.start();
        }
    }

    /**
     * Stop the currently running quiz, and any autoquiz wait period
     */
    public void stopQuiz() {
        state = QuizState.FINISHED;
        getServer().getScheduler().cancelTasks(this);
        sendPlayers(messages.getString("quiz.stopped"));
        scores = new HashMap<Player, Integer>();
        isRunning = false;

    }

    /**
     * Check for question database file and save default questions if none exist
     *
     * @param filePath Location to check for question database file
     */
    private void checkQuestions(String filePath) {

        File quizFile = new File(filePath);
        if (!quizFile.exists()) {
            Bukkit.getLogger().info(prefixMessage(messages.getString("error.noquizdata")));

            if (!quizFile.exists()) {
                try {
                    //noinspection ResultOfMethodCallIgnored
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

    /**
     * Load questions from a quizfile
     *
     * @param quizfilepath File path of quizfile
     * @throws IOException
     */
    private void loadQuestions(String quizfilepath) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(quizfilepath));
        String line;
        while ((line = br.readLine()) != null) {
            Question thisLine = new Question();
            String[] splitLine = line.split("\\|");
            if (splitLine.length < 2) {
                getLogger().info(prefixMessage(messages.getString("error.invalidquestion")));
                continue;
            }
            thisLine.setQuestion(splitLine[0]);
            thisLine.setAnswer((String[]) ArrayUtils.subarray(splitLine, 1, splitLine.length));
            questions.add(thisLine);
        }
        br.close();
    }

    /**
     * Prefix messages with configured prefix text
     *
     * @param message Mesasge to be prefixed
     * @return message with prefix added
     */
    public String prefixMessage(String message) {
        message = ChatColor.translateAlternateColorCodes('&', config.getString("general.prefix") + ' ' + message); //NON-NLS
        return message;
    }

    /**
     * Ask a random question without repeating the last one
     */
    public void askQuestion() {
        Random ran = new Random();
        Integer thisNumber;
        do {
            thisNumber = ran.nextInt(questions.size());
        } while (thisNumber.equals(lastQuestion));
        lastQuestion = thisNumber;
        currentQuestion = questions.get(thisNumber);

        sendPlayers(MessageFormat.format(messages.getString("quiz.question.question"), currentQuestion.getQuestion()));
        state = QuizState.GETANSWER;
    }

    /**
     * Checks chat during a quiz for correct answers from players
     *
     * @param player  Player who sent the chat message
     * @param message Chat message
     */
    public void checkAnswer(Player player, String message) {
        for (String a : currentQuestion.getAnswer()) {
            if (message.equalsIgnoreCase(a)) {
                this.getLogger().log(Level.INFO,MessageFormat.format("Question answered by {0}.",player.getName()));
                getServer().getScheduler().cancelTask(questionRunnable.getID());
                sendPlayers(MessageFormat.format(messages.getString("quiz.question.playercorrect"), ChatColor.GREEN, player.getName()));
                scores.put(player, scores.get(player) + 1);
                if (scores.get(player) == config.getInt("quiz.winningScore")) { //NON-NLS
                    this.getLogger().log(Level.INFO,"Winning score reached. Quiz stopping.");
                    state = QuizState.FINISHED;
                    finishQuiz();
                } else {
                    this.getLogger().log(Level.INFO,"Starting next question runnable.");
                    state = QuizState.WAITFORNEXT;
                    sendPlayers(messages.getString("quiz.question.next"));
                    waitForNextRunnable.start();
                }
                break;
            }
        }
    }

    /**
     * Finish a quiz and display the final scores
     */
    void finishQuiz() {
        if (scores.size() != 0) {
            Map<Player, Integer> sortedScores = Util.sortScores(scores);
            sendPlayers("---------- " + messages.getString("quiz.scores.final") + " ----------");
            for (Map.Entry<Player, Integer> score : sortedScores.entrySet()) {
                sendPlayers(MessageFormat.format(messages.getString("quiz.scores.points"), score.getKey().getName(), score.getValue()));
            }
            scores = new HashMap<Player, Integer>();
            isRunning = false;
        }
    }

    /**
     * Display scores to a player on request
     *
     * @param s CommandSender requesting score
     */
    public void displayScores(CommandSender s) {
        if (scores.size() != 0) {
            Map<Player, Integer> sortedScores = Util.sortScores(scores);
            s.sendMessage(prefixMessage("---------- " + messages.getString("quiz.scores.interim") + " ----------"));
            for (Map.Entry<Player, Integer> score : sortedScores.entrySet()) {
                s.sendMessage(prefixMessage(MessageFormat.format(messages.getString("quiz.scores.points"), score.getKey().getName(), score.getValue())));
            }
        } else {
            s.sendMessage(prefixMessage(messages.getString("error.noscores")));
        }
    }

    /**
     * Generate a hint string for the current question
     *
     * @param p Percentage of answer to hide
     * @return Answer with percentage of answer hidden by '*'
     * @throws IllegalArgumentException
     */
    public String getHint(Integer p) throws IllegalArgumentException {

        // If more than 100 we get a result that won't work
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

    public void sendPlayers(String message) {

        for (Player p : scores.keySet()) {
            p.sendMessage(prefixMessage(message));
        }

    }

}

