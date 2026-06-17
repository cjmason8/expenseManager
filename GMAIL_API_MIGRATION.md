# Gmail API Migration Guide

## Overview

Migrating from JavaMail IMAP to Gmail API provides:
- **Better Security**: OAuth 2.0 instead of app passwords
- **Modern API**: RESTful, well-documented
- **Gmail Features**: Labels, threads, search operators
- **Better Rate Limits**: 1 billion quota units per day
- **No IMAP Issues**: Direct API access

## Prerequisites

### 1. Google Cloud Console Setup

#### A. Create/Select Project
1. Go to https://console.cloud.google.com/
2. Create new project: "expense-manager" or use existing
3. Note the Project ID

#### B. Enable Gmail API
```bash
gcloud services enable gmail.googleapis.com --project=YOUR_PROJECT_ID
```
Or via Console: APIs & Services > Library > Search "Gmail API" > Enable

#### C. Create OAuth 2.0 Credentials

**For Server Application (Recommended):**
1. APIs & Services > Credentials > Create Credentials
2. Choose "OAuth 2.0 Client ID"
3. Application type: "Desktop app" (for first-time token generation)
4. Name it: "expense-manager-oauth"
5. Download JSON as `credentials.json`

**For Service Account (Alternative - No User Interaction):**
1. Create Service Account instead
2. Enable Domain-Wide Delegation (if using Google Workspace)
3. Download JSON key file

#### D. Configure OAuth Consent Screen
1. APIs & Services > OAuth consent screen
2. User Type: Internal (if Google Workspace) or External
3. App name: "Expense Manager"
4. User support email: your email
5. Add scopes:
   - `https://www.googleapis.com/auth/gmail.readonly`
   - `https://www.googleapis.com/auth/gmail.modify`
6. Add test users (if external): Add `cjmason8bills@gmail.com`

### 2. First-Time Token Generation

The first time you run the app, it will open a browser for OAuth consent:
1. User logs in with Google account
2. Grants permissions to app
3. App receives refresh token
4. Token stored locally for future use

## Step-by-Step Migration

### Step 1: Update pom.xml Dependencies

Remove:
```xml
<dependency>
    <groupId>javax.mail</groupId>
    <artifactId>mail</artifactId>
    <version>1.5.0-b01</version>
</dependency>
```

Add:
```xml
<!-- Gmail API -->
<dependency>
    <groupId>com.google.apis</groupId>
    <artifactId>google-api-services-gmail</artifactId>
    <version>v1-rev20240520-2.0.0</version>
</dependency>

<!-- Google OAuth Client -->
<dependency>
    <groupId>com.google.oauth-client</groupId>
    <artifactId>google-oauth-client-jetty</artifactId>
    <version>1.36.0</version>
</dependency>

<!-- Google API Client -->
<dependency>
    <groupId>com.google.api-client</groupId>
    <artifactId>google-api-client</artifactId>
    <version>2.7.0</version>
</dependency>

<!-- For email parsing (still useful) -->
<dependency>
    <groupId>jakarta.mail</groupId>
    <artifactId>jakarta.mail-api</artifactId>
    <version>2.1.3</version>
</dependency>
<dependency>
    <groupId>org.eclipse.angus</groupId>
    <artifactId>angus-mail</artifactId>
    <version>2.0.3</version>
</dependency>
```

### Step 2: Create GmailService Configuration

Create `GmailServiceConfig.java`:

```java
package au.com.mason.expensemanager.config;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GmailServiceConfig {

    private static final String APPLICATION_NAME = "Expense Manager";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(GmailScopes.GMAIL_MODIFY);

    @Value("${gmail.credentials.file:credentials.json}")
    private String credentialsFilePath;

    @Value("${gmail.tokens.directory:tokens}")
    private String tokensDirectoryPath;

    @Bean
    public Gmail gmailService() throws GeneralSecurityException, IOException {
        NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        return new Gmail.Builder(httpTransport, JSON_FACTORY, getCredentials(httpTransport))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    private Credential getCredentials(final NetHttpTransport httpTransport) throws IOException {
        // Load client secrets
        GoogleClientSecrets clientSecrets;
        try (FileInputStream in = new FileInputStream(credentialsFilePath)) {
            clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
        }

        // Build flow and trigger user authorization request
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(tokensDirectoryPath)))
                .setAccessType("offline")
                .build();

        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }
}
```

### Step 3: Store OAuth Credentials in AWS Secrets Manager

Instead of file-based credentials, store in AWS Secrets:

**Create secret `gmail-oauth-credentials`:**
```json
{
  "client_id": "your-client-id.apps.googleusercontent.com",
  "client_secret": "your-client-secret",
  "refresh_token": "your-refresh-token"
}
```

