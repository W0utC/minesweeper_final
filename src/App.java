import model.Difficulty;
import model.Minesweeper;
import model.PlayableMinesweeper;
import view.MinesweeperView;

public class App {
    public static void main(String[] args) throws Exception {
        MinesweeperView view = new MinesweeperView();

        //Uncomment the lines below once your game model code is ready; don't forget to import your game model 
        PlayableMinesweeper model = new Minesweeper();
        
        
        /**
            Your code to bind your game model to the game user interface
        */
        
        view.setGameModel(model);
        model.startNewGame(Difficulty.EASY);

    }
}
