package cn.dreamtof.common.util;

import cn.dreamtof.common.api.vo.BilibiliUserInfoVO;
import cn.dreamtof.core.config.VirtualTaskManager;
import cn.dreamtof.core.utils.JsonUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.Cookie;
import com.microsoft.playwright.options.WaitUntilState;
import jakarta.annotation.PreDestroy;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Slf4j
@Component
public class PlaywrightUtil {

    // ==================== UA 指纹池 ====================
    private static final String[] USER_AGENTS = {
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36 Edg/124.0.0.0"
    };

    // ==================== 视口池 ====================
    private static final int[][] VIEWPORTS = {
            {1920, 1080}, {1920, 1080},
            {1536, 864},
            {1440, 900},
            {1366, 768}
    };

    // ==================== 限流器（日均 2000，允许突发 5 连） ====================
    private static final long DAILY_LIMIT = 2000;
    private static final long MS_PER_DAY = 86400000L;
    private static final long REFILL_MS = MS_PER_DAY / DAILY_LIMIT;
    private static final long BURST_MAX = 10;
    private final AtomicLong tokens = new AtomicLong(BURST_MAX);
    private volatile long lastRefillTime = System.currentTimeMillis();

    // ==================== 工作队列 (容量 100，满则丢弃) ====================
    private static final int QUEUE_CAPACITY = 100;
    private static final BlockingQueue<QueuedTask> WORK_QUEUE = new LinkedBlockingQueue<>(QUEUE_CAPACITY);
    private static volatile boolean consumerRunning = false;

    @Getter
    @AllArgsConstructor
    public static class QueuedTask {
        private final String id;
        private final Runnable action;
    }

    // ==================== Cookie 持久化路径 ====================
    private static final Path COOKIE_FILE = Paths.get(
            System.getProperty("java.io.tmpdir"), "bilibili_cookies.json"
    );

    // ==================== Stealth JS ====================
    private static final String STEALTH_SCRIPT = """
            () => {
                Object.defineProperty(navigator, 'webdriver', { get: () => false });
                Object.defineProperty(navigator, 'plugins', {
                    get: () => [
                        { name: 'Chrome PDF Plugin', filename: 'internal-pdf-viewer' },
                        { name: 'Chrome PDF Viewer', filename: 'mhjfbmdgcfjbbpaeojofohoefgiehjai' },
                        { name: 'Native Client', filename: 'internal-nacl-plugin' },
                        { name: 'Widevine Content Decryption Module', filename: 'widevinecdm' }
                    ]
                });
                Object.defineProperty(navigator, 'mimeTypes', { get: () => [] });
                Object.defineProperty(navigator, 'languages', { get: () => ['zh-CN', 'zh', 'en'] });
                Object.defineProperty(navigator, 'platform', { get: () => 'Win32' });
                Object.defineProperty(navigator, 'hardwareConcurrency', { get: () => 8 });
                Object.defineProperty(navigator, 'deviceMemory', { get: () => 8 });
                if (window.chrome) {
                    window.chrome.runtime = {
                        id: 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa',
                        onInstalled: { addListener: () => {} },
                        onStartup: { addListener: () => {} },
                        onMessage: { addListener: () => {} },
                        sendMessage: () => {}
                    };
                }
                const origQuery = navigator.permissions.query.bind(navigator.permissions);
                navigator.permissions.query = (p) => (
                    ['geolocation', 'notifications', 'microphone', 'camera'].includes(p.name)
                        ? Promise.resolve({ state: 'denied', onchange: null })
                        : origQuery(p)
                );
                const proto = WebGLRenderingContext.prototype;
                const origGetParam = proto.getParameter;
                proto.getParameter = function(p) {
                    if (p === 37445) return 'Intel Inc.';
                    if (p === 37446) return 'Intel Iris OpenGL Engine';
                    return origGetParam.call(this, p);
                };
                Object.defineProperty(screen, 'colorDepth', { get: () => 24 });
                Object.defineProperty(screen, 'pixelDepth', { get: () => 24 });
                delete window.cdc_adoQpoasnfa76pfcZLmcfl_Array;
                delete window.cdc_adoQpoasnfa76pfcZLmcfl_Promise;
                delete window.cdc_adoQpoasnfa76pfcZLmcfl_Symbol;
            }
            """;

