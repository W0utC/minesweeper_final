package test.gamemodel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;

import model.Minesweeper;
import org.junit.Before;
import org.junit.Test;

import model.AbstractTile;
//import model.Minesweeper;
import notifier.IGameStateNotifier;
import notifier.ITileStateNotifier;
import test.TestableMinesweeper;
import test.TestableTile;

public class GameModelTester {
    private static final int X=0, Y=1;
    private TestableMinesweeper gameModel;

    @Before
    public void init() {
        //uncomment the line below once your game model code is ready for testing
        gameModel = new Minesweeper();
    }

    @Test //test 1 OK
    public void testGeneratingEmptyTile() {
        TestableTile tile = gameModel.generateEmptyTile();
        assertNotNull(tile);

        tile.setTileNotifier(new MockTileStateNotifier() {
            @Override
            public void notifyOpened(int explosiveNeighbourCount) {
                assertTrue(explosiveNeighbourCount >= 0);
                super.setInvoked();
            }
        });
        tile.open();
    }

    @Test //test 2 OK
    public void testGeneratingExplosiveTile() {
        TestableTile tile = gameModel.generateExplosiveTile(); // orginele code TestableTile tile = gameModel.generateEmptyTile();
        assertNotNull(tile);

        tile.setTileNotifier(new MockTileStateNotifier() {
            @Override
            public void notifyExplode() {
                assertTrue(true);
                super.setInvoked();
            }
        });
        tile.open();
    }


    @Test //test 3
    public void testInitializingNewGame() {
        final int h = 5, w = 3, totalExplosion = 4;
        gameModel.setGameStateNotifier(new MockGameStateNotifier() {
            @Override
            public void notifyNewGame(int row, int col) {
                assertEquals(row, h);
                assertEquals(col, w);
                super.setInvoked();
            }           
        });
        gameModel.startNewGame(h, w, 4);
        assertEquals(gameModel.getHeight(), h);
        assertEquals(gameModel.getWidth(), w);
        int explosionCount = 0;

        assertThrows(Exception.class, () -> {
            for (int i=0; i<h; ++i)
                for (int j=0; j<w; ++j) 
                    try {
                        TestableTile temp = gameModel.getTile(j, i);
                        if (temp == null) throw new Exception();
                    }catch (Exception e) {
                        e.printStackTrace();
                        return;
                    }
            throw new Exception();
        });

        for (int i=0; i<h; ++i)
            for (int j=0; j<w; ++j) {
                TestableTile temp = gameModel.getTile(j, i);
                explosionCount += (temp.isExplosive())? 1 : 0;
            }  
        assertEquals(explosionCount, totalExplosion);

        for (int i=0; i<h; ++i)
            for (int j=0; j<w; ++j)
                assertNotNull(gameModel.getTile(j, i));

        for (int i=0; i<h; ++i)
            for (int j=0; j<w; ++j)
                assertTrue(gameModel.getTile(j, i) instanceof TestableTile);

    }

    @Test //test 4
    public void testInjectingTiles() {
        int row=3, col=4;
        AbstractTile world[][] = new AbstractTile[row][col];
        for (int i=0; i<row; ++i)
            for (int j=0; j<col; ++j)
                world[i][j] = ((i+j)%2 == 0)? gameModel.generateEmptyTile() : gameModel.generateExplosiveTile();
        

        gameModel.setGameStateNotifier(new MockGameStateNotifier(){
            @Override
            public void notifyNewGame(int row, int col) {
                assertEquals(row, world.length);
                assertEquals(col, world[0].length);
                super.setInvoked();
            }
        });
        gameModel.setWorld(world);
        for (int i=0; i<row; ++i)
            for (int j=0; j<col; ++j)
                assertEquals(gameModel.getTile(j, i), world[i][j]);
    }

    @Test //test 5
    public void testFlagTile() {
        int size = 3;
        Deque<int[]> testQueue = new ArrayDeque<>();
        Deque<int[]> expectedResultQueue = new ArrayDeque<>();

        gameModel.setGameStateNotifier(new MockGameStateNotifier(){
            private int flagCounter = 0;
            @Override
            public void notifyNewGame(int row, int col) {
                assertEquals(row, size);
                assertEquals(col, size);
                super.setInvoked();
            }
            @Override
            public void notifyFlagCountChanged(int newFlagCount) {
                assertEquals(newFlagCount, flagCounter);
                super.setInvoked();
            }
            @Override
            public void notifyFlagged(int x, int y) {
                int[] expected = expectedResultQueue.pop();
                assertEquals(x, expected[X]);
                assertEquals(y, expected[Y]);
                ++flagCounter;
                super.setInvoked();
            }

        });
        gameModel.startNewGame(size, size, 1);
        testQueue.push(new int[]{1, 1});
        testQueue.push(new int[]{2, 2});

        gameModel.startNewGame(size, size, 1);
        while(!testQueue.isEmpty()) {
            int[] currentFlagCoord = testQueue.pop();
            expectedResultQueue.push(currentFlagCoord);
            gameModel.flag(currentFlagCoord[X], currentFlagCoord[Y]);
            assertTrue(gameModel.getTile(currentFlagCoord[X], currentFlagCoord[Y]).isFlagged());
        }
    }