**Update GmailServiceConfig to use AWS Secrets:**
```java
@Autowired
private AwsSecretsService awsSecretsService;

@Value("${gmail.oauth.secret.name:gmail-oauth-credentials}")
private String gmailOAuthSecretName;

private Credential getCredentialsFromSecrets(final NetHttpTransport httpTransport) throws IOException {
    Map<String, String> oauthCreds = awsSecretsService.getSecretAsMap(gmailOAuthSecretName);

    String clientId = oauthCreds.get("client_id");
    String clientSecret = oauthCreds.get("client_secret");
    String refreshToken = oauthCreds.get("refresh_token");

    GoogleClientSecrets.Details details = new GoogleClientSecrets.Details()
            .setClientId(clientId)
            .setClientSecret(clientSecret);
    GoogleClientSecrets clientSecrets = new GoogleClientSecrets().setInstalled(details);

    GoogleCredential credential = new GoogleCredential.Builder()
            .setTransport(httpTransport)
            .setJsonFactory(JSON_FACTORY)
            .setClientSecrets(clientSecrets)
            .build();
    credential.setRefreshToken(refreshToken);

    return credential;
}
```

### Step 4: Refactor EmailTrawler

Create new `GmailEmailTrawler.java`:

```java
package au.com.mason.expensemanager.robot;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.ModifyMessageRequest;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import au.com.mason.expensemanager.domain.Notification;
import au.com.mason.expensemanager.domain.RefData;
import au.com.mason.expensemanager.processor.EmailProcessor;
import au.com.mason.expensemanager.service.NotificationService;
import au.com.mason.expensemanager.service.RefDataService;

@Component
public class GmailEmailTrawler {

    private static final Logger LOGGER = LogManager.getLogger(GmailEmailTrawler.class);
    private static final String USER_ID = "me";

    @Autowired
    private Gmail gmailService;

    @Autowired
    private RefDataService refDataService;

    @Autowired
    private NotificationService notificationService;

    public void check() {
        try {
            // Query for unread messages
            String query = "is:unread in:inbox";
            ListMessagesResponse response = gmailService.users().messages()
                    .list(USER_ID)
                    .setQ(query)
                    .execute();

            List<Message> messages = response.getMessages();
            if (messages == null || messages.isEmpty()) {
                LOGGER.info("No unread messages found.");
                return;
            }

            LOGGER.info("Found {} unread messages", messages.size());
            List<RefData> refDatas = refDataService.getAllWithEmailKey();

            for (Message messageStub : messages) {
                try {
                    // Fetch full message
                    Message fullMessage = gmailService.users().messages()
                            .get(USER_ID, messageStub.getId())
                            .setFormat("raw")
                            .execute();

                    // Convert to MimeMessage for easier parsing
                    MimeMessage mimeMessage = convertToMimeMessage(fullMessage);

                    // Check blacklist
                    if (isBlacklisted(mimeMessage)) {
                        markAsRead(fullMessage.getId());
                        continue;
                    }

                    String subject = mimeMessage.getSubject();
                    LOGGER.info("Processing: {}", subject);

                    // Find matching processor
                    boolean foundIt = false;
                    for (RefData refData : refDatas) {
                        if (refDataMatch(mimeMessage, refData)) {
                            LOGGER.info("Found Processor: {}", refData.getEmailProcessor().getProcessor().getClass());
                            // Note: You may need to adapt processors to work with MimeMessage
                            refData.getEmailProcessor().getProcessor().execute(mimeMessage, refData);
                            foundIt = true;
                            break;
                        }
                    }

                    if (!foundIt) {
                        Notification notification = new Notification();
                        notification.setMessage("Unhandled Email: " + subject);
                        notificationService.create(notification);
                    }

                    // Mark as read
                    markAsRead(fullMessage.getId());

                } catch (Exception e) {
                    LOGGER.error("Error processing message: {}", messageStub.getId(), e);
                }
            }

        } catch (Exception e) {
            LOGGER.error("Error checking Gmail", e);
        }
    }

    private MimeMessage convertToMimeMessage(Message message) throws Exception {
        byte[] emailBytes = Base64.getUrlDecoder().decode(message.getRaw());
        Session session = Session.getDefaultInstance(new java.util.Properties());
        return new MimeMessage(session, new ByteArrayInputStream(emailBytes));
    }

    private void markAsRead(String messageId) throws Exception {
        ModifyMessageRequest modifyRequest = new ModifyMessageRequest()
                .setRemoveLabelIds(Collections.singletonList("UNREAD"));
        gmailService.users().messages().modify(USER_ID, messageId, modifyRequest).execute();
    }

    private boolean isBlacklisted(MimeMessage message) throws Exception {
        List<String> blacklist = List.of("tripadvisor", "roses", "puzzles", "youtube",
                "messages.telstra.com", "storm", "marvel", "paypal", "tennis", "mightymunch");

        String from = message.getFrom() != null && message.getFrom().length > 0
                ? message.getFrom()[0].toString()
                : "";

        return blacklist.stream().anyMatch(from.toLowerCase()::contains);
    }

    private boolean refDataMatch(MimeMessage message, RefData refData) throws Exception {
        // Port your existing refDataMatch logic here
        // The MimeMessage API is similar to javax.mail.Message
        String subject = message.getSubject();
        String body = getBodyContent(message);

        // Your existing matching logic...
        return subject != null && subject.contains(refData.getEmailKey());
    }

    private String getBodyContent(MimeMessage message) throws Exception {
        Object content = message.getContent();
        if (content instanceof String) {
            return (String) content;
        } else if (content instanceof jakarta.mail.Multipart) {
            jakarta.mail.Multipart multipart = (jakarta.mail.Multipart) content;
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < multipart.getCount(); i++) {
                jakarta.mail.BodyPart bodyPart = multipart.getBodyPart(i);
                if (bodyPart.isMimeType("text/html") || bodyPart.isMimeType("text/plain")) {
                    sb.append(bodyPart.getContent());
                }
            }
            return sb.toString();
        }
        return "";
    }
}
```

