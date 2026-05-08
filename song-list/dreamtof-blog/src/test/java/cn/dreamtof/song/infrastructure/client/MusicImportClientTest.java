package cn.dreamtof.song.infrastructure.client;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
class MusicImportClientTest {

    @Autowired
    private MusicImportClient musicImportClient;

    @BeforeEach
    void setUp() {
        log.info("========== 测试开始 ==========");
    }

    @Test
    @DisplayName("网易云 - 导入78首歌的歌单")
    void testNetEaseImport() {
        String shareUrl = "https://music.163.com/playlist?id=7928400067&uct2=U2FsdGVkX19L1RZkQ7iAvaC7j0EHUP2/YyJDOW4bqTY=";

        MusicImportClient.ImportedPlaylist playlist =
                musicImportClient.fetchPlaylist(shareUrl, cn.dreamtof.song.domain.enums.MusicPlatformEnum.NETEASE);

        assertNotNull(playlist, "歌单不应为空");
        assertNotNull(playlist.name(), "歌单名称不应为空");
        log.info("网易云歌单名称: {}", playlist.name());

        List<MusicImportClient.ImportedSong> songs = playlist.songs();
        assertNotNull(songs, "歌曲列表不应为空");
        log.info("网易云歌曲数量: {}", songs.size());

        assertEquals(78, songs.size(), "歌单应有78首歌，不能多也不能少");

        for (int i = 0; i < songs.size(); i++) {
            MusicImportClient.ImportedSong song = songs.get(i);
            assertFalse(song.name().isBlank(), "第" + (i + 1) + "首歌名不能为空");
            log.info("  [{}] {} | {} | {}", i + 1, song.name(), song.singer(), song.album());
        }

        long withSinger = songs.stream().filter(s -> !s.singer().isBlank()).count();
        long withAlbum = songs.stream().filter(s -> !s.album().isBlank()).count();
        long withCover = songs.stream().filter(s -> !s.coverUrl().isBlank()).count();
        long withJump = songs.stream().filter(s -> !s.jumpUrl().isBlank()).count();

        log.info("三要素统计 - 有歌手: {}/78, 有专辑: {}/78, 有封面: {}/78, 有跳转: {}/78",
                withSinger, withAlbum, withCover, withJump);

        assertTrue(withSinger >= 75, "至少75首歌应有歌手信息");
        assertTrue(withAlbum >= 70, "至少70首歌应有专辑信息");
    }

    @Test
    @DisplayName("QQ音乐 - 导入歌单")
    void testQQImport() {
        String shareUrl = "https://i2.y.qq.com/n3/other/pages/details/playlist.html?platform=11&appshare=android_qq&appversion=20030008&hosteuin=oKSqNeSFoe-sov**&id=5915409295&ADTAG=qfshare&qq_aio_chat_type=2";

        MusicImportClient.ImportedPlaylist playlist =
                musicImportClient.fetchPlaylist(shareUrl, cn.dreamtof.song.domain.enums.MusicPlatformEnum.QQ);

        assertNotNull(playlist, "歌单不应为空");
        log.info("QQ音乐歌单名称: {}", playlist.name());

        List<MusicImportClient.ImportedSong> songs = playlist.songs();
        assertNotNull(songs, "歌曲列表不应为空");
        assertFalse(songs.isEmpty(), "歌曲列表不应为空");
        log.info("QQ音乐歌曲数量: {}", songs.size());

        for (int i = 0; i < songs.size(); i++) {
            MusicImportClient.ImportedSong song = songs.get(i);
            assertFalse(song.name().isBlank(), "第" + (i + 1) + "首歌名不能为空");
            log.info("  [{}] {} | {} | {}", i + 1, song.name(), song.singer(), song.album());
        }

        long withSinger = songs.stream().filter(s -> !s.singer().isBlank()).count();
        long withAlbum = songs.stream().filter(s -> !s.album().isBlank()).count();
        log.info("QQ音乐统计 - 有歌手: {}/{}, 有专辑: {}/{}",
                withSinger, songs.size(), withAlbum, songs.size());
    }

