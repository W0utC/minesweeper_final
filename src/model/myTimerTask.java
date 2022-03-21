package model;

import java.time.Duration;
import java.time.LocalTime;
import java.util.TimerTask;

public  class myTimerTask extends TimerTask
        implements Runnable {

    private final Minesweeper minesweeper;

    public myTimerTask(Minesweeper minesweeper){
        this.minesweeper = minesweeper;
    }
    @Override
    public void run() {
        minesweeper.viewNotifier.notifyTimeElapsedChanged(Duration.between(LocalTime.now(), minesweeper.getStartTime()));
        //minesweeper.viewNotifier.notifyTimeElapsedChanged(Duration.between(minesweeper.getStartTime(), LocalTime.now()));
    }
}