### Step 5: Update Configuration Files

**application.properties:**
```properties
# Gmail API Configuration
gmail.credentials.file=${GMAIL_CREDENTIALS_FILE:/resources/credentials.json}
gmail.tokens.directory=${GMAIL_TOKENS_DIR:/resources/tokens}
gmail.oauth.secret.name=gmail-oauth-credentials
```

**AWS Secret: `gmail-oauth-credentials`:**
```json
{
  "client_id": "123456789-abcdef.apps.googleusercontent.com",
  "client_secret": "GOCSPX-your-client-secret",
  "refresh_token": "1//your-refresh-token"
}
```

### Step 6: Update Processor Interface (if needed)

If your processors use `javax.mail.Message`, update them to use `jakarta.mail.internet.MimeMessage`:

```java
public interface EmailProcessorInterface {
    void execute(jakarta.mail.internet.MimeMessage message, RefData refData) throws Exception;
}
```

## Testing Plan

### 1. Local Testing
```bash
# Set environment variables
export ENV=local
export GMAIL_CREDENTIALS_FILE=/path/to/credentials.json

# Run application
mvn spring-boot:run
```

### 2. First Run
- Browser will open for OAuth consent
- Log in with `cjmason8bills@gmail.com`
- Grant permissions
- Token saved to `tokens/` directory

### 3. Verify Token Storage
- Check `tokens/StoredCredential` file created
- Extract refresh token for AWS Secrets Manager

## Comparison: Current vs Gmail API

| Feature | Current (IMAP) | Gmail API |
|---------|----------------|-----------|
| **Authentication** | App Password | OAuth 2.0 |
| **Security** | Password-based | Token-based, revocable |
| **Protocol** | IMAP | REST API |
| **Rate Limits** | Gmail IMAP limits | 1B quota units/day |
| **Features** | Basic email | Labels, threads, search |
| **Connection** | Socket connection | HTTPS |
| **Error Handling** | Connection timeouts | HTTP status codes |
| **Gmail-specific** | No | Yes (filters, labels) |

## Migration Checklist

- [ ] Enable Gmail API in Google Cloud Console
- [ ] Create OAuth 2.0 credentials
- [ ] Download credentials.json
- [ ] Update pom.xml dependencies
- [ ] Create GmailServiceConfig
- [ ] Refactor EmailTrawler to GmailEmailTrawler
- [ ] Test OAuth flow locally
- [ ] Extract refresh token
- [ ] Store credentials in AWS Secrets Manager
- [ ] Update configuration files
- [ ] Test in local environment
- [ ] Test in production environment
- [ ] Update deployment scripts
- [ ] Remove old javax.mail dependency
- [ ] Remove email-credentials secret (no longer needed)

## Rollback Plan

Keep old EmailTrawler code in parallel:
- Rename to `LegacyEmailTrawler`
- Use feature flag to switch between implementations
- Remove after successful migration

```java
@Value("${email.use.gmail.api:true}")
private boolean useGmailApi;

public void check() {
    if (useGmailApi) {
        gmailEmailTrawler.check();
    } else {
        legacyEmailTrawler.check();
    }
}
```

## Additional Resources

- [Gmail API Java Quickstart](https://developers.google.com/gmail/api/quickstart/java)
- [Gmail API Reference](https://developers.google.com/gmail/api/reference/rest)
- [OAuth 2.0 for Server-Side Apps](https://developers.google.com/identity/protocols/oauth2/web-server)
