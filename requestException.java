package headline;

public class requestException extends Exception{

	private static final long serialVersionUID = 1L;

	public requestException(int status, String errorResponse) {

        super(ResponseParser.getErrorMessage(status , errorResponse));
    }
}
