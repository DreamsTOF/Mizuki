package cn.dreamtof.media.api.controller;

import cn.dreamtof.core.base.BaseResponse;
import cn.dreamtof.core.utils.ResultUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * 音乐代理控制器
 * <p>
 * 为前端 Meting API 请求提供后端代理，避免跨域问题并支持本地缓存。
 * </p>
 */
@Tag(name = "媒体/音乐代理")
@Slf4j
@RestController
@RequestMapping("/media/music")
@RequiredArgsConstructor
public class MusicMetingController {

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();

    private final Cache<String, String> metingCache = CacheBuilder.newBuilder()
            .maximumSize(50)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();

    @GetMapping("meting")
    @Operation(summary = "Meting API 代理")
    public BaseResponse<String> proxyMeting(
            @RequestParam(defaultValue = "netease") String server,
            @RequestParam(defaultValue = "playlist") String type,
            @RequestParam(defaultValue = "14164869977") String id,
            @RequestParam(required = false) String auth) {

        String cacheKey = server + ":" + type + ":" + id;
        String cached = metingCache.getIfPresent(cacheKey);
        if (cached != null) {
            log.debug("Meting cache hit: {}", cacheKey);
            return ResultUtils.success(cached);
        }

        String apiUrl = "https://meting.mysqil.com/api?server=" + server
                + "&type=" + type
                + "&id=" + id
                + "&auth=" + (auth != null ? auth : "")
                + "&r=" + System.currentTimeMillis();

        log.info("Proxying Meting API request: server={}, type={}, id={}", server, type, id);

        try {
            Request request = new Request.Builder()
                    .url(apiUrl)
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .header("Referer", "https://music.163.com")
                    .get()
                    .build();
            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful() || response.body() == null) {
                    log.warn("Meting API request failed: {}", response.code());
                    return ResultUtils.error("Meting API 请求失败: " + response.code());
                }
                String json = response.body().string();
                metingCache.put(cacheKey, json);
                return ResultUtils.success(json);
            }
        } catch (Exception e) {
            log.error("Meting API 代理请求异常: server={}, type={}, id={}", server, type, id, e);
            return ResultUtils.error("Meting API 代理请求异常: " + e.getMessage());
        }
    }
}
