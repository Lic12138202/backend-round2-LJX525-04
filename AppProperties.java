package com.studio.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 将 application.yml 中 app.* 配置绑定到类型安全对象。
 * 规则外置：修改配置并重启即可调整行为，无需改 Java 代码。
 */
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    /** 用户数据文件路径 */
    private String userFile = "data/users.json";

    /** 密码最小长度 */
    private int passwordMinLength = 6;

    /** 用户名规则 */
    private Username username = new Username();

    public String getUserFile() {
        return userFile;
    }

    public void setUserFile(String userFile) {
        this.userFile = userFile;
    }

    public int getPasswordMinLength() {
        return passwordMinLength;
    }

    public void setPasswordMinLength(int passwordMinLength) {
        this.passwordMinLength = passwordMinLength;
    }

    public Username getUsername() {
        return username;
    }

    public void setUsername(Username username) {
        this.username = username;
    }

    /** 对应 yml 中 app.username.* */
    public static class Username {
        private String pattern = "^[a-zA-Z0-9_]{3,20}$";
        private int minLength = 3;
        private int maxLength = 20;

        public String getPattern() {
            return pattern;
        }

        public void setPattern(String pattern) {
            this.pattern = pattern;
        }

        public int getMinLength() {
            return minLength;
        }

        public void setMinLength(int minLength) {
            this.minLength = minLength;
        }

        public int getMaxLength() {
            return maxLength;
        }

        public void setMaxLength(int maxLength) {
            this.maxLength = maxLength;
        }
    }
}
