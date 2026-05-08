//package cn.dreamtof.songs.common.utils;
//
//import cn.dreamtof.songs.result.domain.model.Song;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import java.util.List;
//
//@Component
//public class SongDedupUtils {
//
//    private static SongDeduplicator deduplicator;
//
//    // 通过构造函数或注入方式给静态变量赋值
//    @Autowired
//    public void setDeduplicator(SongDeduplicator deduplicator) {
//        SongDedupUtils.deduplicator = deduplicator;
//    }
//
//    public static List<Song> filter(List<Song> songs) {
//        return deduplicator.filterNewSongs(songs);
//    }
//
//    public static void bind(List<Song> insertedSongs) {
//        deduplicator.bindUlids(insertedSongs);
//    }
//}
