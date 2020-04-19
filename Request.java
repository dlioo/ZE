package headline;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Request {

	private static final String[] FIELDS = {"author", "title", "url", "publishedAt"};
	private static final String HEADER = "source,author,title,url,publishedAt";

	private static final String HEADLINE_REQUEST_TEMPLATE =
			"https://newsapi.org/v1/articles?"
			+ "source=%s&sortBy=%s&apiKey=%s";

	public static void main(String[] args) {
		if(args.length  < 2) {
			printInvalidArgMessage();
			System.exit(0);
		}

		String source = args[0];
		String path = args[1];
		boolean append = false;

		if(args.length == 3) {
			if(args[2].equals("-a")) {
				append = true;
			}else {

			}
		}
		String apiKey = "";

		String url = String.format(HEADLINE_REQUEST_TEMPLATE,
				source, "top", apiKey);

	    try {
			Set<String> existingHeadlines = readCSV(path);
			String response = processRequest(url);
			List<String> headlines = ResponseParser.processHeadline(source, response, FIELDS );

			List<String> newHeadlines;
			if(append) {
				newHeadlines = headlines.stream().filter(
						h -> !existingHeadlines.contains(h)
						).collect(Collectors.toList());
			}else {
				newHeadlines = headlines;
			}

			saveCSV(source, newHeadlines, path, append);

		}
	    catch (IOException | requestException e) {
			System.err.println(
					String.format("The request was not executed successfully: \n"
							+ "\t %s", e.getMessage()));
		}
		finally {

		}
	}

	private static String processRequest(String url) throws requestException, IOException{

		URL conn = new URL(url);
		HttpURLConnection con = (HttpURLConnection) conn.openConnection();

		con.setRequestMethod("GET");
		con.setRequestProperty("Content-Type", "application/json");

		int status = con.getResponseCode();

        InputStream stream = status != 200 ? con.getErrorStream() : con.getInputStream();

        BufferedReader in = new BufferedReader(
                new InputStreamReader(stream));

        StringBuilder response = new StringBuilder();
        String line;

        while ((line = in.readLine()) != null) {
        	response.append(line);
        }

        in.close();
        con.disconnect();

        if(status != 200) {
        	throw new requestException(status, response.toString());
        }

        return response.toString();
	}

	private static void saveCSV(String source, Collection<String> headlines, String path, boolean append) {
		String date =  LocalDate.now().toString();
		String fullPath = path + String.format("/top_headlines_%s.csv", date);
    	File csvOutputFile = new File(fullPath);
    	boolean fileExists = csvOutputFile.exists();

       try (PrintWriter pw = new PrintWriter(
    		   new FileOutputStream(csvOutputFile, append))){
    	   if(!fileExists) {
    		   pw.println(HEADER);
    	   }
        	headlines.stream().forEach(pw::println);
        } catch (FileNotFoundException e) {
        	String message = e.getMessage();
			System.err.println(
					String.format("The file was not saved successfully: %s",
							message ));
		}
	}

	private static Set<String> readCSV(String path) {
		String date =  LocalDate.now().toString();
		String fullPath = path + String.format("/top_headlines_%s.csv", date);

		Set<String> headlines = new HashSet<>();
		try (BufferedReader br = new BufferedReader(new FileReader(fullPath))) {
		    String line;
		    while ((line = br.readLine()) != null) {
		    	headlines.add(line);
		    }
		    br.close();
		} catch (IOException e) {

		}

		return headlines;
	}

	private static void printInvalidArgMessage() {
		System.err.println("Please enter a source argument followed by \n"
				+ "the path of the directory where the file will be saved.\n"
				+ "-a can be entered as an optional third argument, \n"
				+ "new headlines will be appended to the daily headlines file \n"
				+ "if the file already exists. \n"
				+ "A list of sources is available at https://newsapi.org/v1/sources.");
	}

}
