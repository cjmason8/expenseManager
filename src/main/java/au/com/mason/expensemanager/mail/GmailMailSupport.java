package au.com.mason.expensemanager.mail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import jakarta.mail.Flags;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Store;
import jakarta.mail.search.FlagTerm;

public final class GmailMailSupport {

	private GmailMailSupport() {
	}

	public static Optional<String> findLabelFolder(Store store, String labelName) throws MessagingException {
		Folder direct = store.getFolder(labelName);
		if (direct.exists()) {
			return Optional.of(labelName);
		}

		String normalizedTarget = normalizeLabel(labelName);
		List<String> matches = new ArrayList<>();
		collectLabelMatches(store.getDefaultFolder(), normalizedTarget, matches);
		if (matches.size() == 1) {
			return Optional.of(matches.get(0));
		}
		if (matches.size() > 1) {
			return matches.stream().filter(name -> normalizeLabel(name).equals(normalizedTarget)).findFirst()
				.or(() -> Optional.of(matches.get(0)));
		}
		return Optional.empty();
	}

	public static List<String> listLabelFolderNames(Store store) throws MessagingException {
		List<String> labels = new ArrayList<>();
		collectAllFolders(store.getDefaultFolder(), labels);
		return labels.stream().filter(name -> !name.startsWith("[Gmail]/") && !name.equals("INBOX")
			&& !name.equals("[Gmail]") && !name.equals("Drafts")).sorted().toList();
	}

	public static Message[] fetchUnreadFromAllMailWithLabel(Folder allMailFolder, String labelName)
		throws MessagingException {
		Message[] unread = allMailFolder.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
		List<Message> matches = new ArrayList<>();
		for (Message message : unread) {
			if (hasGmailLabel(message, labelName)) {
				matches.add(message);
			}
		}
		return matches.toArray(Message[]::new);
	}

	public static Message[] fetchUnreadFromAllMailWithLabel(Store store, String labelName) throws MessagingException {
		Folder allMail = store.getFolder("[Gmail]/All Mail");
		if (!allMail.exists()) {
			return new Message[0];
		}

		allMail.open(Folder.READ_ONLY);
		try {
			return fetchUnreadFromAllMailWithLabel(allMail, labelName);
		} finally {
			allMail.close(false);
		}
	}

	public static boolean hasGmailLabel(Message message, String labelName) throws MessagingException {
		String[] labels = message.getHeader("X-GM-LABELS");
		if (labels == null) {
			return false;
		}
		String normalizedTarget = normalizeLabel(labelName);
		return Arrays.stream(labels).flatMap(value -> Arrays.stream(value.split(","))).map(GmailMailSupport::normalizeLabel)
			.anyMatch(label -> label.equals(normalizedTarget));
	}

	private static void collectLabelMatches(Folder folder, String normalizedTarget, List<String> matches)
		throws MessagingException {
		for (Folder child : folder.list()) {
			String fullName = child.getFullName();
			if (child.exists() && (normalizeLabel(fullName).endsWith("/" + normalizedTarget)
				|| normalizeLabel(fullName).equals(normalizedTarget))) {
				matches.add(fullName);
			}
			if (fullName.split("/").length < 4) {
				collectLabelMatches(child, normalizedTarget, matches);
			}
		}
	}

	private static void collectAllFolders(Folder folder, List<String> names) throws MessagingException {
		for (Folder child : folder.list()) {
			if (child.exists()) {
				names.add(child.getFullName());
			}
			if (child.getFullName().split("/").length < 4) {
				collectAllFolders(child, names);
			}
		}
	}

	private static String normalizeLabel(String value) {
		if (value == null) {
			return "";
		}
		return value.trim().replaceAll("^\"|\"$", "").toLowerCase(Locale.ROOT);
	}

}
