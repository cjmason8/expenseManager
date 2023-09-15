package au.com.mason.expensemanager.service;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.apache.hc.client5.http.utils.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@PropertySource("file:${resources.dir}/required")
public class EncryptionService {
	
	@Value("${beta.key}")
	private String betaKey;
	
	@Value("${alpha.vec}")
	private String alphaKey;
	
    public String encrypt(String value) {
        try {
            IvParameterSpec iv = new IvParameterSpec(betaKey.getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(alphaKey.getBytes("UTF-8"), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

            byte[] encrypted = cipher.doFinal(value.getBytes());

            return Base64.encodeBase64String(encrypted);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public String decrypt(String encrypted) {
        try {
            IvParameterSpec iv = new IvParameterSpec(betaKey.getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(alphaKey.getBytes("UTF-8"), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

            byte[] original = cipher.doFinal(Base64.decodeBase64(encrypted));

            return new String(original);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }
    
    public static void main(String[] args) {
		System.out.println(new EncryptionService().decrypt("password"));
	}

}
