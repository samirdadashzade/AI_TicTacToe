import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class Main {

    public static Gson gson = new Gson();

    public static void main(String[] args) {
        int teamId1 = 1243; //we "X" now
        int teamId2 = 1246;
        int boardSize = 3;
        String mine = teamId1 == 1243 ? "O" : "X";
        int target = 3;
        int gameId = 2370;
        int recentCount = boardSize * boardSize;

        //1. Create a game local (for having a Game instance)
        //2. Create a game remote
        //3. While game is not in Terminal state continue line 4, if Terminal continue line 8
        //4. Make a best move locally and change player
        //5. Send your move online
        //6. Get the new board state and make a move on behalf of the opponent
        //7. Continue
        //8. Show winner/loser/draw and finish game

        //1. Create a game local (for having a Game instance)
        Game game = new Game(boardSize,target, (byte)1);

        //2. Create a game remote
        //BaseResponse response = ApiHelper.CreateGame(teamId1, teamId2, boardSize, target);
        //gameId = ParseGameId(response);

        BaseResponse response = ApiHelper.GetMyMoves(gameId, recentCount);
//
        game = game.FromState(ParseGameBoard(response, boardSize, mine), target);

        //3. While game is not in Terminal state
        while(!game.Terminal(game.GameState))
        {



            System.out.println("Board before move:");
            game.printBoard(game.GameState);
            Action bestMove = game.GetBestMove(target);


            //4. Make a best move locally and change player
            game.Move(game.nextPlayer, bestMove);
//            if(true) continue;
//            if(true) continue;
            //5. Send your move online (retry until move sent)
            response = ApiHelper.MakeMove(gameId, teamId1, bestMove.x, bestMove.y);
            boolean isSuccessful = IsSuccess(response);

            while(!isSuccessful)
            {
                response = ApiHelper.MakeMove(gameId, teamId1, bestMove.x, bestMove.y);
                isSuccessful = IsSuccess(response);
            }
//            int swap = teamId1;
//            teamId1 = teamId2;
//            teamId2 = swap;
//
//
//            mine = teamId1 == 1243 ? "O" : "X";
//            response = ApiHelper.GetMyMoves(gameId, recentCount);
//            game = game.FromState(ParseGameBoard(response, boardSize, mine), target);


            //6. Get the new board state and make a move on behalf of the opponent
            //response = ApiHelper.GetMyMoves(gameId, recentCount);
            //byte[][] newState = ParseGameBoard(response, boardSize);

            //game = game.FromState(newState, target);
        }

//        System.out.println(response.ResponseBody);
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