package headline;

import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class ResponseParser {

	private static final String MESSAGE_FIELD = "message";
	private static final String ARTICLES_FIELD = "articles";

	public static String getErrorMessage(int status, String errorResponse) {
		JSONObject response =  new JSONObject(errorResponse);
		String message = response.isNull(MESSAGE_FIELD)? "" :
			response.getString(MESSAGE_FIELD);
		return String.format("status: %d - %s", status, message);
	}

	public static List<String> processHeadline(String source, String response,
			String[] fields){
        List<String> headlines = new LinkedList<>();

        JSONObject jsonObj = new JSONObject(response);
        JSONArray articles = jsonObj.getJSONArray(ARTICLES_FIELD);

        for(int i = 0; i < articles.length(); i++) {
        	JSONObject article = articles.getJSONObject(i);
        	String csv = headlineInCSVFormat(source, fields, article);
        	headlines.add(csv);
        }

        return headlines;
	}

	private static String headlineInCSVFormat(String source,
			String[] fields, JSONObject article) {
		StringBuilder headline = new StringBuilder(source);

		for(String header : fields) {
			headline.append(",");

			if(!article.isNull(header)) {
				headline.append(escapeCharacters(article.getString(header)));
			}
		}

		return headline.toString();
	}

	private static String escapeCharacters(String input) {
		String escapedData = input.replaceAll("\\r", " ");
	    if (escapedData.contains(",") || escapedData.contains("\"")
	    		|| escapedData.contains("'")) {
	    	escapedData = escapedData.replace("\"", "\"\"");
	        escapedData = "\"" + escapedData + "\"";
	    }
		return escapedData;
	}

}
