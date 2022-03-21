package model;
import java.util.Random;

//TODO voor ineens verschillende lege vakjes te clearen gebruik:
//  public void clearBlanks(int i, int j) {
//        if (i==-1 || i==TILE_COLUMNS || j==-1 || j==TILE_ROWS  || getTileAt(i,j).isRaised()==false)
//            return;
//        if (Mines [i][j]!=' ') {
//            PressTile (getTileAt(i,j), false);
//            return;
//        }
//        PressTile (getTileAt(i,j), false);
//        for (int p=-1;p<=1;p++) {
//            for (int q=-1;q<=1;q++) {
//                clearBlanks (i+p,j+q);
//            }
//        }
//    }

public class Minesweeper extends AbstractMineSweeper {

    private int height;
    private int width;
    private int countFlagged;
    private int countOpened;

    private AbstractTile[][] playingField;
    private int explosionCount; //hoeveelheid bommen er aanwezig moeten zijn
    private int countBombs; //effectief aanwizige bommen

    public Minesweeper(){
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
    public void startNewGame(Difficulty level) { //TODO implement time elapsed panel
                                                //TODO implement count of flagged
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

        this.viewNotifier.notifyNewGame(row,col);
        playingField = new AbstractTile[row][col];

        setWorld(playingField);

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
        //System.out.print("\n");
        //System.out.println("aantal bommen aanwezig: " + countBombs);

        //flagAllBombs();
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
        return playingField[y][x]; //TODO hard mode doesn't work, index out of bound. because x and Y changed
                                    //At this moment the program makes a 30x16 instead of a 16x30
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

    }

    @java.lang.Override
    public void open(int x, int y) {
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
                this.viewNotifier.notifyGameLost();
                revealAllBombs();
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
        getTile(x, y).flag();
        this.viewNotifier.notifyFlagged(x, y);
        countFlagged++;
        this.viewNotifier.notifyFlagCountChanged(countFlagged);
        //System.out.println("Tile (" + x + "," + y + ") flagged.");
    }

    @java.lang.Override
    public void unflag(int x, int y) {
        getTile(x, y).unflag();
        this.viewNotifier.notifyUnflagged(x, y);
        countFlagged--;
        this.viewNotifier.notifyFlagCountChanged(countFlagged);
        //System.out.println("Tile (" + x + "," + y + ") unflagged.");
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
        /*if(countOpened == tempVal){
            this.viewNotifier.notifyGameWon();
        }*/
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
        }

    }

    public void revealAllBombs(){
        for (int row = 0; row < getHeight(); row++) {
            for (int col = 0; col < getWidth(); col++) {
                if(getTile(row, col).isExplosive()){
                    this.viewNotifier.notifyExploded(row, col);
                }
            }
        }
    }

    public void firstRule(AbstractTile[][] world){
        Random rand = new Random(); // maak een gigantisch random nummer aan

        int randNumForHeight = rand.nextInt(getHeight());
        int randNumForWidth = rand.nextInt(getWidth());
        world[randNumForHeight][randNumForWidth] = generateExplosiveTile();

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
