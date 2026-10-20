package vn.edu.ictu.qlkh.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

public final class PasswordUtil {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 120_000;
    private static final int SALT_LENGTH = 16;
    private static final int KEY_LENGTH = 256;

    private PasswordUtil() {
    }

    public static String hashPassword(String password) {
        byte[] salt = new byte[SALT_LENGTH];
        new SecureRandom().nextBytes(salt);

        byte[] hash = pbkdf2(
                password.toCharArray(),
                salt,
                ITERATIONS,
                KEY_LENGTH
        );

        return "pbkdf2$"
                + ITERATIONS + "$"
                + Base64.getEncoder().encodeToString(salt) + "$"
                + Base64.getEncoder().encodeToString(hash);
    }

    public static boolean verifyPassword(String password, String encodedHash) {
        if (password == null || encodedHash == null) {
            return false;
        }

        try {
            String[] parts = encodedHash.split("\\$");

            if (parts.length != 4 || !"pbkdf2".equals(parts[0])) {
                return false;
            }

            int iterations = Integer.parseInt(parts[1]);
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[3]);

            byte[] actualHash = pbkdf2(
                    password.toCharArray(),
                    salt,
                    iterations,
                    expectedHash.length * 8
            );

            return MessageDigest.isEqual(expectedHash, actualHash);

        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static byte[] pbkdf2(
            char[] password,
            byte[] salt,
            int iterations,
            int keyLength) {

        PBEKeySpec spec =
                new PBEKeySpec(password, salt, iterations, keyLength);

        try {
            SecretKeyFactory factory =
                    SecretKeyFactory.getInstance(ALGORITHM);

            return factory.generateSecret(spec).getEncoded();

        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException(
                    "Không thể băm mật khẩu.",
                    e
            );
        } finally {
            spec.clearPassword();
        }
    }
}