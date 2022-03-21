package model;
import java.util.Random;
import java.util.Timer;
import java.time.*;
import java.util.TimerTask;

public class Minesweeper extends AbstractMineSweeper {

    private int height;
    private int width;
    private int countFlagged;
    private int countOpened;

    private AbstractTile[][] playingField;
    private int explosionCount; //hoeveelheid bommen er aanwezig moeten zijn
    private int countBombs; //effectief aanwizige bommen

    private LocalDateTime startTime = LocalDateTime.now();
    private Timer timer = new Timer();
    private TimerTask timerTask;

    public Minesweeper(){
    }
    public LocalDateTime getStartTime(){
        return startTime;
    }
    @Override
    public int getWidth() {
        return width;
    }

    @java.lang.Override
    public int getHeight() {
        return height;
    }

    @java.lang.Override
    public void startNewGame(Difficulty level) {

        //Start a new game on the basis of the difficulty
        switch (level){
            case EASY:
                startNewGame(8,8,10);
                break;
            case MEDIUM:
                startNewGame(16,16,40);

                break;
            case HARD:
                startNewGame(16, 30, 99);
                break;
        }
    }

    @java.lang.Override
    public void startNewGame(int row, int col, int explosionCount) {
        height = row;
        width = col;
        countOpened = 0;
        countBombs = 0;
        this.explosionCount = explosionCount;

        playingField = new AbstractTile[row][col];
        setWorld(playingField);
        this.viewNotifier.notifyNewGame(row,col);

        //printer om te checken in terminal
        for (int Nrow = 0; Nrow < height; Nrow++) {
            System.out.print('\n');
            for (int Ncol = 0; Ncol < width; Ncol++) {
                if(playingField[Nrow][Ncol].isExplosive()){
                    System.out.print(1 + " ");
                }else{
                    System.out.print(0 + " ");
                }
            }
        }
        System.out.print("\n");


        //flagAllBombs();

        startTime = LocalDateTime.now();
        timerTask = new myTimerTask(this);
        timer.schedule(timerTask, 0, 1000);
    }

    @java.lang.Override
    public void toggleFlag(int x, int y) {
        boolean isFlagged = getTile(x, y).isFlagged();
        if(isFlagged){
            unflag(x, y);
        }
        else {
            flag(x, y);
        }
    }

    @java.lang.Override
    public AbstractTile getTile(int x, int y) {
        return playingField[y][x];
    }

    @java.lang.Override
    public void setWorld(AbstractTile[][] world) {
        Random rand = new Random(); // maak een gigantisch random nummer aan
        countBombs = 0; //effectief aanwizige bommen

        //create the playing field
        for (int row = 0; row < getHeight(); row++) {
            for (int col = 0; col < getWidth(); col++) {
                int randNum = rand.nextInt(64); // kies een nummer tussen 0-64 van rand
                if (randNum <= 10 && countBombs < explosionCount) {
                    world[row][col] = generateExplosiveTile();
                    countBombs++;
                } else {
                    world[row][col] = generateEmptyTile();
                }
            }
        }
        //if there are too few bombs create new bombs
        if(countBombs < explosionCount){
            while(countBombs < explosionCount) {
                int randNumForHeight = rand.nextInt(getHeight());
                int randNumForWidth = rand.nextInt(getWidth());
                if(!world[randNumForHeight][randNumForWidth].isExplosive()){
                    world[randNumForHeight][randNumForWidth] = generateExplosiveTile();
                    countBombs++;
                }
            }
        }
        //playingField = world;

    }

