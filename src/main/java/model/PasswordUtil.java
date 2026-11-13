package model;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Guarda e confere senhas SEM gravar a senha em texto puro.
 *
 * Usa PBKDF2 com HMAC-SHA256, que já vem no Java (nenhuma biblioteca extra):
 *  - um "sal" aleatório por senha (duas pessoas com a mesma senha têm hashes diferentes);
 *  - centenas de milhares de repetições, o que deixa o ataque de força bruta lento.
 *
 * Formato gravado na coluna users.password_hash:
 *   pbkdf2_sha256$<repetições>$<sal em Base64>$<hash em Base64>
 */
public final class PasswordUtil {

	private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
	private static final String PREFIX = "pbkdf2_sha256";
	private static final int ITERATIONS = 600000;
	private static final int SALT_BYTES = 16;
	private static final int KEY_BITS = 256;

	// Hash de exemplo, usado só para "gastar o mesmo tempo" quando o e-mail
	// digitado não existe (assim quem ataca não descobre quais e-mails existem
	// medindo o tempo de resposta).
	private static final String DUMMY_HASH = "pbkdf2_sha256$600000$TAiz/HWCz1OFoFhzkuTC/g==$R9/f5AZXa98OzSyI0ZppOz8YTYx2iPjx7TiaPKGfDis=";

	private PasswordUtil() {
	}

	/** Gera o texto que vai para o banco. */
	public static String hash(String password) {
		byte[] salt = new byte[SALT_BYTES];
		new SecureRandom().nextBytes(salt);

		byte[] hash = derive(password, salt, ITERATIONS, KEY_BITS);

		return PREFIX + "$" + ITERATIONS + "$"
				+ Base64.getEncoder().encodeToString(salt) + "$"
				+ Base64.getEncoder().encodeToString(hash);
	}

	/** Confere a senha digitada contra o texto que está no banco. */
	public static boolean matches(String password, String stored) {
		if (password == null || stored == null)
			return false;

		String[] parts = stored.split("\\$");
		if (parts.length != 4 || !PREFIX.equals(parts[0]))
			return false;

		try {
			int iterations = Integer.parseInt(parts[1]);
			byte[] salt = Base64.getDecoder().decode(parts[2]);
			byte[] expected = Base64.getDecoder().decode(parts[3]);

			byte[] actual = derive(password, salt, iterations, expected.length * 8);

			// comparação em tempo constante (não vaza em qual byte diferiu)
			return MessageDigest.isEqual(expected, actual);
		} catch (IllegalArgumentException e) {
			return false;
		}
	}

	/** Faz o mesmo trabalho de uma conferência real, sem resultado útil. */
	public static void burnTime(String password) {
		matches(password, DUMMY_HASH);
	}

	private static byte[] derive(String password, byte[] salt, int iterations, int keyBits) {
		try {
			PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, keyBits);
			return SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).getEncoded();
		} catch (GeneralSecurityException e) {
			throw new IllegalStateException("PBKDF2 indisponível nesta JVM", e);
		}
	}
}
