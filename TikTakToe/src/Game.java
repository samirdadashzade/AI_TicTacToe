import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

public class Game {
    public boolean IsFinished = false;
    public byte[][] GameState;
    public byte nextPlayer = 0;


    public Game(int number, byte nextPlayer)
    {
        GameState = new byte[number][number];

        printBoard(GameState);
    }

    //make value int type
    public Hashtable<String, String> ActionUtilMap;

    public void SetNextPlayer()
    {
        int n = GameState.length;

        int xCount = 0;
        int oCount = 0;

        for(int i=0; i < n; i++) {
            for(int j = 0; j < n; j++) {
                if(this.GameState[i][j] == 1)
                {
                    xCount++;
                }
                else if(this.GameState[i][j] == -1)
                {
                    oCount++;
                }
            }
        }

        if(oCount == 0 || oCount == xCount) {
            this.nextPlayer = -1;
        }
        else {
            this.nextPlayer = 1;
        }
    }

    public byte GetNextPlayer()
    {
        return this.nextPlayer;
    }

    public void ToggleNextPlayer()
    {
        this.nextPlayer = (byte)(this.nextPlayer * -1);
        //this.notify(this.nextPlayer);
    }

    public int GetN() {
        return this.GameState.length;
    }

    public byte[][] CopyState(byte[][] state)
    {
        int n = state.length;
        byte[][] newState = new byte[n][n];

        for(int i = 0; i < n; i++)
        {
            for (int j = 0; j < n; j++) {
                newState[i][j] = state[i][j];
            }
        }

        return newState;
    }

    public Action GetBestMove()
    {
        int n = this.GameState.length;
        int depth = n > 4 ? 2 : n;
        ActionResult bestMove = this.Minimax(this.GameState, depth, Integer.MIN_VALUE, Integer.MAX_VALUE,false);
        return bestMove.Action;
    }

    public byte[][] Result(byte[][] state, byte player, Action action) {
        if(action != null)
        {
            state[action.y][action.x] = player;
        }

        return state;
    }

    public byte Utility(byte[][] state)
    {
        int n = state.length;
        Set<Byte> rowSet = new HashSet<Byte>();
        Set<Byte> colSet = new HashSet<Byte>();
        Set<Byte> diag1Set = new HashSet<Byte>();
        Set<Byte> diag2Set = new HashSet<Byte>();

        for(int i = 0; i < n; i++) {
            rowSet = new HashSet<Byte>();
            colSet = new HashSet<Byte>();

            for(int j = 0; j < n; j++) {
                rowSet.add(state[i][j]);
                colSet.add(state[j][i]);

                if(i == j) {
                    diag1Set.add(state[i][j]);
                }
                if(i + j == n - 1) {
                    diag2Set.add(state[i][j]);
                }
            }

            // check rows
            if(rowSet.size() == 1) {
                return (byte)rowSet.toArray()[0];
            }

            //check cols
            if(colSet.size() == 1) {
                return (byte)colSet.toArray()[0];
            }
        }

        // check diagonals
        if(diag1Set.size() == 1) {
            return (byte)diag1Set.toArray()[0];
        }
        if(diag2Set.size() == 1) {
            return (byte)diag2Set.toArray()[0];
        }

        return 0;
    }

    public Action[] Actions(byte[][] state) {
        int n = state.length;
        Action[] actions = new Action[n * n];
        int actionIndex = 0;

        for(int i = 0; i < n; i++) {
            for(int j = 0; j < n; j++) {
                if(state[i][j] == 0)
                    actions[actionIndex++] = new Action(i, j);
            }
        }

        return actions;
    }

    public boolean Terminal(byte[][] state) {
        int util = this.Utility(state);
        if(this.Actions(state).length == 0 || util != 0) {
            return true;
        }

        return false;
    }

    public void Move(byte player, Action action) {
        if(this.IsFinished)
        {
            return;
        }

        int x = action.x;
        int y = action.y;

        if(this.GameState[y][x] == 0) {
            this.GameState[y][x] = player;
            this.ToggleNextPlayer();
        }

        if(this.Terminal(this.GameState)) {
            this.IsFinished = true;
            int util = this.Utility(this.GameState);
            System.out.println("FINISHED " + util);

            if(util == 1) {
                System.out.println("WINNER X");
            } else if(util == -1) {
                System.out.println("WINNER O");
            } else {
                System.out.println("DRAW");
            }
        }
    }

    public ActionResult Minimax(byte[][] state, int depth, int alpha, int beta, boolean maxPlayer) {
        if(depth == 0 || this.Terminal(state))
        {
            return new ActionResult(this.Utility(state), new Action(-1, -1));
        }

        if(maxPlayer)
        {
            int v = Integer.MIN_VALUE;//Integer.MAX_VALUE;
            Action[] actionsList = this.Actions(state);
            ActionResult maxAction = new ActionResult(Integer.MIN_VALUE, new Action(-1, -1));

            for (Action action : actionsList)
            {
                byte[][] copyState =  this.CopyState(state);
                byte[][] newState = this.Result(copyState,(byte)1,action);
                ActionResult r = this.Minimax(newState,depth-1,alpha,beta,false);
                v = Math.max(v, r.Utility);
                alpha = Math.max(alpha, v);
                if(alpha >= beta) {
                    System.out.println("AB: " + alpha + " " + beta);
                    break;
                }
                if(v > maxAction.Utility) {
                    maxAction.Utility = v;
                    maxAction.Action = action;
                }
            }

            return maxAction;
        }
        else {
            int v = Integer.MAX_VALUE;
            Action[] actionsList = this.Actions(state);
            ActionResult minAction = new ActionResult(Integer.MAX_VALUE, new Action(-1, -1));

            for(Action action : actionsList) {
                byte[][] copyState = this.CopyState(state);
                byte[][] newState = this.Result(copyState,(byte)-1,action);
                ActionResult r = this.Minimax(newState,depth-1,alpha,beta,true);
                v = Math.min(v, r.Utility);
                beta = Math.max(beta, v);
                if(alpha >= beta) {
                    System.out.println("AB: " + alpha + " " + beta);
                    break;
                }
                if(v < minAction.Utility) {
                    minAction.Utility = v;
                    minAction.Action = action;
                }
            }

            return minAction;
        }
    }

    public static Game FromState(byte[][] gameState) {
        int n = gameState.length;

        Game game = new Game(n, (byte) -1);

        for(int i = 0; i < n; i++) {
            for(int j = 0; j < n; j++) {
                game.GameState[i][j] = gameState[i][j];
            }
        }

        game.SetNextPlayer();
        //notify.Execute(game.GetNextPlayer());

        return game;
    }

    // print board
    public void printBoard(byte[][] board){

        int boardSize = GetN();// NxN dimension

        System.out.print("    ");
        for (int i = 0; i < boardSize; i++) {
            if(i<10)
                System.out.print(i + " | ");
            else
                System.out.print(i + "| ");
        }

        System.out.println();

        for (int i = 0; i < boardSize; i++) {

            if(i<10)
                System.out.print(i+" | ");
            else
                System.out.print(i+"| ");

            for (int j = 0; j < boardSize; j++) {
                System.out.print(board[i][j] + " | ");
            }
            System.out.println();
        }
    }
}
