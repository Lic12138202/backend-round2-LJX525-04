package com.studio.auth.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studio.auth.config.AppProperties;
import com.studio.auth.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 用户数据仓库：以 JSON 文件持久化（内存 Map 缓存 + 落盘）。
 * 文件路径来自 application.yml，便于更换存储位置。
 */
@Repository
public class UserFileRepository {

    private static final Logger log = LoggerFactory.getLogger(UserFileRepository.class);

    private final Path filePath;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, User> users = new LinkedHashMap<>();

    public UserFileRepository(AppProperties props) {
        this.filePath = Paths.get(props.getUserFile());
    }

    /** 启动时将文件加载进内存缓存 */
    @PostConstruct
    public void init() {
        try {
            if (Files.exists(filePath)) {
                String json = Files.readString(filePath);
                if (json != null && !json.isBlank()) {
                    Map<String, User> loaded = objectMapper.readValue(
                            json, new TypeReference<LinkedHashMap<String, User>>() {});
                    users.putAll(loaded);
                }
            } else if (filePath.getParent() != null) {
                Files.createDirectories(filePath.getParent());
            }
            log.info("已加载用户数据: {} 条, 文件={}", users.size(), filePath.toAbsolutePath());
        } catch (IOException e) {
            throw new IllegalStateException("加载用户文件失败: " + filePath, e);
        }
    }

    public boolean existsByUsername(String username) {
        return users.containsKey(username);
    }

    public Optional<User> findByUsername(String username) {
        return Optional.ofNullable(users.get(username));
    }

    public void save(User user) {
        users.put(user.getUsername(), user);
        flush();
    }

    /** 将内存数据写回 JSON 文件 */
    private synchronized void flush() {
        try {
            if (filePath.getParent() != null) {
                Files.createDirectories(filePath.getParent());
            }
            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(filePath.toFile(), users);
        } catch (IOException e) {
            throw new IllegalStateException("写入用户文件失败: " + filePath, e);
        }
    }

    public int count() {
        return users.size();
    }

    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }
}
