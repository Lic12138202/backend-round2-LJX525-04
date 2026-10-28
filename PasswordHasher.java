package com.studio.auth.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 密码散列工具：PBKDF2WithHmacSHA256 + 随机盐 + 10 万次迭代。
 *
 * 设计要点：
 * 1. 使用单向散列而非加密，密码只需验证、无需还原；
 * 2. 每用户独立随机盐，相同密码产生不同散列，防止彩虹表批量破解；
 * 3. 较高迭代次数提高暴力破解成本；
 * 4. 校验时使用常量时间比较，降低计时攻击风险。
 */
public final class PasswordHasher {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 100_000;
    private static final int SALT_BYTES = 16;
    private static final int KEY_BITS = 256;

    private PasswordHasher() {
    }

    /** 生成随机盐（Base64 编码，便于存储） */
    public static String generateSalt() {
        byte[] salt = new byte[SALT_BYTES];
        new SecureRandom().nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * 计算密码散列。
     *
     * @param password   明文密码，仅在内存中短暂存在
     * @param saltBase64 该用户盐
     * @return Base64 编码的散列值
     */
    public static String hash(String password, String saltBase64) {
        try {
            byte[] salt = Base64.getDecoder().decode(saltBase64);
            PBEKeySpec spec = new PBEKeySpec(
                    password.toCharArray(), salt, ITERATIONS, KEY_BITS);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            byte[] hash = factory.generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("密码散列失败", e);
        }
    }

    /** 校验密码：重算散列后与存档值做常量时间比较 */
    public static boolean verify(String password, String saltBase64, String expectedHash) {
        String actual = hash(password, saltBase64);
        return MessageDigest.isEqual(
                actual.getBytes(StandardCharsets.UTF_8),
                expectedHash.getBytes(StandardCharsets.UTF_8));
    }
}