    @Test //test 6
    public void testUnflagTile() {
        int size = 3;
        Deque<int[]> testQueue = new ArrayDeque<>();
        Deque<int[]> expectedResultQueue = new ArrayDeque<>();

        gameModel.setGameStateNotifier(new MockGameStateNotifier(){
            private int flagCounter = 0;
            @Override
            public void notifyNewGame(int row, int col) {
                assertEquals(row, size);
                assertEquals(col, size);
                super.setInvoked();
            }
            @Override
            public void notifyFlagCountChanged(int newFlagCount) {
                assertEquals(newFlagCount, flagCounter);
                super.setInvoked();
            }
            @Override
            public void notifyFlagged(int x, int y) {
                ++flagCounter;
                super.setInvoked();
            }
            @Override
            public void notifyUnflagged(int x, int y) {
                int[] expected = expectedResultQueue.pop();
                assertEquals(x, expected[X]);
                assertEquals(y, expected[Y]);
                --flagCounter;
                super.setInvoked();
            }
            
        });

        testQueue.push(new int[]{1, 1});
        testQueue.push(new int[]{2, 2});

        gameModel.startNewGame(size, size, 1);
        while(!testQueue.isEmpty()) {
            int[] currentFlagCoord = testQueue.pop();
            expectedResultQueue.push(currentFlagCoord);
            gameModel.flag(currentFlagCoord[X], currentFlagCoord[Y]);
            assertTrue(gameModel.getTile(currentFlagCoord[X], currentFlagCoord[Y]).isFlagged());
        }

        testQueue.push(new int[]{1, 1});
        testQueue.push(new int[]{2, 2});
        while(!testQueue.isEmpty()) {
            int[] currentFlagCoord = testQueue.pop();
            expectedResultQueue.push(currentFlagCoord);
            gameModel.unflag(currentFlagCoord[X], currentFlagCoord[Y]);
            assertTrue(!gameModel.getTile(currentFlagCoord[X], currentFlagCoord[Y]).isFlagged());
        }

    }

    @Test //test 7
    public void testTogglingTile() {
        int size = 3;
        int[] target = new int[] {1,1};
        gameModel.setGameStateNotifier(new MockGameStateNotifier() {
            @Override
            public void notifyNewGame(int row, int col) {
                assertEquals(row, size);
                assertEquals(col, size);
                super.setInvoked();
            }

            @Override
            public void notifyFlagCountChanged(int newFlagCount) {
                assertEquals(newFlagCount, testStepCounter);
                super.setInvoked();
            }

            int testStepCounter = 0;
            @Override
            public void notifyFlagged(int x, int y) {
                assertEquals(x, target[0]);
                assertEquals(y, target[1]);
                ++testStepCounter;
                super.setInvoked();
            }

            @Override
            public void notifyUnflagged(int x, int y) {
                assertEquals(x, target[0]);
                assertEquals(y, target[1]);
                --testStepCounter;
                super.setInvoked();
            }
            
        });
        
        gameModel.startNewGame(size, size, 1);
        gameModel.toggleFlag(target[0], target[1]);
        assertTrue(gameModel.getTile(target[0], target[1]).isFlagged());

        gameModel.toggleFlag(target[0], target[1]);
        assertTrue(!gameModel.getTile(target[0], target[1]).isFlagged());
    }

    @Test //test 8
    public void testOpeningEmptyTileWithExplosiveNeighbours() {
        int target[] = {1, 1};
        final int explosionCount = 4;
        AbstractTile[][] world = new AbstractTile[][] {
            {gameModel.generateEmptyTile(), gameModel.generateExplosiveTile(), gameModel.generateEmptyTile()}, 
            {gameModel.generateExplosiveTile(), gameModel.generateEmptyTile(), gameModel.generateExplosiveTile()},
            {gameModel.generateEmptyTile(), gameModel.generateExplosiveTile(), gameModel.generateEmptyTile()}, 
        };
        gameModel.setGameStateNotifier(new MockGameStateNotifier() {
            @Override
            public void notifyNewGame(int row, int col) {
                assertEquals(row, world.length);
                assertEquals(col, world[0].length);
                super.setInvoked();
            }
            @Override
            public void notifyOpened(int x, int y, int explosiveNeighbourCount) {
                assertEquals(x, target[0]);
                assertEquals(y, target[1]);
                assertEquals(explosiveNeighbourCount, explosionCount);
            }
        });
        gameModel.setWorld(world);
        gameModel.open(target[X], target[Y]);
    }

