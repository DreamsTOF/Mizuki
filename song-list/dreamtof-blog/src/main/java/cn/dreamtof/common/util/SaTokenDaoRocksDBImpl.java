package cn.dreamtof.common.util;

//
//import cn.dev33.satoken.dao.SaTokenDao;
//import com.github.benmanes.caffeine.cache.Cache;
//import com.github.benmanes.caffeine.cache.Caffeine;
//import jakarta.annotation.PostConstruct;
//import jakarta.annotation.PreDestroy;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.lang3.SerializationUtils;
//import org.rocksdb.Options;
//import org.rocksdb.RocksDB;
//import org.rocksdb.RocksDBException;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//import java.io.File;
//import java.io.Serializable;
//import java.nio.charset.StandardCharsets;
//import java.util.List;
//import java.util.concurrent.atomic.LongAdder;
//
///**
// * Sa-Token 高性能混合缓存实现 (L1 Caffeine + L2 RocksDB)
// * 解决 ChronicleMap 在现代 JDK 中的模块化兼容性问题。
// */
//@Component
//@Slf4j
//public class SaTokenDaoRocksDBImpl implements SaTokenDao {
//
//    static {
//        // 必须加载 RocksDB 原生库
//        RocksDB.loadLibrary();
//    }
//
//    private Cache<String, CacheEntry> l1Cache;
//    private RocksDB rocksDb;
//    private final String dbPath = "./data/rocksdb_sa_token";
//
//    @PostConstruct
//    public void init() {
//        // 1. 初始化 L1 Caffeine
//        this.l1Cache = Caffeine.newBuilder().maximumSize(50_000).build();
//
//        // 2. 初始化 L2 RocksDB
//        try {
//            File dir = new File(dbPath);
//            if (!dir.exists()) dir.mkdirs();
//
//            Options options = new Options();
//            options.setCreateIfMissing(true);
//            // 开启自动压缩和后台清理
//            options.setWriteBufferSize(64 * 1024 * 1024);
//
//            this.rocksDb = RocksDB.open(options, dbPath);
//            log.info("Sa-Token RocksDB DAO 初始化成功，路径: {}", dbPath);
//        } catch (RocksDBException e) {
//            log.error("RocksDB 初始化失败", e);
//            throw new RuntimeException("RocksDB 初始化失败", e);
//        }
//    }
//
//    // ================= 核心读写逻辑 =================
//
//    @Override
//    public Object getObject(String key) {
//        // 1. L1 命中
//        CacheEntry entry = l1Cache.getIfPresent(key);
//
//        // 2. L1 未命中，查 L2
//        if (entry == null) {
//            try {
//                byte[] val = rocksDb.get(key.getBytes(StandardCharsets.UTF_8));
//                if (val != null) {
//                    // 使用 Apache Commons Lang3 的序列化工具，非常稳定
//                    entry = SerializationUtils.deserialize(val);
//                    l1Cache.put(key, entry);
//                }
//            } catch (RocksDBException e) {
//                log.error("RocksDB 读取异常", e);
//            }
//        }
//
//        // 3. 过期判定
//        if (entry != null) {
//            if (entry.isExpired()) {
//                this.deleteObject(key);
//                return null;
//            }
//            return entry.data;
//        }
//        return null;
//    }
//
//    @Override
//    public void setObject(String key, Object value, long timeout) {
//        if (timeout == 0 || timeout < SaTokenDao.NEVER_EXPIRE) return;
//
//        CacheEntry entry = new CacheEntry(value, timeout);
//        l1Cache.put(key, entry);
//
//        try {
//            byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
//            byte[] valBytes = SerializationUtils.serialize(entry);
//            rocksDb.put(keyBytes, valBytes);
//        } catch (RocksDBException e) {
//            log.error("RocksDB 写入异常", e);
//        }
//    }
//
//    @Override
//    public void deleteObject(String key) {
//        l1Cache.invalidate(key);
//        try {
//            rocksDb.delete(key.getBytes(StandardCharsets.UTF_8));
//        } catch (RocksDBException e) {
//            log.error("RocksDB 删除异常", e);
//        }
//    }
//
//    @Override
//    public long getObjectTimeout(String key) {
//        CacheEntry entry = (CacheEntry) getObject(key);
//        if (entry == null) return SaTokenDao.NOT_VALUE_EXPIRE;
//        if (entry.expireAt == Long.MAX_VALUE) return SaTokenDao.NEVER_EXPIRE;
//        return (entry.expireAt - System.currentTimeMillis()) / 1000;
//    }
//
//    @Override
//    public void updateObjectTimeout(String key, long timeout) {
//        Object value = getObject(key);
//        if (value != null) {
//            this.setObject(key, value, timeout);
//        }
//    }
//
//    // ================= 清理与销毁 =================
//
//    /**
//     * 深夜清理 RocksDB 中的过期数据 (RocksDB 本身不支持自动 TTL，除非用 TTL 模式打开)
//     */
//    @Scheduled(cron = "0 0 3 * * ?")
//    public void clearExpired() {
//        log.info("RocksDB 全量过期扫描开始...");
//        LongAdder count = new LongAdder();
//        // 这种全量扫描在 RocksDB 性能非常高，因为它通过迭代器顺序读取
//        try (var it = rocksDb.newIterator()) {
//            for (it.seekToFirst(); it.isValid(); it.next()) {
//                CacheEntry entry = SerializationUtils.deserialize(it.value());
//                if (entry != null && entry.isExpired()) {
//                    rocksDb.delete(it.key());
//                    count.increment();
//                }
//            }
//        } catch (Exception e) {
//            log.error("清理 RocksDB 异常", e);
//        }
//        log.info("清理完成，释放空间: {}", count.sum());
//    }
//
//    @PreDestroy
//    public void close() {
//        if (rocksDb != null) {
//            rocksDb.close();
//        }
//    }
//
//    // ============= 兼容性接口实现 =============
//    @Override public String get(String key) { Object v = getObject(key); return v == null ? null : v.toString(); }
//    @Override public void set(String key, String value, long timeout) { setObject(key, value, timeout); }
//    @Override public void update(String key, String value) { updateObject(key, value); }
//    @Override public void updateObject(String key, Object value) { updateObjectTimeout(key, getObjectTimeout(key)); }
//    @Override public long getTimeout(String key) { return getObjectTimeout(key); }
//    @Override public void updateTimeout(String key, long timeout) { updateObjectTimeout(key, timeout); }
//    @Override public void delete(String key) { deleteObject(key); }
//    @Override public List<String> searchData(String prefix, String keyword, int start, int size, boolean sortType) { return List.of(); }
//
//    // 内部类保持不变，但确保 data 属性是 Serializable
//    public static class CacheEntry implements Serializable {
//        private static final long serialVersionUID = 1L;
//        public final Object data;
//        public final long expireAt;
//        public CacheEntry(Object data, long timeout) {
//            this.data = data;
//            this.expireAt = (timeout == SaTokenDao.NEVER_EXPIRE) ? Long.MAX_VALUE : System.currentTimeMillis() + (timeout * 1000);
//        }
//        public boolean isExpired() {
//            return this.expireAt != Long.MAX_VALUE && System.currentTimeMillis() > this.expireAt;
//        }
//    }
//}