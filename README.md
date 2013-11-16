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

```
general:
  prefix: '&d[Quiz]&f' # can contain color codes
  locale: 'en_AU' # defaults to Australian English
quiz:
  winningScore: 5 # when score reached quiz is ended
  hints: true # show hints during question countdown
  autorun:
    default: false # whether to autorun quiz (/quiz autorun) when using /quiz start
    delay: 300 # in seconds
```

Localisation (from v0.4.0)
--------------------------
Localisation is now possible, however the plugin contains no translations as yet. If you are willing to help translate it, please leave me a message.

If you do not set a locale in the config.yml it will use the system's default locale. If the locale doesn't have a translation it will default to Australian English.

Set your locale via the general.prefix setting in config.yml. For example:

```
# For German
general:
  locale: 'de_DE'
```

Question file format
--------------------

The question file is a bar (|) delimited file. The first field is the question and any other fields are answers. At least one answer is required.

For examples, look at the questions file (questions.dat) provided with the plugin.

