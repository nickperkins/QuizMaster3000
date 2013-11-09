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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class QuizMaster3000 extends JavaPlugin {

	private QuizMaster3000CommandExecutor commandExecutor = new QuizMaster3000CommandExecutor(this);
	private QuizMaster3000Listener listener = new QuizMaster3000Listener(this);

	public QuizThread thread = new QuizThread(this);
	volatile public QuizState state = QuizState.FINISHED;

	public HashMap<Player, Integer> scores = new HashMap<Player, Integer>();
	public ArrayList<Question> questions = new ArrayList<Question>();
	public Question currentQuestion = null;

	@Override
	public void onEnable() {

		// this.saveDefaultConfig();

		// Create data folder if required
		if (!this.getDataFolder().exists()) {
			this.getDataFolder().mkdir();
		}

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

	public void stopQuiz() {
		state = QuizState.FINISHED;
		thread.stop();
		this.scores = new HashMap<Player, Integer>();
		getServer().broadcastMessage(Util.formatMessage("%sQuiz Ended!",ChatColor.GOLD));
		
	}

	private void checkQuestions(String filepath) {

		File quizfile = new File(filepath);
		if (!quizfile.exists()) {
			Bukkit.getLogger().info(Util.formatMessage("%sQuiz data file does not exist - providing default questions.", ChatColor.GOLD));

			File file = new File("newname.ext");
			if (!file.exists()) {
				try {
					quizfile.createNewFile();
					InputStream in = (getClass().getResourceAsStream("/questions.dat"));
					FileOutputStream out = new FileOutputStream(quizfile);
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
			String[] splitline = line.split("\\|");
			Question thisline = new Question();
			thisline.setQuestion(splitline[0]);
			thisline.setAnswer((String[]) ArrayUtils.subarray(splitline, 1, splitline.length));
			questions.add(thisline);
		}
		br.close();
	}

}
