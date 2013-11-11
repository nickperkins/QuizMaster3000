QuizMaster3000
==============

Quiz Plugin for Bukkit servers

This plugin allows you to run quiz contests in game. Questions are loaded from a data file that can be updated with as many questions as you can think of. Questions can have multiple answers.

A default set of questions are provided and saved to the plugin's folder when first run. New questions can be added by editing the file and reloading the plugin.

How It Works
------------
A quiz can be started using the in-game command. During the registration period people can join the quiz. Once the registration period is finished, questions are asked until one player gets five right. They are then declared the winner. 

Commands
--------
### Player Commands

* /quiz join - Join a quiz that has started and is accepting registrations.

### Admin Commands

* /quizadmin start - Start a new quiz and accept registrations.
* /quizadmin stop - Stop a quiz at any point during the quiz.
* /quizadmin autorun -  Run a quiz, then repeat after a delay (set in config.yml)

Permissions
-----------
* quizmaster3000.quiz - Access to /quiz
* quizmaster3000.admin - Access to /quizadmin

Configuration
-------------

Configuration is pretty self-explainitory

* Color codes can be used in the prefix
* autorun delay is in seconds

Question file format
--------------------

The question file is a bar (|) delimited file. The first field is the question and any other fields are answers. At least one answer is required.

For examples, look at the questions file (questions.dat) provided with the plugin.

Changelog
---------
**Version v0.2**
* Add configuration options - see config.yml (will generate on first run)
* Autorun quizes every x seconds using /quizadmin autorun (defined in config.yml - default 5 minutes)

**Version v0.1.2**
* Fixed a bug where quiz threads would run after plugin was disabled or reloaded
* Quizes stop running if no players have joined or if all quiz players leave

**Version v0.1.1**
* Initial version

ToDo
----

This is very much a work in progress. Some features to come include:
* ~~Configurable prefix.~~ Added v0.2
* Configure the time periods for registration, answering questions, etc.
* Use in-game scoreboards to display the scores.
* Use multiple question files, with the questions selected when a quiz is started.
* Proper permissions nodes
