package com.studio.auth.service;

import com.studio.auth.config.AppProperties;
import com.studio.auth.exception.BizException;
import com.studio.auth.model.LoginRequest;
import com.studio.auth.model.RegisterRequest;
import com.studio.auth.model.User;
import com.studio.auth.repository.UserFileRepository;
import com.studio.auth.util.PasswordHasher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

/**
 * 认证业务逻辑：注册 / 登录。
 * 业务规则被破坏时抛出 BizException，由全局异常处理器统一响应。
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserFileRepository userRepository;
    private final AppProperties props;
    private final Pattern usernamePattern;

    public AuthService(UserFileRepository userRepository, AppProperties props) {
        this.userRepository = userRepository;
        this.props = props;
        this.usernamePattern = Pattern.compile(props.getUsername().getPattern());
    }

    /**
     * 注册：空值校验 -> 用户名规则 -> 密码长度 -> 重复检测 -> 造盐散列存储。
     */
    public User register(RegisterRequest request) {
        String username = request.getUsername() == null ? "" : request.getUsername().trim();
        String password = request.getPassword() == null ? "" : request.getPassword();

        if (username.isEmpty()) {
            throw BizException.badRequest("用户名不能为空");
        }
        if (password.isEmpty()) {
            throw BizException.badRequest("密码不能为空");
        }

        if (username.length() < props.getUsername().getMinLength()
                || username.length() > props.getUsername().getMaxLength()) {
            throw BizException.badRequest(String.format(
                    "用户名长度必须在 %d-%d 之间",
                    props.getUsername().getMinLength(),
                    props.getUsername().getMaxLength()));
        }
        if (!usernamePattern.matcher(username).matches()) {
            throw BizException.badRequest("用户名只能包含字母、数字和下划线");
        }

        if (password.length() < props.getPasswordMinLength()) {
            throw BizException.badRequest(String.format(
                    "密码长度不能少于 %d 位", props.getPasswordMinLength()));
        }

        // 重复注册检测
        if (userRepository.existsByUsername(username)) {
            throw BizException.conflict("用户名已存在，请换一个");
        }

        // 散列存储，不保存明文
        String salt = PasswordHasher.generateSalt();
        String hash = PasswordHasher.hash(password, salt);
        User user = new User(username, hash, salt, System.currentTimeMillis());
        userRepository.save(user);

        log.info("注册成功: username={}", username);
        return user;
    }

    /**
     * 登录：校验用户名密码。
     * 用户不存在与密码错误返回相同提示，避免泄露账号是否存在。
     */
    public User login(LoginRequest request) {
        String username = request.getUsername() == null ? "" : request.getUsername().trim();
        String password = request.getPassword() == null ? "" : request.getPassword();

        if (username.isEmpty()) {
            throw BizException.badRequest("用户名不能为空");
        }
        if (password.isEmpty()) {
            throw BizException.badRequest("密码不能为空");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> BizException.unauthorized("用户名或密码错误"));

        if (!PasswordHasher.verify(password, user.getSalt(), user.getPasswordHash())) {
            log.warn("登录失败（密码错误）: username={}", username);
            throw BizException.unauthorized("用户名或密码错误");
        }

        log.info("登录成功: username={}", username);
        return user;
    }
}
