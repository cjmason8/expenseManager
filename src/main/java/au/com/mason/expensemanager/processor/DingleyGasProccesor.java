package au.com.mason.expensemanager.processor;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import javax.mail.Message;

import org.springframework.stereotype.Component;

import au.com.mason.expensemanager.domain.RefData;

@Component
public class DingleyGasProccesor extends Processor {
	
	@Override
	public void execute(Message message, RefData refData) throws Exception {
		String body;
		if (message.isMimeType("text/html")) {
	        body = message.getContent().toString();
	        /*int startIndex = body.indexOf("View Your Bill") + 26;
            String url = body.substring(startIndex, body.indexOf("\" ", startIndex)).trim();*/
	        
	        LocalDate dueDate = null;
	        String amount = null;
	        String discountAmount = null;
	        
	        int startIndex = body.indexOf("<strong>", body.indexOf("Due date by Direct Debit")) + 8;
	        String dueDateString = body.substring(startIndex, body.indexOf("<", startIndex)).trim();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy").localizedBy(Locale.ENGLISH);
            dueDate = LocalDate.parse(dueDateString, formatter);
            
            startIndex = body.indexOf("$", body.indexOf("Total amount if paid")) + 1;
	        amount = body.substring(startIndex, body.indexOf("<", startIndex)).trim();
	        
	        startIndex = body.indexOf("$", body.indexOf("Discounted amount")) + 1;
	        discountAmount = body.substring(startIndex, body.indexOf("<", startIndex)).trim();
	        
	        updateExpense(refData, dueDate, amount, null, "PDF requires uploading, discounted amount is $" + discountAmount);
	        
            
/*            CloseableHttpClient httpclient = HttpClients.custom()
                    .setRedirectStrategy(new LaxRedirectStrategy())
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
            }
            
            HttpClient client = HttpClient.newBuilder()
            	      .version(Version.HTTP_2)
            	      .followRedirects(Redirect.ALWAYS)
            	      .build();
            
            HttpRequest request = HttpRequest.newBuilder()
            	      .uri(URI.create("https://onlinebilling.energyaustralia.com.au/drsclient/EA_SME.html"))
            	      .timeout(Duration.ofMinutes(1))
            	      .header("Content-Type", "application/json")
            	      .GET()
            	      .build();
            
            HttpResponse<String> response =
            	      client.send(request, BodyHandlers.ofString());
            	System.out.println(response.statusCode());
            	System.out.println(response.body());*/
		}
	}

}
