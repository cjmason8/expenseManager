# AWS Secrets Manager Integration

## Overview

This project now includes integration with AWS Secrets Manager to securely manage sensitive configuration like database credentials, API keys, and other secrets.

## Components Added

### 1. AwsSecretsService

A Spring service that provides easy access to AWS Secrets Manager.

**Location**: `src/main/java/au/com/mason/expensemanager/service/AwsSecretsService.java`

### 2. Dependencies

Added to `pom.xml`:
- `software.amazon.awssdk:secretsmanager:2.25.70`

## Configuration

### Environment Variables (Recommended for Production)

```bash
# Secrets Manager
export AWS_SECRETS_ENABLED=true
export AWS_SECRETS_REGION=ap-southeast-2

# Credentials (optional - use IAM roles in AWS)
export AWS_ACCESS_KEY_ID=your-access-key
export AWS_SECRET_ACCESS_KEY=your-secret-key
```

### Application Properties (Development)

```properties
aws.secrets.enabled=true
aws.secrets.region=ap-southeast-2
aws.secrets.access-key=${AWS_ACCESS_KEY_ID:}
aws.secrets.secret-key=${AWS_SECRET_ACCESS_KEY:}
```

## Current Implementation

### EmailTrawler Integration

The `EmailTrawler` class now uses AWS Secrets Manager to retrieve email credentials:

```java
// In EmailTrawler.java
@Autowired
private AwsSecretsService awsSecretsService;

@Value("${email.secret.name:email-credentials}")
private String emailSecretName;

public void check() {
    String user = awsSecretsService.getSecretValue(emailSecretName, "USER_NAME");
    String password = awsSecretsService.getSecretValue(emailSecretName, "PASSWORD");
    // ... use credentials to connect to Gmail
}
```

**AWS Secret Format** (`email-credentials`):
```json
{
  "USER_NAME": "cjmason8bills@gmail.com",
  "PASSWORD": "your-app-password-here"
}
```

### DatabaseConfig Integration

The `DatabaseConfig` class uses AWS Secrets Manager for database credentials, automatically selecting the correct secret based on environment:

```java
// In DatabaseConfig.java
@Value("${env:local}")
private String environment;

public DataSource dataSource() {
    String secretName = environment.equalsIgnoreCase("prd")
        ? "prod-database-credentials"
        : "local-database-credentials";
    dataSource.setUsername(awsSecretsService.getSecretValue(secretName, "USER_NAME"));
    dataSource.setPassword(awsSecretsService.getSecretValue(secretName, "PASSWORD"));
}
```

**AWS Secret Formats**:

`local-database-credentials`:
```json
{
  "USER_NAME": "postgres",
  "PASSWORD": "Yoke1976%"
}
```

`prod-database-credentials`:
```json
{
  "USER_NAME": "postgres",
  "PASSWORD": "postgres2"
}
```

## Additional Usage Examples

### Example 1: Retrieve Custom Credentials from Secrets Manager

```java
@Service
public class DatabaseConfigService {

    @Autowired
    private AwsSecretsService awsSecretsService;

    public void configureDatabaseFromSecrets() {
        // Retrieve entire secret as JSON
        Map<String, String> dbCredentials = awsSecretsService.getSecretAsMap("prod/expensemanager/database");

        String dbHost = dbCredentials.get("host");
        String dbUser = dbCredentials.get("username");
        String dbPassword = dbCredentials.get("password");
        String dbName = dbCredentials.get("database");

        // Use credentials to configure database connection
    }

    // Or retrieve specific value
    public String getDatabasePassword() {
        return awsSecretsService.getSecretValue("prod/expensemanager/database", "password");
    }
}
```

### Example 2: Retrieve API Keys

```java
@Service
public class ExternalApiService {

    @Autowired
    private AwsSecretsService awsSecretsService;

    public String getApiKey() {
        // For a simple string secret
        return awsSecretsService.getSecretString("prod/expensemanager/api-key");
    }
}
```

### Example 3: Conditional Usage Based on Environment

```java
@Service
public class ConfigService {

    @Autowired
    private AwsSecretsService awsSecretsService;

    @Value("${database.password:}")
    private String localDbPassword;

    public String getDatabasePassword() {
        if (awsSecretsService.isEnabled()) {
            return awsSecretsService.getSecretValue("prod/expensemanager/database", "password");
        } else {
            // Fallback to local configuration
            return localDbPassword;
        }
    }
}
```

