public class BaseResponse {
    public boolean Result;
    public int ResultCode;
    public String ResponseBody;

    public BaseResponse(int resultCode, boolean result, String body)
    {
        Result = result;
        ResultCode = resultCode;
        ResponseBody = body;
    }
}
