import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

public class ClassChecker {
	private static final String PUSH_NOTIFICATION_IDENTIFIER = "123abc456def";
	public static final long UPDATE_INTERVAL_MS = 5 * 60 * 1000;
	public static final String[] CLASSES_TO_CHECK = new String[] {
			"https://sa.ucla.edu/ro/Public/SOC/Results/ClassDetail?term_cd=20F&subj_area_cd=MATH%20%20%20&crs_catlg_no=0032A%20%20%20&class_id=262206221&class_no=%20003%20%20",
			"https://sa.ucla.edu/ro/Public/SOC/Results/ClassDetail?term_cd=20F&subj_area_cd=MATH%20%20%20&crs_catlg_no=0032A%20%20%20&class_id=262206234&class_no=%20004%20%20" };

	public static void main(String[] args) {
		System.out.println("Hello World!");
		sendPushNotification(PUSH_NOTIFICATION_IDENTIFIER, "UCLA Class Checker Starting Up", "Currently booting up",
				null);

		// Initialize static data
		URL[] urls = new URL[CLASSES_TO_CHECK.length];
		// open = true; closed = false;
		HashMap<URL, Boolean> previousState = new HashMap<URL, Boolean>(urls.length);

		for (int tempIndex = 0; tempIndex < CLASSES_TO_CHECK.length; tempIndex++) {
			try {
				urls[tempIndex] = new URL(CLASSES_TO_CHECK[tempIndex]);
				previousState.put(urls[tempIndex], false);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}

		Pattern textExtractorFromHTML = Pattern.compile("\\<.*?>");

		// Program Loop
		while (true) {
			try {
				Thread.sleep(UPDATE_INTERVAL_MS);
				URL u;

				for (int counter = 0; counter < urls.length; counter++) {
					long a = System.currentTimeMillis();
					u = urls[counter];
					URLConnection conn = u.openConnection();
					// BufferedReader in = new BufferedReader(new
					// InputStreamReader(conn.getInputStream()));
					BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
					ArrayList<String> HTMLLines = new ArrayList<String>();
					String inputLine;
					boolean containsClosed = false;
					boolean containsOpen = false;
					int openClosedLineIndex = -1;
					int classInfoLineIndex = -1;
					int index = -1;

					// Scan raw HTML for open/closed status and store to ArrayList
					while ((inputLine = in.readLine()) != null) {
						index++;
						HTMLLines.add(inputLine);
						if (!containsClosed && !containsOpen && inputLine.contains("Closed:")) {
							containsClosed = true;
							openClosedLineIndex = index;
						}
						if (!containsOpen && !containsClosed && inputLine.contains("Open:")) {
							containsOpen = true;
							openClosedLineIndex = index;
						}
						if (classInfoLineIndex == -1 && inputLine.contains("subject_class")) {
							classInfoLineIndex = index;
						}
					}
					in.close();

					// If something is both open and closed at the same time, send error alert
					if (!(containsClosed ^ containsOpen)) {
						String message = String.format(
								"Detecting open/closed status failed%nClass = %s%ncontainsClosed = %b, containsOpen = %b",
								textExtractorFromHTML.matcher(HTMLLines.get(classInfoLineIndex + 1)).replaceAll("")
										.trim(),
								containsClosed, containsOpen);
						System.err.println(message);
						sendPushNotification(PUSH_NOTIFICATION_IDENTIFIER, "Error", message, u);
						continue;
					}

					boolean currentState = containsOpen;

					if (previousState.get(u) != currentState) {
						// Run below code if update found;
						String classTitle = textExtractorFromHTML.matcher(HTMLLines.get(classInfoLineIndex + 1))
								.replaceAll("").trim();
						String discussionInfo = textExtractorFromHTML.matcher(HTMLLines.get(classInfoLineIndex + 2))
								.replaceAll("").trim();
						String classID = textExtractorFromHTML.matcher(HTMLLines.get(classInfoLineIndex + 3))
								.replaceAll("").trim();
						String classWebsite = textExtractorFromHTML.matcher(HTMLLines.get(classInfoLineIndex + 4))
								.replaceAll("").trim();
						String classEnrollmentInfo = textExtractorFromHTML.matcher(HTMLLines.get(openClosedLineIndex))
								.replaceAll("").trim();
						String message = String.format(
								"Status has changed from %s to %s.%nClass Title: %s%n%s%n%s%n%s%nCurrent Status: %s",
								(!currentState) ? "Open" : "Closed", currentState ? "Open" : "Closed", classTitle,
								discussionInfo, classID, classWebsite, classEnrollmentInfo);
						URL classURL = new URL(classWebsite.replaceAll("Class Website:", "").trim());
						sendPushNotification(PUSH_NOTIFICATION_IDENTIFIER, classTitle, message, classURL);

						// update hashmap
						previousState.put(u, currentState);
					}

					System.out.println(System.currentTimeMillis() - a + "ms");
				}
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public static int sendPushNotification(String identifier, String title, String message, URL url) {
		try {
			HttpURLConnection conn;
			// URL u = new
			// URL("https://pushmeapi.jagcesar.se?identifier=XXXXXXXXXX&title=test");

			URL u = new URL(new URI("https", "pushmeapi.jagcesar.se", null,
					String.format("identifier=%s&title=%s&body=%s", identifier, title, message), null).toString()
					+ (url != null ? "&url=" + url.toString() : ""));

			System.out.println("Push request made with get request at " + u);
			conn = (HttpURLConnection) u.openConnection();
			conn.setRequestMethod("GET");

			return conn.getResponseCode();
		} catch (IOException | URISyntaxException e) {
			e.printStackTrace();
		}
		return HttpURLConnection.HTTP_BAD_REQUEST;

	}
}