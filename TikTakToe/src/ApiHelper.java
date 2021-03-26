import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

public class ApiHelper {

    public static BaseResponse GetMyTeams()
    {
        return SendRequest("type=myTeams", true);
    }

    //TEAM MANAGEMENT

    //Create a new game between teams
    //Post returns GameId
    public static BaseResponse CreateGame(int teamId1, int teamId2, int boardSize, int target)
    {
        String path = "type=game&teamId1="+teamId1+"&teamId2="+teamId2+ "&gameType=TTT&boardSize="+boardSize+"&target="+target;
        return SendRequest(path, false);
    }

    //Returns comma separated games
    public static BaseResponse GetMyGames() { return SendRequest("type=myGames", true); }


    //GAME MANAGEMENT

    //Make a new move
    //Returns moveId
    public static BaseResponse MakeMove(int gameId, int teamId, int xMove, int yMove)
    {
        String path = "type=move&gameId="+gameId+"&teamId="+teamId+ "&move="+xMove+","+yMove;
        return SendRequest(path, false);
    }

    //Returns all moves for the game with the count (comma separated)
    public static BaseResponse GetMyMoves(int gameId, int count) {
        String path = "type=moves&gameId="+gameId+"&count="+count;
        return SendRequest(path, true);
    }


    //BOARD MANAGEMENT

    //Returns Board, in a form of a string of O,X,-
    public static BaseResponse GetBoardString(int gameId) {
        String path = "type=boardString&gameId="+gameId;
        return SendRequest(path, true);
    }

    //Returns Board, in a form of a string of O,X,-
    public static BaseResponse GetBoardMap(int gameId) {
        String path = "type=boardMap&gameId="+gameId;
        return SendRequest(path, true);
    }

    public static BaseResponse SendRequest(String path, boolean get)
    {
        BaseResponse response = null;
        HttpResponse<String> httpResponse = null;

        try {
            if(get)
            {
                httpResponse = Unirest.get(
                                "https://www.notexponential.com/aip2pgaming/api/index.php?" + path)
                                .header("x-api-key", "69f316d7e77c3b595ddf")
                                .header("userid", "1049")
                                .header("Content-Type", "application/x-www-form-urlencoded")
                                .asString();
            }
            else
            {
                httpResponse = Unirest.post(
                                "https://www.notexponential.com/aip2pgaming/api/index.php")
                                .body(path)
                                .header("x-api-key", "69f316d7e77c3b595ddf")
                                .header("userid", "1049")
                                .header("Content-Type", "application/x-www-form-urlencoded")
                                .asString();
            }

            response = new BaseResponse(httpResponse.getStatus(), true, httpResponse.getBody());
        }
        catch (Exception ex)
        {
            response = new BaseResponse(500, false, null);
        }

        return response;
    }
}
