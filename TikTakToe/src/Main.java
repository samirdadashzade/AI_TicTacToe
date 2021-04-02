import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Scanner;

public class Main {

    public static Gson gson = new Gson();

    public static void main(String[] args) {


        if(true){
            int boardSize = 8;
            int target = 6;
            byte currPlayer = 1;

            Game game = new Game(boardSize, target, (byte) -1);

            while(!game.Terminal(game.GameState)){
                Scanner coordPlayer = new Scanner(System.in);
                int cI = coordPlayer.nextInt();
                int cJ = coordPlayer.nextInt();
                game.Move(currPlayer, new Action(cI, cJ));

                if (game.Terminal(game.GameState)) {
                    //game ended, check who is winner
                    int utility = game.Utility(game.GameState);
                    if (utility == 1) {
                        System.out.println("WE WON");
                    } else if (utility == -1) {
                        System.out.println("WE LOST");
                    } else {
                        System.out.println("DRAW");
                    }
                    return;
                }

                System.out.println("Board before move:");
                game.printBoard(game.GameState);
                Action bestMove = game.GetBestMove(target);

                //4. Make a best move locally and change player
                game.Move((byte)-1, bestMove);
                System.out.println("Board after move:");
                game.printBoard(game.GameState);
            }


        }else {
            int teamId1 = 1243; //we "X" now
            int teamId2 = 1244;//1250;//;1244
            int boardSize = 8;
            int target = 6;
            int gameId = 2799;

            String mine = teamId1 != 1243 ? "O" : "X";
            byte currPlayer = -1;

            int recentCount = boardSize * boardSize;
            int opponentWaitTimeoutSecond = 60; //1 minute
            int opponentWaitTimeoutRetry = 10;
            int myRecentMoveId = 0;


            //1. Create a game local (for having a Game instance)
            //2. Create a game remote
            //3. While game is not in Terminal state continue line 4, if Terminal continue line 8
            //4. Make a best move locally and change player
            //5. Send your move online
            //6. Get the new board state and make a move on behalf of the opponent
            //7. Continue
            //8. Show winner/loser/draw and finish game

            //1. Create a game local (for having a Game instance)
            Game game = new Game(boardSize, target, (byte) 1);

            //2. Create a game remote
            //BaseResponse response = ApiHelper.CreateGame(teamId1, teamId2, boardSize, target);
            //gameId = ParseGameId(response);

            BaseResponse response = ApiHelper.GetMyMoves(gameId, recentCount);

            game = game.FromState(ParseGameBoard(response, boardSize, mine), target);

            //3. While game is not in Terminal state
            while (!game.Terminal(game.GameState)) {
                System.out.println("Board before move:");
                game.printBoard(game.GameState);
                Action bestMove = game.GetBestMove(target);

                //4. Make a best move locally and change player
                game.Move(currPlayer, bestMove);
                System.out.println("Board after move:");
                game.printBoard(game.GameState);

                //5. Send your move online (retry until move sent)
                response = ApiHelper.MakeMove(gameId, teamId1, bestMove.x, bestMove.y);
                boolean isSuccessful = IsSuccess(response);

                if (isSuccessful) {
                    myRecentMoveId = ParseMyMoveId(response);
                } else {
                    int counter = 0;
                    boolean isOpponentTurn = false;

                    if (ParseError(response).contains("Cannot make move - It is not the turn of team:")) {
                        isOpponentTurn = true;
                    }

                    while (!isSuccessful && !isOpponentTurn) {
                        response = ApiHelper.MakeMove(gameId, teamId1, bestMove.x, bestMove.y);
                        isSuccessful = IsSuccess(response);

                        if (counter++ >= 5) {
                            System.out.println("API Error: " + ParseError(response));
                            return;
                        }
                    }

                    if (!isOpponentTurn) {
                        myRecentMoveId = ParseMyMoveId(response);
                    }
                }

                //check whether terminal state
                if (game.Terminal(game.GameState)) {
                    //game ended, check who is winner
                    int utility = game.Utility(game.GameState);
                    if (utility == 1) {
                        System.out.println("WE WON");
                    } else if (utility == -1) {
                        System.out.println("WE LOST");
                    } else {
                        System.out.println("DRAW");
                    }
                    return;
                }

                //6. Get the new board state
                response = ApiHelper.GetMyMoves(gameId, recentCount);
                int recentMoveId = ParseRecentMoveId(response);

                if (recentMoveId > myRecentMoveId) {
                    byte[][] newState = ParseGameBoard(response, boardSize, mine);
                    game = game.FromState(newState, target);
                } else {
                    int counter = 0;
                    while (counter <= opponentWaitTimeoutRetry && recentMoveId <= myRecentMoveId) {
                        try {
                            System.out.println("Waiting for opponent...");
                            Thread.sleep(opponentWaitTimeoutSecond / opponentWaitTimeoutRetry * 1000);
                            counter++;
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        response = ApiHelper.GetMyMoves(gameId, recentCount);
                        recentMoveId = ParseRecentMoveId(response);
                    }

                    if (counter > opponentWaitTimeoutRetry) {
                        System.out.println("Opponent didn't make any move within " + opponentWaitTimeoutSecond + " seconds. Ending game.");
                        return;
                    } else {
                        byte[][] newState = ParseGameBoard(response, boardSize, mine);
                        game = game.FromState(newState, target);
                        continue;
                    }
                }
            }
        }
    }

    public static int ParseRecentMoveId(BaseResponse response)
    {
        JsonObject jsonObject = gson.fromJson(response.ResponseBody, JsonObject.class);
        JsonArray moves = jsonObject.get("moves").getAsJsonArray();
        return moves.get(0).getAsJsonObject().get("moveId").getAsInt();
    }

    public static int ParseMyMoveId(BaseResponse response)
    {
        JsonObject jsonObject = gson.fromJson(response.ResponseBody, JsonObject.class);
        return jsonObject.get("moveId").getAsInt();
    }

    public static int ParseGameId(BaseResponse response)
    {
        JsonObject jsonObject = gson.fromJson(response.ResponseBody, JsonObject.class);
        return jsonObject.get("gameId").getAsInt();
    }

    public static boolean IsSuccess(BaseResponse response)
    {
        JsonObject jsonObject = gson.fromJson(response.ResponseBody, JsonObject.class);
        return jsonObject.get("code").getAsString().equals("OK");
    }

    public static String ParseError(BaseResponse response)
    {
        JsonObject jsonObject = gson.fromJson(response.ResponseBody, JsonObject.class);
        try
        {
            if(jsonObject.get("code").getAsString().equals("FAIL"))
            {
                return jsonObject.get("message").getAsString();
            }
        }
        catch (Exception ex)
        {
            return "Unknown Error";
        }

        return  null;
    }

    public static byte[][] ParseGameBoard(BaseResponse response, int boardSize, String myPlayer)
    {
        JsonObject jsonObject = gson.fromJson(response.ResponseBody, JsonObject.class);

        byte[][] byteArr = new byte[boardSize][boardSize];

        if(jsonObject.get("code").hashCode() == "OK".hashCode()){
            int x,y;
            JsonArray movesArray = jsonObject.get("moves").getAsJsonArray();
            for (int i = 0; i < movesArray.size(); i++) {
                JsonObject tmpJsonObject = gson.fromJson(movesArray.get(i),JsonObject.class);

                x = tmpJsonObject.get("moveX").getAsInt();
                y = tmpJsonObject.get("moveY").getAsInt();

                byteArr[x][y] = tmpJsonObject.get("symbol").hashCode() == myPlayer.hashCode() ? (byte)1 : (byte)-1;

            }
        }

        return byteArr;
    }
}