    @Test
    @DisplayName("酷狗音乐 - 导入歌单")
    void testKugouImport() {
        String shareUrl = "https://m.kugou.com/songlist/gcid_3zhsy6p1z3z070/?src_cid=3zhsy6p1z3z070&uid=936355351&chl=message&iszlist=1";

        MusicImportClient.ImportedPlaylist playlist =
                musicImportClient.fetchPlaylist(shareUrl, cn.dreamtof.song.domain.enums.MusicPlatformEnum.KUGOU);

        assertNotNull(playlist, "歌单不应为空");
        log.info("酷狗歌单名称: {}", playlist.name());

        List<MusicImportClient.ImportedSong> songs = playlist.songs();
        assertNotNull(songs, "歌曲列表不应为空");
        assertFalse(songs.isEmpty(), "歌曲列表不应为空");
        log.info("酷狗歌曲数量: {}", songs.size());

        for (int i = 0; i < songs.size(); i++) {
            MusicImportClient.ImportedSong song = songs.get(i);
            assertFalse(song.name().isBlank(), "第" + (i + 1) + "首歌名不能为空");
            log.info("  [{}] {} | {} | {}", i + 1, song.name(), song.singer(), song.album());
        }

        long withSinger = songs.stream().filter(s -> !s.singer().isBlank()).count();
        long withAlbum = songs.stream().filter(s -> !s.album().isBlank()).count();
        log.info("酷狗统计 - 有歌手: {}/{}, 有专辑: {}/{}",
                withSinger, songs.size(), withAlbum, songs.size());
    }

    @Test
    @DisplayName("咪咕音乐 - 导入歌单")
    void testMiguImport() {
        String shareUrl = "https://c.migu.cn/00CFms?ifrom=f17fb85a758b8d1d512516817496307b";

        MusicImportClient.ImportedPlaylist playlist =
                musicImportClient.fetchPlaylist(shareUrl, cn.dreamtof.song.domain.enums.MusicPlatformEnum.MIGU);

        assertNotNull(playlist, "歌单不应为空");
        log.info("咪咕歌单名称: {}", playlist.name());

        List<MusicImportClient.ImportedSong> songs = playlist.songs();
        assertNotNull(songs, "歌曲列表不应为空");
        assertFalse(songs.isEmpty(), "歌曲列表不应为空");
        log.info("咪咕歌曲数量: {}", songs.size());

        for (int i = 0; i < songs.size(); i++) {
            MusicImportClient.ImportedSong song = songs.get(i);
            assertFalse(song.name().isBlank(), "第" + (i + 1) + "首歌名不能为空");
            log.info("  [{}] {} | {} | {}", i + 1, song.name(), song.singer(), song.album());
        }

        long withSinger = songs.stream().filter(s -> !s.singer().isBlank()).count();
        long withAlbum = songs.stream().filter(s -> !s.album().isBlank()).count();
        log.info("咪咕统计 - 有歌手: {}/{}, 有专辑: {}/{}",
                withSinger, songs.size(), withAlbum, songs.size());
    }

    @Test
    @DisplayName("链接与平台不匹配时应抛异常")
    void testUrlPlatformMismatch() {
        String neteaseUrl = "https://music.163.com/playlist?id=7928400067";

        assertThrows(Exception.class, () ->
                musicImportClient.fetchPlaylist(neteaseUrl,
                        cn.dreamtof.song.domain.enums.MusicPlatformEnum.QQ),
                "网易云链接配QQ音乐平台应抛异常");

        assertThrows(Exception.class, () ->
                musicImportClient.fetchPlaylist(neteaseUrl,
                        cn.dreamtof.song.domain.enums.MusicPlatformEnum.KUGOU),
                "网易云链接配酷狗平台应抛异常");
    }

    @Test
    @DisplayName("无效链接应抛异常")
    void testInvalidUrl() {
        assertThrows(Exception.class, () ->
                musicImportClient.fetchPlaylist("https://music.163.com/playlist",
                        cn.dreamtof.song.domain.enums.MusicPlatformEnum.NETEASE),
                "缺少歌单ID的链接应抛异常");

        assertThrows(Exception.class, () ->
                musicImportClient.fetchPlaylist("not-a-url",
                        cn.dreamtof.song.domain.enums.MusicPlatformEnum.NETEASE),
                "无效链接应抛异常");
    }
}
