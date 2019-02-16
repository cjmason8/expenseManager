package au.com.mason.expensemanager.processor;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import javax.mail.Message;

import org.springframework.stereotype.Component;

import au.com.mason.expensemanager.domain.RefData;

@Component
public class DingleyWaterProccesor extends Processor {

	@Override
	public void execute(Message message, RefData refData) throws Exception {
		String body;
		if (message.isMimeType("text/html")) {
			body = message.getContent().toString();
			
			String amount = body.substring(body.indexOf("$") + 1, body.indexOf("<", body.indexOf("$"))).trim();
			
			int startIndex = body.indexOf("Date due") + 26;
			int beginIndex = body.indexOf("align=\"right\"", startIndex) + 14;
			String dueDateString = body.substring(beginIndex, body.indexOf("<", beginIndex));
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy").localizedBy(Locale.ENGLISH);
			LocalDate dueDate = LocalDate.parse(dueDateString, formatter);
			
			updateExpense(refData, dueDate, amount, null, "PDF requires uploading");
			
			/*CloseableHttpClient httpclient = HttpClients.custom().setRedirectStrategy(new LaxRedirectStrategy())
					.build();

			try {
				HttpClientContext context = HttpClientContext.create();
				HttpGet httpGet = new HttpGet("https://onlinebilling.energyaustralia.com.au/drsclient/EA_SME.html");
				System.out.println("Executing request " + httpGet.getRequestLine());
				System.out.println("----------------------------------------");

				httpclient.execute(httpGet, context);
				HttpHost target = context.getTargetHost();
				List<URI> redirectLocations = context.getRedirectLocations();
				URI location = URIUtils.resolve(httpGet.getURI(), target, redirectLocations);
				System.out.println("Final HTTP location: " + location.toASCIIString());

			} finally {
				httpclient.close();
			}*/

/*			HttpClient client = HttpClient.newBuilder().version(Version.HTTP_2).followRedirects(Redirect.ALWAYS)
					.build();

			HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create("https://analytics.linkre.direct/clickthrough?id=1BE5F1089D48455EBE0758C9C756D7E8&amp;issuer=sewaterp&amp;template=SEW0001&amp;url=https%3A%2F%2Fsoutheastwater.secure.force.com%2Fpublic%2FBillView%3Fcnum%3D15pA3jtv2hnB8AAMHyK3dA__%26bnum%3D66"))
					.timeout(Duration.ofMinutes(1)).header("Content-Type", "application/json").GET().build();

			HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
			System.out.println(response.statusCode());
			System.out.println(response.body());*/

/*			updateExpense(refData, dueDate, amount, null,
					"PDF requires uploading, discounted amount is $" + discountAmount);*/

		}
	}

}
