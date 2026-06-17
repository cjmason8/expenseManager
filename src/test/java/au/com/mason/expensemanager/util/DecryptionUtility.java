package au.com.mason.expensemanager.util;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.hc.client5.http.utils.Base64;

public class DecryptionUtility {

	public static String decrypt(String encrypted, String betaKey, String alphaKey) {
		try {
			IvParameterSpec iv = new IvParameterSpec(betaKey.getBytes("UTF-8"));
			SecretKeySpec skeySpec = new SecretKeySpec(alphaKey.getBytes("UTF-8"), "AES");

			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
			cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

			byte[] original = cipher.doFinal(Base64.decodeBase64(encrypted));

			return new String(original);
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	public static void main(String[] args) {
		String betaKey = "CL0112JY1406ESSa";
		String alphaKey = "E66YYu84iW50GE66";

		System.out.println("=== Email Credentials ===");
		System.out.println("Production Environment:");
		String requiredInfoPrd = "S4Eqz7962JO0qeRAfQdevwjQAfUilB86oZnyH0RD4zs=";
		String reqAccountPrd = "icMZGRCb+RbYUKI5RX7HaM33C3mLyertwbUl2RhYdt8=";
		System.out.println("  REQUIRED_INFO (password): " + decrypt(requiredInfoPrd, betaKey, alphaKey));
		System.out.println("  REQ_ACCOUNT (email): " + decrypt(reqAccountPrd, betaKey, alphaKey));

		System.out.println("\nLocal Environment:");
		String requiredInfoLcl = "KfImvSFjTU1LpgQd3F4l5w==";
		String reqAccountLcl = "icMZGRCb+RbYUKI5RX7HaM33C3mLyertwbUl2RhYdt8=";
		System.out.println("  REQUIRED_INFO (password): " + decrypt(requiredInfoLcl, betaKey, alphaKey));
		System.out.println("  REQ_ACCOUNT (email): " + decrypt(reqAccountLcl, betaKey, alphaKey));

		System.out.println("\n=== Database Passwords ===");
		System.out.println("Production Environment:");
		String dbPassPrd = "29XCSdu61eixtdTi2WIkiQ==";
		System.out.println("  DB_PASS: " + decrypt(dbPassPrd, betaKey, alphaKey));

		System.out.println("\nLocal Environment:");
		String dbPassLcl = "KfImvSFjTU1LpgQd3F4l5w==";
		System.out.println("  DB_PASS: " + decrypt(dbPassLcl, betaKey, alphaKey));
	}
}
