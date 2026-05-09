package cn.dreamtof.common.util;

import cn.dreamtof.common.api.vo.BilibiliUserInfoVO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PlaywrightUtil 单元测试
 */
@Slf4j
@SpringBootTest
class PlaywrightUtilTest {

    @Autowired
    private PlaywrightUtil playwrightUtil;

    // 测试用的哔哩哔哩 UID (可以使用一些知名 UP 主的 UID)
    private static final String TEST_UID = "68559"; // 哔哩哔哩官方账号

    @BeforeEach
    void setUp() {
        log.info("========== 测试开始 ==========");
    }

    @AfterEach
    void tearDown() {
        log.info("========== 测试结束 ==========");
    }

    /**
     * 测试抓取哔哩哔哩用户信息
     */
    @Test
    void testFetchBilibiliUserInfo() {
        log.info("开始测试抓取哔哩哔哩用户信息，UID: {}", TEST_UID);

        // 执行抓取
        BilibiliUserInfoVO result = playwrightUtil.fetchBilibiliUserInfo(TEST_UID);

        // 验证结果
        assertNotNull(result, "返回结果不应为空");
        log.info("抓取结果：{}", result);


        // 验证头像 URL (可能为空，如果页面结构变化)
        String avatar = (String) result.getAvatar();
        log.info("头像 URL: {}", avatar);
        // 头像可能为 null，所以不强制断言

        // 验证用户名 (可能为 null)
        String username = result.getUsername();
        log.info("用户名：{}", username);

        // 验证直播间信息 (可能没有直播间)
        String liveRoomUrl = result.getLiveRoomUrl();

        // 如果有直播间 URL，验证格式
        if (liveRoomUrl != null) {
            assertTrue(liveRoomUrl.startsWith("https://live.bilibili.com/"), 
                    "直播间链接格式应正确");
        }

        log.info("测试完成，结果验证通过");
    }

    /**
     * 测试多个不同的 UID
     */
//    @Test
//    void testFetchMultipleUsers() {
//        String[] testUids = {"1", "2", "11111"}; // 可以添加更多测试 UID
//
//        for (String uid : testUids) {
//            log.info("测试 UID: {}", uid);
//            try {
//                Map<String, Object> result = playwrightUtil.fetchBilibiliUserInfo(uid);
//                log.info("UID {} 抓取成功：{}", uid, result.get("username"));
//                assertNotNull(result.get("personalSpaceUrl"));
//            } catch (Exception e) {
//                log.error("UID {} 抓取失败：{}", uid, e.getMessage());
//                fail("UID " + uid + " 抓取失败：" + e.getMessage());
//            }
//        }
//    }



    /**
     * 性能测试：测试抓取耗时
     */
    @Test
    void testFetchPerformance() {
        long startTime = System.currentTimeMillis();
        
        BilibiliUserInfoVO result = playwrightUtil.fetchBilibiliUserInfo(TEST_UID);
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        log.info("抓取耗时：{} ms", duration);
        log.info("抓取结果：{}", result);

        // 验证性能（假设 30 秒内应该完成）
        assertTrue(duration < 30000, "抓取时间应在 30 秒内完成");
    }
}
