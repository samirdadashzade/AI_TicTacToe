import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

public class Game {
    public boolean IsFinished = false;
    public byte[][] GameState;
    public byte nextPlayer = 0;
    public int targetLength = 0;


    public Game(int number, int trgtLength,byte nextPlayer)
    {
        GameState = new byte[number][number];
        this.targetLength = trgtLength;
//        printBoard(GameState);
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

    public Action GetBestMove(int trgtLength)
    {
        this.targetLength = trgtLength;
        int n = this.GameState.length;

        int depth = 7;//n > 4 ? 7 : 7;
        ActionResult bestMove = this.Minimax(this.GameState, depth, Integer.MIN_VALUE, Integer.MAX_VALUE,false);
        return bestMove.Action;
    }

    public boolean isMovesLeft(byte[][] state, int stateSize)
    {
        for (int i = 0; i < stateSize; i++) {
            for (int j = 0; j < stateSize; j++) {
                if(state[i][j]==0) return true;
            }
        }
        return false;
    }


    public byte[][] Result(byte[][] state, byte player, Action action) {
        if(action != null)
        {
            state[action.x][action.y] = player;
        }

        return state;
    }
/*
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
*/
    // check if somebody wins
    public byte Utility(byte[][]  board){
        // Explanation of possible values:
        // N -> null, namely nothing fancy
        // D -> Draw or tie.
        // X -> X wins
        // O -> O wins
        int startElmnt = 0;
        int boardLength = board.length;
        // this value will detect if all values of cells were filled
        // with sign of X or O
        // if it is false at the end of function
        // that means it is draw
        boolean fIn = false;

        for (int i = 0; i < boardLength; i++) {
            startElmnt = 0;
            for (int j = 0; j < boardLength; j++) {
                // if the current cell is empty then just move to
                // next cell
                if(board[i][j] == 0){
                    fIn = true;
                    continue;
                }

                // checking row (right) move
                // if start_elem is X then all
                // row elements right or left should be X to win
                startElmnt = board[i][j];
                // if enough space remainings from current to the
                // end of board (in row order)

                if(j+targetLength <= boardLength){
                    boolean f = true;
                    for (int k = j+1; k < j + targetLength; k++) {
                        // there is different sign in row from start_elem value
//                        System.out.println(startElmnt+ " <++> "+boardMatrix.get(i).get(k));
                        if(board[i][k] != startElmnt){
                            f = false;
                            break;
                        }
                    }

                    // startElmnt won!
                    if(f){
//                        printMatrix();
//                        System.out.println("ROW WIN");
                        // returning winner player
                        return (byte)startElmnt;
                    }

                }

                // if enough space remainings from current to the
                // end of board (in column order)

                if(i + targetLength <= boardLength){
                    boolean f = true;

                    for (int k = i+1; k < i+targetLength; k++) {
                        // there is different sign in row from start_elem value
                        if(board[k][j]!=startElmnt){
                            f = false;

                            break;
                        }
                    }

                    // startElmnt won!
                    if(f){
//                        System.out.println("COLUMN WIN");
                        // returning winner player
                        return (byte)startElmnt;
                    }
                }

                // check diagonal through right
                if(i + targetLength-1 < boardLength && j + targetLength - 1 < boardLength){
                    int tmpI = i+1;
                    int tmpJ = j+1;
                    boolean f = true;
                    for (int k = 0; k < targetLength - 1; k++) {
                        if(board[tmpI][tmpJ] != startElmnt){
                            f = false;
                            break;
                        }
                        tmpI++;
                        tmpJ++;
                    }

                    if(f){
//                        System.out.println("DIAGONAL_RIGHT_WIN");
                        // returning winner player
                        return (byte)startElmnt;
                    }
                }

                // check diagonal through left /
                if(i + targetLength - 1 < boardLength &&
                   j - (targetLength - 1) >= 0 &&
                   j - (targetLength - 1) < boardLength
                ){
                    int tmpI = i+1;
                    int tmpJ = j-1;
                    boolean f = true;
                    for (int k = 0; k < targetLength - 1; k++) {
                        if(board[tmpI][tmpJ] != startElmnt){
                            f = false;

                            break;
                        }
                        tmpI++;
                        tmpJ--;
                    }

                    if(f){
//                        System.out.println("DIAGONAL_LEFT_WIN");
                        // returning winner player
                        return (byte)startElmnt;
                    }
                }

            }
        }
        // draw
//        if(fIn==false)
//            return 0;

        return  (byte)0;
    }

    public Action[] Actions(byte[][] state) {
        int n = state.length;
        Action[] actions = new Action[n * n];
        int actionIndex = 0;

        for(int i = n/2; i < n; i++) {
            for(int j = n/2; j < n; j++) {
                if(state[i][j] == 0)
                    actions[actionIndex++] = new Action(i, j);
            }
            for(int j = 0; j < n/2; j++) {
                if(state[i][j] == 0)
                    actions[actionIndex++] = new Action(i, j);
            }
        }
        for(int i = 0; i < n/2; i++) {
            for(int j = n/2; j < n; j++) {
                if(state[i][j] == 0)
                    actions[actionIndex++] = new Action(i, j);
            }
            for(int j = 0; j < n/2; j++) {
                if(state[i][j] == 0)
                    actions[actionIndex++] = new Action(i, j);
            }
        }


        return actions;
    }
    // checkout this function again
    public boolean Terminal(byte[][] state) {
        int util = this.Utility(state);
        if(this.Actions(state).length == 0 || util != 0 || !this.isMovesLeft(state, state.length)) {
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

        if(this.GameState[x][y] == 0) {
            this.GameState[x][y] = player;
            this.ToggleNextPlayer();
        }

        if(this.Terminal(this.GameState)) {
            this.IsFinished = true;
            int util = this.Utility(this.GameState);
            System.out.println("FINISHED " + util);

            if(util == 1) {
                System.out.println("WE WON");
            } else if(util == -1) {
                System.out.println("WE LOST");
            } else {
                System.out.println("DRAW");
            }
        }
    }

    public ActionResult Minimax(byte[][] state, int depth, int alpha, int beta, boolean maxPlayer) {
        // checks if it is end of depth,
        // or terminal state
        if(depth == 0 || this.Terminal(state))
        {
            // if it is terminal state or end of depth then simply return default values.
            return new ActionResult(this.Utility(state), new Action(-1, -1));
        }

        // check if maximizing player
        if(maxPlayer)
        {
            int v = Integer.MIN_VALUE;
            // get all possible actions list of state
            Action[] actionsList = this.Actions(state);
            // create default maximum
            ActionResult maxAction = new ActionResult(Integer.MIN_VALUE, new Action(-1, -1));
            // loop through all actions
            // and check all one by one on state
            for (Action action : actionsList)
            {
                byte[][] copyState =  this.CopyState(state);
                // create new state after adding current action to state
                // and put 1 as the player, because it is the turn of maximizing player
                byte[][] newState = this.Result(copyState,(byte)1,action);

                // call minimax recursively and decrease depth of tree,
                // now it is turn of minimizing
                ActionResult r = this.Minimax(newState,depth-1,alpha,beta,false);
                // Find the maximum between utility of other moves and current move
                v = Math.max(v, r.Utility);
                // check the value of alpha
                alpha = Math.max(alpha, v);
// alpha-beta pruning was here
                if(v > maxAction.Utility) {
                    maxAction.Utility = v;
                    maxAction.Action = action;
                }
// changed the places of alpha-beta pruning
                if(alpha >= beta) {
//                    System.out.println("AB: " + alpha + " " + beta);
                    break;
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
                beta = Math.min(beta, v);
// alpha-beta pruning was here
                if(v < minAction.Utility) {
                    minAction.Utility = v;
                    minAction.Action = action;
                }
// changed the places of alpha-beta pruning
                if(alpha >= beta) {
//                    System.out.println("AB: " + alpha + " " + beta);
                    break;
                }
            }

            return minAction;
        }
    }

    public static Game FromState(byte[][] gameState, int trgtLength) {
        int n = gameState.length;

        Game game = new Game(n, trgtLength, (byte) -1);

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
        for (int i = 0; i < boardSize; i++)
        {
            if(i<10)
                System.out.print(i + " | ");
            else
                System.out.print(i + "| ");
        }

        System.out.println();

        for (int i = 0; i < boardSize; i++)
        {
            if(i<10)
                System.out.print(i+" | ");
            else
                System.out.print(i+"| ");

            for (int j = 0; j < boardSize; j++)
            {
                System.out.print(board[i][j]);
                System.out.print(board[i][j] == -1 ? "| " : " | ");
            }
            System.out.println();
        }
    }
}