## AWS Secrets Manager Secret Format

### JSON Format (Recommended)

Secrets stored in JSON format for structured data:

```json
{
  "host": "database.example.com",
  "port": "5432",
  "username": "dbuser",
  "password": "secure-password",
  "database": "expensemanager"
}
```

### Plain Text Format

For simple string values like API keys:

```
your-api-key-here
```

## Creating Secrets in AWS

### Using AWS CLI

```bash
# Create email credentials secret (REQUIRED for EmailTrawler)
aws secretsmanager create-secret \
    --name email-credentials \
    --description "Email credentials for EmailTrawler" \
    --secret-string '{
        "USER_NAME": "cjmason8bills@gmail.com",
        "PASSWORD": "your-app-password-here"
    }' \
    --region ap-southeast-2

# Create local database credentials secret
aws secretsmanager create-secret \
    --name local-database-credentials \
    --description "Local database credentials for expense manager" \
    --secret-string '{
        "USER_NAME": "postgres",
        "PASSWORD": "Yoke1976%"
    }' \
    --region ap-southeast-2

# Create production database credentials secret
aws secretsmanager create-secret \
    --name prod-database-credentials \
    --description "Production database credentials for expense manager" \
    --secret-string '{
        "USER_NAME": "postgres",
        "PASSWORD": "postgres2"
    }' \
    --region ap-southeast-2

# Create a plain text secret
aws secretsmanager create-secret \
    --name prod/expensemanager/api-key \
    --secret-string "your-api-key-here" \
    --region ap-southeast-2
```

### Using AWS Console

1. Navigate to AWS Secrets Manager
2. Click "Store a new secret"
3. Choose secret type:
   - "Other type of secret" for custom JSON
   - "Plaintext" for simple strings
4. Enter secret name (e.g., `prod/expensemanager/database`)
5. Configure rotation if needed
6. Review and create

## IAM Permissions Required

Your application needs these IAM permissions:

```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": [
                "secretsmanager:GetSecretValue",
                "secretsmanager:DescribeSecret"
            ],
            "Resource": [
                "arn:aws:secretsmanager:ap-southeast-2:*:secret:prod/expensemanager/*"
            ]
        }
    ]
}
```

## Local Development

For local development, you can:

1. **Disable Secrets Manager**: Set `aws.secrets.enabled=false`
2. **Use LocalStack**: Run LocalStack to simulate AWS services locally
3. **Use AWS Credentials**: Configure AWS CLI with credentials for a dev account

## Best Practices

1. **Use IAM Roles**: In AWS (EC2, ECS, Lambda), use IAM roles instead of access keys
2. **Secret Naming**: Use a consistent naming convention like `{environment}/{app}/{service}`
3. **Rotation**: Enable automatic secret rotation for production databases
4. **Least Privilege**: Grant only necessary permissions to access secrets
5. **Caching**: Consider caching secrets in memory to reduce API calls
6. **Error Handling**: Always handle `SecretsManagerException` appropriately

## Error Handling

```java
try {
    String password = awsSecretsService.getSecretString("my-secret");
} catch (software.amazon.awssdk.services.secretsmanager.model.ResourceNotFoundException e) {
    // Secret doesn't exist
    logger.error("Secret not found: {}", e.getMessage());
} catch (software.amazon.awssdk.services.secretsmanager.model.SecretsManagerException e) {
    // Other AWS errors
    logger.error("Failed to retrieve secret: {}", e.getMessage());
}
```

## Troubleshooting

### Common Issues

1. **Authentication Errors**
   - Verify AWS credentials are configured
   - Check IAM permissions
   - Ensure correct region is set

2. **Secret Not Found**
   - Verify secret name matches exactly
   - Check you're in the correct AWS region
   - Confirm the secret exists in AWS console

3. **Access Denied**
   - Review IAM policy attached to your role/user
   - Verify resource ARNs match your secrets

## Migration Path

1. **Create secrets in AWS Secrets Manager** for all sensitive values
2. **Update application configuration** to use AwsSecretsService
3. **Test in development environment** first
4. **Deploy to staging** and verify
5. **Remove hardcoded secrets** from configuration files
6. **Deploy to production**
