package au.com.mason.expensemanager.processor;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import jakarta.mail.BodyPart;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

import org.eclipse.angus.mail.util.BASE64DecoderStream;

import au.com.mason.expensemanager.domain.Document;
import au.com.mason.expensemanager.domain.Notification;
import au.com.mason.expensemanager.domain.RefData;
import au.com.mason.expensemanager.domain.RentalPayment;
import au.com.mason.expensemanager.pdf.PdfReader;
import au.com.mason.expensemanager.service.DocumentService;
import au.com.mason.expensemanager.service.NotificationService;
import au.com.mason.expensemanager.service.RentalPaymentService;

public class TestSouthKingsvilleProcessor {

    public static void main(String[] args) {
        try {
            System.out.println("=== Starting South Kingsville Rental Statement Processor Test ===\n");
            
            // Read the PDF file
            byte[] pdfBytes = Files.readAllBytes(Paths.get("statement.pdf"));
            System.out.println("✓ Read statement.pdf - " + pdfBytes.length + " bytes");
            
            // Create mock processor with mocked services
            SouthKingsvilleRentalStatementProccesor processor = new SouthKingsvilleRentalStatementProccesor();
            
            // Create mock services
            MockNotificationService mockNotificationService = new MockNotificationService();
            MockRentalPaymentService mockRentalPaymentService = new MockRentalPaymentService();
            MockDocumentService mockDocumentService = new MockDocumentService();
            
            // Use reflection to set private fields
            setPrivateField(processor, "notificationService", mockNotificationService);
            setPrivateField(processor, "rentalPaymentService", mockRentalPaymentService);
            setPrivateField(processor, "documentService", mockDocumentService);
            
            // Create a mock email message with PDF attachment
            Session session = Session.getDefaultInstance(new Properties());
            MimeMessage message = new MimeMessage(session);
            message.setSubject("Statement #93 from 11/11/2025 to 10/12/2025");
            
            // Create multipart message
            MimeMultipart multipart = new MimeMultipart();
            
            // Add HTML part (will be skipped)
            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent("<html><body>Rental Statement</body></html>", "text/html");
            multipart.addBodyPart(htmlPart);
            
            // Add PDF part (this should trigger the else if on line 58)
            MimeBodyPart pdfPart = new MimeBodyPart();
            // Use setDataHandler to properly set the content with the right MIME type
            jakarta.activation.DataHandler dh = new jakarta.activation.DataHandler(
                new jakarta.activation.DataSource() {
                    public java.io.InputStream getInputStream() {
                        return new ByteArrayInputStream(pdfBytes);
                    }
                    public java.io.OutputStream getOutputStream() {
                        throw new UnsupportedOperationException();
                    }
                    public String getContentType() {
                        return "APPLICATION/PDF";
                    }
                    public String getName() {
                        return "statement.pdf";
                    }
                }
            );
            pdfPart.setDataHandler(dh);
            pdfPart.setFileName("statement.pdf");
            multipart.addBodyPart(pdfPart);
            
            message.setContent(multipart);
            
            System.out.println("✓ Created mock email message with multipart content");
            System.out.println("  - Subject: " + message.getSubject());
            System.out.println("  - Parts: " + multipart.getCount());
            System.out.println("  - Part 0: " + multipart.getBodyPart(0).getContentType());
            System.out.println("  - Part 1: " + multipart.getBodyPart(1).getContentType());
            
            // Debug: Check if the condition will be met
            BodyPart testPart = multipart.getBodyPart(1);
            System.out.println("\nDebug Part 1:");
            System.out.println("  - Content Type: " + testPart.getContentType());
            System.out.println("  - Starts with APPLICATION/PDF: " + testPart.getContentType().startsWith("APPLICATION/PDF"));
            System.out.println("  - Is MIME text/html: " + testPart.isMimeType("text/html"));
            
            // Create mock RefData
            RefData refData = new RefData();
            refData.setDescription("South Kingsville Rental Statement");
            
            System.out.println("\n=== Processing message ===\n");
            
            // Execute the processor
            processor.execute(message, refData);
            
            System.out.println("\n=== Test completed successfully! ===");
            
        } catch (Exception e) {
            System.err.println("\n❌ Test failed with exception:");
            e.printStackTrace();
        }
    }
    
    // Helper method to set private fields using reflection
    private static void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = getField(target.getClass(), fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
    
    // Helper to find field in class hierarchy
    private static Field getField(Class<?> clazz, String fieldName) throws NoSuchFieldException {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            Class<?> superClass = clazz.getSuperclass();
            if (superClass == null) {
                throw e;
            }
            return getField(superClass, fieldName);
        }
    }
    
    // Mock services
    static class MockNotificationService extends NotificationService {
        @Override
        public Notification create(Notification notification) {
            System.out.println("📧 Notification created: " + notification.getMessage());
            return notification;
        }
    }
    
    static class MockRentalPaymentService extends RentalPaymentService {
        public MockRentalPaymentService() {
            super("docs", null, null);
        }
        
        @Override
        public RentalPayment createRentalPayment(RentalPayment rentalPayment) {
            System.out.println("\n💰 Rental Payment Created:");
            System.out.println("  - Property: " + rentalPayment.getProperty());
            System.out.println("  - Total Rent: $" + rentalPayment.getTotalRent());
            System.out.println("  - Management Fee: $" + rentalPayment.getManagementFee());
            System.out.println("  - Admin Fee: $" + rentalPayment.getAdminFee());
            System.out.println("  - Payment to Owner: $" + rentalPayment.getPaymentToOwner());
            System.out.println("  - Statement From: " + rentalPayment.getStatementFrom());
            System.out.println("  - Statement To: " + rentalPayment.getStatementTo());
            return rentalPayment;
        }
    }
    
    static class MockDocumentService extends DocumentService {
        @Override
        public Document createDocumentForRentalStatement(byte[] file, String fileName, 
                String folderPath, Map<String, Object> metaData) {
            System.out.println("\n📄 Document Created:");
            System.out.println("  - File Name: " + fileName);
            System.out.println("  - Folder Path: " + folderPath);
            System.out.println("  - Metadata: " + metaData);
            System.out.println("  - File Size: " + file.length + " bytes");
            
            Document doc = new Document();
            doc.setFileName(fileName);
            doc.setFolderPath(folderPath);
            return doc;
        }
    }
}