    @Test //test 9
    public void testOpeningEmptyTileWithoutExplosiveNeighbours() {
        AbstractTile[][] world = new AbstractTile[][] {
            {gameModel.generateExplosiveTile(), gameModel.generateEmptyTile(), gameModel.generateEmptyTile(), gameModel.generateEmptyTile(), gameModel.generateExplosiveTile()},
            {gameModel.generateEmptyTile(), gameModel.generateEmptyTile(), gameModel.generateEmptyTile(), gameModel.generateEmptyTile(),  gameModel.generateEmptyTile()}, 
            {gameModel.generateEmptyTile(), gameModel.generateEmptyTile(), gameModel.generateEmptyTile(), gameModel.generateEmptyTile(),  gameModel.generateEmptyTile()}, 
            {gameModel.generateEmptyTile(), gameModel.generateEmptyTile(), gameModel.generateEmptyTile(), gameModel.generateEmptyTile(),  gameModel.generateEmptyTile()}, 
            {gameModel.generateExplosiveTile(), gameModel.generateEmptyTile(), gameModel.generateEmptyTile(), gameModel.generateEmptyTile(), gameModel.generateExplosiveTile()},
        };
        final int[][] map = new int[][] {
            {-1, 1, 0, 1, -1},
            {1, 1, 0, 1, 1},
            {0, 0, 0, 0, 0},
            {1, 1, 0, 1, 1},
            {-1, 1, 0, 1, -1},
        };
        boolean[][] openedMap = new boolean[map.length][map[0].length];
        gameModel.setGameStateNotifier(new MockGameStateNotifier() {
            @Override
            public void notifyNewGame(int row, int col) {
                assertEquals(row, world.length);
                assertEquals(col, world[0].length);
                super.setInvoked();
            }
            @Override
            public void notifyOpened(int x, int y, int explosiveNeighbourCount) {
                super.setInvoked();
                openedMap[y][x] = true;
                if (explosiveNeighbourCount < 0) {
                    assertTrue(false);
                    return;
                }
                assertEquals(explosiveNeighbourCount, map[y][x]);
            }
            @Override
            public void notifyGameWon() {
                assertTrue(true);
                super.setInvoked();
            }
        });
        gameModel.setWorld(world);
        int target[] = {2, 2};
        gameModel.open(target[X], target[Y]);
        boolean isNonExplosiveTileOpened = true;
        for (int i=0; i<openedMap.length; ++i)
            for (int j=0; j<openedMap[i].length; ++j) 
                if (map[i][j] >= 0)
                    isNonExplosiveTileOpened |= openedMap[i][j];
        assertTrue(isNonExplosiveTileOpened);
    }

    @Test //test 10
    public void testOpeningExplosiveTile() {
        int target[] = {0, 1};
        AbstractTile[][] world = new AbstractTile[][] {
            {gameModel.generateEmptyTile(), gameModel.generateExplosiveTile(),}, 
            {gameModel.generateExplosiveTile(), gameModel.generateEmptyTile(),},
        };
        final int[][] map = new int[][] {
            {2, -1},
            {-1, 2},};
        boolean[][] openedMap = new boolean[map.length][map[0].length];
        gameModel.setGameStateNotifier(new MockGameStateNotifier() {
            @Override
            public void notifyNewGame(int row, int col) {
                assertEquals(row, world.length);
                assertEquals(col, world[0].length);
                super.setInvoked();
            }
            @Override
            public void notifyOpened(int x, int y, int explosiveNeighbourCount) {
                super.setInvoked();
                openedMap[y][x] = true;
                if (explosiveNeighbourCount < 0) {
                    assertTrue(false);
                    return;
                }
                assertEquals(explosiveNeighbourCount, map[y][x]);
            }
            @Override
            public void notifyExploded(int x, int y) {
                assertEquals(map[y][x], -1);
                super.setInvoked();       
            }
            @Override
            public void notifyGameLost() {
                assertTrue(true);
                super.setInvoked();
            }
        });
        gameModel.setWorld(world);
        gameModel.deactivateFirstTileRule();
        gameModel.open(target[X], target[Y]);
        boolean isAllTileOpened = true;
        for (int i=0; i<openedMap.length; ++i)
            for (int j=0; j<openedMap[i].length; ++j)
                isAllTileOpened |= openedMap[i][j];
        assertTrue(isAllTileOpened);
    }