    private final Playwright playwright;
    private final Browser browser;
    private final Random random = new Random();

    private List<Cookie> cachedCookies;

    public PlaywrightUtil() {
        this.playwright = Playwright.create();
        this.browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions()
                        .setHeadless(true)
                        .setArgs(List.of(
                                "--no-sandbox",
                                "--disable-setuid-sandbox",
                                "--disable-dev-shm-usage",
                                "--disable-gpu",
                                "--disable-blink-features=AutomationControlled"
                        ))
        );
        loadCookiesFromDisk();
        startQueueConsumer();
        log.info("[PlaywrightUtil] 初始化完成，每日限流 {} 次，队列容量 {}", DAILY_LIMIT, QUEUE_CAPACITY);
    }

    // ===================================================================
    //  工作队列方法
    // ===================================================================

    public static boolean enqueue(String id, Runnable task) {
        if (id == null || task == null) return false;
        QueuedTask qt = new QueuedTask(id, task);
        boolean accepted = WORK_QUEUE.offer(qt);
        if (!accepted) {
            log.warn("[PlaywrightUtil] 队列已满 ({}), 丢弃任务: {}", QUEUE_CAPACITY, id);
        } else {
            log.debug("[PlaywrightUtil] 任务入队成功: {}, 当前队列大小: {}", id, WORK_QUEUE.size());
        }
        return accepted;
    }

    public static CompletableFuture<Boolean> enqueueAsync(String id, Runnable task) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        VirtualTaskManager.execute(() -> {
            try {
                boolean accepted = enqueue(id, task);
                future.complete(accepted);
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    public static String pollId() {
        QueuedTask task = WORK_QUEUE.poll();
        return task != null ? task.getId() : null;
    }

    public static List<String> getQueuedIds() {
        return WORK_QUEUE.stream()
                .map(QueuedTask::getId)
                .collect(Collectors.toList());
    }

    public static int getQueueSize() {
        return WORK_QUEUE.size();
    }

    public static int getQueueRemainingCapacity() {
        return WORK_QUEUE.remainingCapacity();
    }

    // ==================== 队列消费者守护线程 ====================

    private synchronized void startQueueConsumer() {
        if (consumerRunning) return;
        consumerRunning = true;

        Thread consumer = Thread.ofVirtual()
                .name("pw-queue-consumer")
                .unstarted(() -> {
                    log.info("[PlaywrightUtil] 队列消费者守护线程已启动");
                    while (consumerRunning && !Thread.currentThread().isInterrupted()) {
                        try {
                            QueuedTask task = WORK_QUEUE.poll(10, TimeUnit.SECONDS);
                            if (task != null) {
                                log.debug("[PlaywrightUtil] 消费者取出任务: {}", task.getId());
                                VirtualTaskManager.execute(task.getAction());
                            }
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            break;
                        } catch (Exception e) {
                            log.error("[PlaywrightUtil] 消费者执行任务异常", e);
                        }
                    }
                    log.info("[PlaywrightUtil] 队列消费者守护线程已退出");
                });

        consumer.start();
    }

    // ===================================================================
    //  公开入口
    // ===================================================================

    public BilibiliUserInfoVO fetchBilibiliUserInfo(String uid) {
        acquireToken();

        log.info("[PlaywrightUtil] 开始抓取哔哩哔哩用户信息，UID: {}", uid);

        String personalSpaceUrl = "https://space.bilibili.com/" + uid;
        String username = null;
        String avatar = null;
        String liveRoomUrl = null;

        String ua = randomUA();
        int[] vp = randomViewport();

        try (BrowserContext context = browser.newContext(
                new Browser.NewContextOptions()
                        .setUserAgent(ua)
                        .setViewportSize(vp[0], vp[1])
                        .setExtraHTTPHeaders(Map.of(
                                "Accept-Language", "zh-CN,zh;q=0.9",
                                "Accept-Encoding", "gzip, deflate, br",
                                "Sec-Ch-Ua", "\"Chromium\";v=\"124\", \"Google Chrome\";v=\"124\"",
                                "Sec-Ch-Ua-Mobile", "?0",
                                "Sec-Ch-Ua-Platform", "\"Windows\""
                        ))
        )) {
            restoreCookies(context);

            Page page = context.newPage();

            page.addInitScript(STEALTH_SCRIPT);

            page.route("**/*.{png,jpg,jpeg,gif,webp,css,woff,woff2,svg,mp4}", Route::abort);

            warmup(page);

            navigateWithRetry(page, "https://space.bilibili.com/" + uid);

            simulateHumanBehavior(page);

            username = extractUsernameFromPage(page);
            avatar = extractAvatarFromPage(page);

            if (username == null || username.isEmpty() || avatar == null) {
                log.info("[PlaywrightUtil] 页面元素提取失败，尝试调用内部接口...");
                BilibiliUserInfoVO apiResult = fetchAvatarFromApi(page, uid);
                if (apiResult != null) {
                    if (username == null || username.isEmpty()) {
                        username = apiResult.getUsername();
                    }
                    if (avatar == null) {
                        avatar = apiResult.getAvatar();
                    }
                }
            }

            liveRoomUrl = fetchLiveRoomInfo(page, uid);

            cacheAndSaveCookies(context);

            log.info("[PlaywrightUtil] 抓取完成，UID: {}, 结果: {}", uid, username);

        } catch (Exception e) {
            log.error("[PlaywrightUtil] 抓取失败，UID: {}", uid, e);
            throw new RuntimeException("抓取失败：" + e.getMessage());
        }

        return BilibiliUserInfoVO.builder()
                .username(username)
                .personalSpaceUrl(personalSpaceUrl)
                .avatar(avatar)
                .liveRoomUrl(liveRoomUrl)
                .build();
    }

    // ===================================================================
    //  限流器
    // ===================================================================

    private void acquireToken() {
        refillTokens();
        while (tokens.get() <= 0) {
            refillTokens();
            if (tokens.get() <= 0) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        tokens.decrementAndGet();
    }

    private void refillTokens() {
        long now = System.currentTimeMillis();
        long elapsed = now - lastRefillTime;
        long earned = elapsed / REFILL_MS;
        if (earned > 0) {
            tokens.updateAndGet(cur -> Math.min(BURST_MAX, cur + earned));
            lastRefillTime += earned * REFILL_MS;
        }
    }

    // ===================================================================
    //  指纹随机化
    // ===================================================================

    private String randomUA() {
        return USER_AGENTS[random.nextInt(USER_AGENTS.length)];
    }

    private int[] randomViewport() {
        int[] vp = VIEWPORTS[random.nextInt(VIEWPORTS.length)];
        return new int[]{vp[0] + random.nextInt(5) - 2, vp[1] + random.nextInt(5) - 2};
    }

    // ===================================================================
    //  Cookie 持久化
    // ===================================================================

    private void restoreCookies(BrowserContext context) {
        if (cachedCookies == null || cachedCookies.isEmpty()) return;
        long now = Instant.now().getEpochSecond();
        List<Cookie> valid = new ArrayList<>();
        for (Cookie c : cachedCookies) {
            Double exp = c.expires;
            if (exp == null || exp > now) {
                valid.add(c);
            }
        }
        if (!valid.isEmpty()) {
            context.addCookies(valid);
            log.info("[PlaywrightUtil] 恢复 {} 个有效 Cookie", valid.size());
        }
    }

    private void cacheAndSaveCookies(BrowserContext context) {
        try {
            cachedCookies = context.cookies();
            String json = JsonUtils.MAPPER.writeValueAsString(cachedCookies);
            Files.writeString(COOKIE_FILE, json);
            log.info("[PlaywrightUtil] 缓存 {} 个 Cookie → {}", cachedCookies.size(), COOKIE_FILE);
        } catch (IOException e) {
            log.warn("[PlaywrightUtil] 保存 Cookie 失败: {}", e.getMessage());
        }
    }

    private void loadCookiesFromDisk() {
        try {
            if (Files.exists(COOKIE_FILE)) {
                String json = Files.readString(COOKIE_FILE);
                if (json.isBlank()) return;
                var arr = JsonUtils.MAPPER.readTree(json);
                if (arr.isArray() && arr.isEmpty()) return;
                log.info("[PlaywrightUtil] 磁盘 Cookie 文件存在，{} 条待验证", arr.size());
            }
        } catch (IOException e) {
            log.debug("[PlaywrightUtil] 读取磁盘 Cookie 失败: {}", e.getMessage());
        }
    }

    // ===================================================================
    //  预热与导航
    // ===================================================================

    private void warmup(Page page) {
        try {
            page.navigate("https://www.bilibili.com/", new Page.NavigateOptions()
                    .setWaitUntil(WaitUntilState.DOMCONTENTLOADED)
                    .setTimeout(8000));
            page.waitForTimeout(500 + random.nextInt(1000));
        } catch (Exception e) {
            log.warn("[PlaywrightUtil] 预热主页超时，跳过...");
        }
    }

    private void navigateWithRetry(Page page, String url) {
        for (int i = 0; i < 3; i++) {
            try {
                page.navigate(url, new Page.NavigateOptions()
                        .setWaitUntil(WaitUntilState.NETWORKIDLE)
                        .setTimeout(15000));
            } catch (Exception e) {
                log.warn("[PlaywrightUtil] 导航超时 (第{}次)，重试...", i + 1);
                continue;
            }

            if (isBlocked(page)) {
                log.warn("[PlaywrightUtil] 触发 412 (第{}次)，指数退避等待...", i + 1);
                boolean passed = false;
                for (int j = 0; j < 3; j++) {
                    long waitMs = 3000L * (1 << j);
                    page.waitForTimeout(waitMs);
                    try {
                        page.reload(new Page.ReloadOptions()
                                .setWaitUntil(WaitUntilState.NETWORKIDLE)
                                .setTimeout(20000));
                        if (!isBlocked(page)) {
                            passed = true;
                            break;
                        }
                    } catch (Exception ignored) {
                    }
                }
                if (passed) {
                    log.info("[PlaywrightUtil] 412 挑战通过");
                    return;
                }
                continue;
            }

            return;
        }

        log.warn("[PlaywrightUtil] 重试耗尽，最后一次硬等后重载...");
        page.waitForTimeout(10000);
        try {
            page.reload(new Page.ReloadOptions().setWaitUntil(WaitUntilState.NETWORKIDLE).setTimeout(20000));
        } catch (Exception ignored) {
        }
    }

    private boolean isBlocked(Page page) {
        try {
            String title = page.title();
            String content = page.content();
            return title.contains("412")
                    || title.contains("验证码")
                    || content.contains("错误号: 412")
                    || content.contains("风控");
        } catch (Exception e) {
            return true;
        }
    }

    // ===================================================================
    //  人类行为模拟
    // ===================================================================

    private void simulateHumanBehavior(Page page) {
        try {
            page.evaluate("() => { window.scrollTo(0, Math.random() * 500); }");
            page.waitForTimeout(200 + random.nextInt(800));
            page.evaluate("() => { window.scrollTo(0, Math.random() * 200); }");
            page.waitForTimeout(100 + random.nextInt(400));
        } catch (Exception ignored) {
        }
    }

    // ===================================================================
    //  页面提取
    // ===================================================================

    private String extractAvatarFromPage(Page page) {
        try {
            Object avatar = page.evaluate("""
                    () => {
                        const img = document.querySelector('.face-container img, img[user-avatar], .h-avatar img');
                        return img ? img.src : null;
                    }
                    """);
            return avatar != null ? avatar.toString() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private String extractUsernameFromPage(Page page) {
        try {
            Object username = page.evaluate("""
                    () => {
                        const el = document.querySelector('.user-name, .nickname, #h-name');
                        return el ? el.textContent.trim() : null;
                    }
                    """);
            return username != null ? username.toString() : null;
        } catch (Exception e) {
            return null;
        }
    }

    // ===================================================================
    //  API 兜底
    // ===================================================================

    private BilibiliUserInfoVO fetchAvatarFromApi(Page page, String uid) {
        try {
            String apiUrl = "https://api.bilibili.com/x/web-interface/card?mid=" + uid;
            APIResponse response = page.request().get(apiUrl);
            JsonNode root = JsonUtils.MAPPER.readTree(response.text());

            if (root.path("code").asInt() == 0) {
                JsonNode data = root.path("data").path("card");
                String avatar = data.path("face").asText();
                String username = data.path("name").asText();
                return BilibiliUserInfoVO.builder()
                        .username(username)
                        .avatar(avatar)
                        .build();
            }
        } catch (Exception e) {
            log.debug("API 获取头像失败：{}", e.getMessage());
        }
        return null;
    }

    private String fetchLiveRoomInfo(Page page, String uid) {
        try {
            String liveApiUrl = "https://api.live.bilibili.com/room/v1/Room/getRoomInfoOld?mid=" + uid;
            APIResponse response = page.request().get(liveApiUrl);
            JsonNode root = JsonUtils.MAPPER.readTree(response.text());

            if (root.path("code").asInt() == 0) {
                JsonNode data = root.path("data");
                if (data.path("roomStatus").asInt() == 1) {
                    long roomId = data.path("roomid").asLong();
                    return "https://live.bilibili.com/" + roomId;
                }
            }
        } catch (Exception e) {
            log.debug("直播间信息获取失败");
        }
        return null;
    }

    public String fetchRenderedPageContent(String url, String waitSelector, long timeoutMs) {
        acquireToken();
        log.info("[PlaywrightUtil] 开始渲染页面: {}", url);

        String ua = randomUA();
        int[] vp = randomViewport();

        try (BrowserContext context = browser.newContext(
                new Browser.NewContextOptions()
                        .setUserAgent(ua)
                        .setViewportSize(vp[0], vp[1])
                        .setExtraHTTPHeaders(Map.of(
                                "Accept-Language", "zh-CN,zh;q=0.9",
                                "Accept-Encoding", "gzip, deflate, br"
                        ))
        )) {
            Page page = context.newPage();
            page.route("**/*.{png,jpg,jpeg,gif,webp,css,woff,woff2,svg,mp4,mp3}", Route::abort);

            page.navigate(url, new Page.NavigateOptions()
                    .setWaitUntil(WaitUntilState.DOMCONTENTLOADED)
                    .setTimeout(timeoutMs));

            if (waitSelector != null && !waitSelector.isBlank()) {
                page.waitForSelector(waitSelector, new Page.WaitForSelectorOptions()
                        .setTimeout(timeoutMs));
            } else {
                page.waitForTimeout(3000);
            }

            String content = page.content();
            log.info("[PlaywrightUtil] 页面渲染完成，内容长度: {}", content.length());
            return content;

        } catch (Exception e) {
            log.error("[PlaywrightUtil] 页面渲染失败: {}", url, e);
            throw new RuntimeException("页面渲染失败：" + e.getMessage());
        }
    }

    public String evaluateJsOnPage(String url, String jsExpression, String waitSelector, long timeoutMs) {
        acquireToken();
        log.info("[PlaywrightUtil] 开始在页面上执行JS: {}", url);

        String ua = randomUA();
        int[] vp = randomViewport();

        try (BrowserContext context = browser.newContext(
                new Browser.NewContextOptions()
                        .setUserAgent(ua)
                        .setViewportSize(vp[0], vp[1])
                        .setExtraHTTPHeaders(Map.of(
                                "Accept-Language", "zh-CN,zh;q=0.9",
                                "Accept-Encoding", "gzip, deflate, br"
                        ))
        )) {
            Page page = context.newPage();
            page.route("**/*.{png,jpg,jpeg,gif,webp,css,woff,woff2,svg,mp4,mp3}", Route::abort);

            page.navigate(url, new Page.NavigateOptions()
                    .setWaitUntil(WaitUntilState.DOMCONTENTLOADED)
                    .setTimeout(timeoutMs));

            if (waitSelector != null && !waitSelector.isBlank()) {
                page.waitForSelector(waitSelector, new Page.WaitForSelectorOptions()
                        .setTimeout(timeoutMs));
            } else {
                page.waitForTimeout(3000);
            }

            Object result = page.evaluate(jsExpression);
            String json = result != null ? result.toString() : null;
            log.info("[PlaywrightUtil] JS执行完成，结果长度: {}", json != null ? json.length() : 0);
            return json;

        } catch (Exception e) {
            log.error("[PlaywrightUtil] 页面JS执行失败: {}", url, e);
            throw new RuntimeException("页面JS执行失败：" + e.getMessage());
        }
    }

    // ===================================================================
    //  生命周期
    // ===================================================================

    @PreDestroy
    public void close() {
        log.info("[PlaywrightUtil] 正在关闭资源...");
        consumerRunning = false;
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
    }
}