    @java.lang.Override
    public void open(int x, int y) {
        if(getTile(x, y).isFlagged()){
            unflag(x, y);
        }

        if(getTile(x, y).isExplosive()){
            if(countFlagged == 0 && countOpened == 0){
                firstRule(playingField);
                playingField[y][x] = generateEmptyTile();


                getTile(x, y).open();
                this.viewNotifier.notifyOpened(x, y, getExplosionCountNeighbours(x, y));
                countOpened++;
            }
            else{
                this.viewNotifier.notifyExploded(x, y);
<<<<<<< HEAD
                timerTask.cancel();
                //timer.purge();
                this.viewNotifier.notifyGameLost();
=======
>>>>>>> c3c729a (v4 Maxime)
                revealAllBombs();
                this.viewNotifier.notifyGameLost();
            }
        }
        else {
            getTile(x, y).open();
            this.viewNotifier.notifyOpened(x, y, getExplosionCountNeighbours(x, y));
            countOpened++;
            //open all tiles around with 0 bombs around it
            if (getExplosionCountNeighbours(x,y) == 0){
                for (int row = x - 1; row < x + 2; row++) {
                    for (int col = y - 1; col < y + 2; col++) {
                        try{
                            if (!getTile(row,col).isOpened()){
                                open(row,col);
                                countOpened++;
                            }
                        }
                        catch (Exception e){

                        }

                    }
                }
            }

            //System.out.println("Tile (" + x + "," + y + ") opened." + "explosives around: " + getExplosionCountNeighbours(x, y));
        }
        //System.out.println(countFlagged);
        //run checkWon if amount of Flagged equals the amount of bombs
        if(countFlagged == explosionCount){
            checkWon();
        }
    }

    @java.lang.Override
    public void flag(int x, int y) {
        if(!getTile(x,y).isOpened()){
            getTile(x, y).flag();
            this.viewNotifier.notifyFlagged(x, y);
            countFlagged++;
            this.viewNotifier.notifyFlagCountChanged(countFlagged);
        }
    }

    @java.lang.Override
    public void unflag(int x, int y) {
        getTile(x, y).unflag();
        this.viewNotifier.notifyUnflagged(x, y);
        countFlagged--;
        this.viewNotifier.notifyFlagCountChanged(countFlagged);

    }

    @java.lang.Override // the first tile rule = the first tile you click can never be a bomb
    public void deactivateFirstTileRule() {

    }

    @java.lang.Override
    public Tile generateEmptyTile() {
        return new Tile();
    }

    @java.lang.Override
    public Tile generateExplosiveTile() {
        Tile tile = new Tile();
        tile.setExplosive();
        return tile;
    }

    public int getExplosionCountNeighbours(int x, int y) {
        int TempExplosionCountNeighbours = 0;

        for(int row = x-1; row < x + 2; row++ ){
            for(int col = y-1; col < y + 2; col++){
                try {
                    boolean temp;
                    temp = getTile(row, col).isExplosive();
                    if(temp){
                        TempExplosionCountNeighbours++;
                    }
                } catch (Exception ArrayIndexOutOfBoundsException) {
                    //ArrayIndexOutOfBoundsException.printStackTrace();

                }
            }

        }
        return TempExplosionCountNeighbours;
    }

    public void checkWon(){
        System.out.println("run game won");
        int tempVal = height*width-explosionCount;
        int tempCountOpend = 0;
        if (countFlagged == explosionCount) {
            for (int row = 0; row < getHeight(); row++) {
                for (int col = 0; col < getWidth(); col++) {
                    if(getTile(row, col).isOpened()){
                        tempCountOpend++;
                    }
                }
            }
        }
        if(tempCountOpend == tempVal){
            this.viewNotifier.notifyGameWon();
            timerTask.cancel();
            //timer.purge();
        }

    }

    public void revealAllBombs(){
        for (int row = 0; row < getHeight(); row++) {
            for (int col = 0; col < getWidth(); col++) {
                if(getTile(col, row).isExplosive()){
                    this.viewNotifier.notifyExploded(col, row);
                }
            }
        }
    }

    public void firstRule(AbstractTile[][] world){
        Random rand = new Random(); // maak een gigantisch random nummer aan
        boolean check = true;
        while(check) {
            int randNumForHeight = rand.nextInt(getHeight());
            int randNumForWidth = rand.nextInt(getWidth());
            if(!getTile(randNumForWidth, randNumForHeight).isExplosive()) {
                world[randNumForHeight][randNumForWidth] = generateExplosiveTile();
                check = false;
            }
        }
    }

    public void flagAllBombs(){
        for (int row = 0; row < getHeight(); row++) {
            for (int col = 0; col < getWidth(); col++) {
                if (getTile(row, col).isExplosive()) {
                    getTile(row, col).flag();
                    this.viewNotifier.notifyFlagged(row, col);
                    countFlagged++;
                }
            }
        }
    }
}

