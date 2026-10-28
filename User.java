package com.studio.auth.model;

/**
 * 用户实体。密码以散列值 + 盐 的形式存储，不保存明文。
 */
public class User {

    private String username;

    /** 密码散列值（PBKDF2），禁止外发 */
    private String passwordHash;

    /** 每用户随机盐，与散列一同持久化 */
    private String salt;

    /** 注册时间戳（毫秒） */
    private long createdAt;

    public User() {
    }

    public User(String username, String passwordHash, String salt, long createdAt) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.createdAt = createdAt;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
