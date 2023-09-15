package au.com.mason.expensemanager.service;

import au.com.mason.expensemanager.dto.LoginInput;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class UserAuthenticationService {
	
	private static final String EXPENSE_MANAGER = "EXPENSE_MANAGER";
	
	@Value("#{systemEnvironment['AUTH_SERVICE_END_POINT']}")
	private String authServiceEndPoint;
	
	public String login(LoginInput loginInput) throws Exception {
		loginInput.setApplicationType(EXPENSE_MANAGER);

        String result = null;
        HttpPost httpPost = new HttpPost(authServiceEndPoint + "/login");
        ObjectMapper mapper = new ObjectMapper();
        String jsonInString = mapper.writeValueAsString(loginInput);
        StringEntity params = new StringEntity(jsonInString);
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setEntity(params);
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
                HttpEntity entity = response.getEntity();
                result = EntityUtils.toString(entity);
                // Ensure that the stream is fully consumed
                EntityUtils.consume(entity);
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        return result;
	}
	
	public JSONObject authenticate(String token) throws Exception {
        String resultContent = null;
        HttpGet httpGet = new HttpGet(authServiceEndPoint + "/authenticate/" + token);
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            try (CloseableHttpResponse response = httpclient.execute(httpGet)) {
                HttpEntity entity = response.getEntity();
                // Get response information
                resultContent = EntityUtils.toString(entity);
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        return new JSONObject(resultContent);

	}	
	
}