    @Test //test 11
    public void testFlaggingOpenedTile() {
        final int size = 1;
        gameModel.setGameStateNotifier(new MockGameStateNotifier(){
            @Override
            public void notifyNewGame(int row, int col) {
                assertEquals(row, size);
                assertEquals(col, size);
                super.setInvoked();
            }
            @Override
            public void notifyOpened(int x, int y, int explosiveNeighbourCount) {
                assertEquals(x, 0);
                assertEquals(y, 0);
                assertEquals(explosiveNeighbourCount, 0);
                super.setInvoked();
            }
            @Override
            public void notifyFlagged(int x, int y) {
                assertTrue("Opened Tile can't be flagged", false);
                super.setInvoked();
            }            
        });
        gameModel.startNewGame(1, 1, 0);
        gameModel.open(0, 0);
        gameModel.flag(0, 0);
    }

    @Test //test 12
    public void testOpenningFirstTile() {
        final int size = 2;
        gameModel.setGameStateNotifier(new MockGameStateNotifier(){
            @Override
            public void notifyNewGame(int row, int col) {
                super.setInvoked();
            }
            @Override
            public void notifyOpened(int x, int y, int explosiveNeighbourCount) {
                super.setInvoked();
            }
            @Override 
            public void notifyExploded(int x, int y) {
                assertTrue("The first opened tile shouldn't be an explosive tile", false);
            }
        });
        gameModel.startNewGame(size, size, 1);
        boolean firstExplosiveTileOpened = false;
        for (int i=0; i<size && !firstExplosiveTileOpened; ++i)
            for (int j=0; j<size && !firstExplosiveTileOpened; ++j)
                if (gameModel.getTile(j, i).isExplosive()) {
                    gameModel.open(j, i);
                    firstExplosiveTileOpened = true;
                }
    }

    @Test //test 13
    public void testAddressingTile() {
        final int size = 2;
        gameModel.setGameStateNotifier(new MockGameStateNotifier() {
            @Override
            public void notifyNewGame(int row, int col) {
                super.setInvoked();
            }
        });
        gameModel.startNewGame(size, size, 0);
        gameModel.open(size, size);
        gameModel.getTile(size, size);
        gameModel.open(-1, -1);
        gameModel.getTile(-1, -1);
    }

    private class MockGameStateNotifier implements IGameStateNotifier{
        private int invoked = 0;
        @Override
        public void notifyNewGame(int row, int col) {
            setInvoked();
            assertTrue("This method shouldn't be invoked in this test", false);       
        }

        @Override
        public void notifyGameLost() {
            setInvoked();
            assertTrue("This method shouldn't be invoked in this test", false);       
        }

        @Override
        public void notifyGameWon() {
            setInvoked();
            assertTrue("This method shouldn't be invoked in this test", false);       
        }

        @Override
        public void notifyFlagCountChanged(int newFlagCount) {
            setInvoked();
            assertTrue("This method shouldn't be invoked in this test", false);       
        }

        @Override
        public void notifyTimeElapsedChanged(Duration newTimeLeft) {
            setInvoked();
            assertTrue("This method shouldn't be invoked in this test", false);       
        }

        @Override
        public void notifyOpened(int x, int y, int explosiveNeighbourCount) {
            setInvoked();
            assertTrue("This method shouldn't be invoked in this test", false);       
        }

        @Override
        public void notifyFlagged(int x, int y) {
            setInvoked();
            assertTrue("This method shouldn't be invoked in this test", false);       
        }

        @Override
        public void notifyUnflagged(int x, int y) {
            setInvoked();
            assertTrue("This method shouldn't be invoked in this test", false);       
        }

        @Override
        public void notifyExploded(int x, int y) {
            setInvoked();
            assertTrue("This method shouldn't be invoked in this test", false);       
        }

        public int getInvokedMethodCount() {return invoked;}
        protected void setInvoked() {invoked++;}

    }
    public class MockTileStateNotifier implements ITileStateNotifier {
        private int invoked = 0;
        @Override
        public void notifyOpened(int explosiveNeighbourCount) {
            setInvoked();
            assertTrue("This method shouldn't be invoked in this test", false);
        }

        @Override
        public void notifyFlagged() {
            setInvoked();
            assertTrue("This method shouldn't be invoked in this test", false);
        }

        @Override
        public void notifyUnflagged() {
            setInvoked();
            assertTrue("This method shouldn't be invoked in this test", false);
        }

        @Override
        public void notifyExplode() {
            setInvoked();
            assertTrue("This method shouldn't be invoked in this test", false);
        }

        public int getInvokedMethodCount() {return invoked;}
        protected void setInvoked() {invoked++;}

    }
}
