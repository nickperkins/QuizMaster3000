package net.nperkins.quizmaster3000;

import java.util.concurrent.Callable;

class CallableSendMessage implements Callable<Object> {


    private final QuizMaster3000 plugin;
    private final String message;


    CallableSendMessage(QuizMaster3000 pl, String m) {

        plugin = pl;
        message = m;
    }


    @Override
    public Object call() throws Exception {
        plugin.sendPlayers(message);
        return null;
    }
}

