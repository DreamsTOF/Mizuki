//package cn.dreamtof.songs.common.utils;
//
//import jakarta.annotation.PostConstruct;
//import jakarta.annotation.PreDestroy;
//import lombok.Getter;
//import net.openhft.hashing.LongHashFunction;
//import org.rocksdb.Options;
//import org.rocksdb.RocksDB;
//import org.rocksdb.RocksDBException;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//
//import java.io.File;
//import java.nio.ByteBuffer;
//import java.nio.charset.StandardCharsets;
//
///**
// * 🚀 通用文本指纹去重引擎 (RocksDB 持久化版)
// * 适配场景：海量数据、动态增长、高并发写入
// */
//@Component
//public class TextFingerprintDeduplicator {
//
//    private static final Logger log = LoggerFactory.getLogger(TextFingerprintDeduplicator.class);
//
//    static {
//        // 必须加载 RocksDB 本地库
//        RocksDB.loadLibrary();
//    }
//
//    // ================= 核心配置 =================
//
//    private static final LongHashFunction HASHER1 = LongHashFunction.wy_3(0x12345678L);
//    private static final LongHashFunction HASHER2 = LongHashFunction.wy_3(0x87654321L);
//    private static final char FIELD_SEPARATOR = '\u0001';
//
//    @Value("${dedup.db.path:./data/fingerprints_rocks}")
//    private String dbPath;
//
//    private static final ThreadLocal<StringBuilder> BUFFER = ThreadLocal.withInitial(() -> new StringBuilder(8192));
//
//    // RocksDB 核心对象
//    private RocksDB db;
//    private Options options;
//
//    @PostConstruct
//    public void init() throws RocksDBException {
//        File file = new File(dbPath);
//        if (!file.exists()) file.mkdirs();
//
//        this.options = new Options();
//        // 如果数据库不存在则创建
//        options.setCreateIfMissing(true);
//        // 针对闪存优化，提高压缩率和读写平衡
//        options.setWriteBufferSize(64 * 1024 * 1024); // 64MB 写缓冲
//
//        try {
//            this.db = RocksDB.open(options, dbPath);
//            log.info("RocksDB 指纹库加载成功 | 路径: {}", dbPath);
//        } catch (RocksDBException e) {
//            log.error("RocksDB 初始化失败", e);
//            throw e;
//        }
//    }
//
//    @PreDestroy
//    public void close() {
//        if (db != null) {
//            db.close();
//        }
//        if (options != null) {
//            options.close();
//        }
//        log.info("RocksDB 优雅关闭完成");
//    }
//
//    // ================= 核心算法 =================
//
//    /**
//     * 计算 128 位指纹（Zero-Allocation 清洗）
//     */
//    public Fingerprint calculate128BitHash(String... fields) {
//        if (fields == null || fields.length == 0) return new Fingerprint(0, 0);
//
//        StringBuilder sb = BUFFER.get();
//        sb.setLength(0);
//
//        for (int i = 0; i < fields.length; i++) {
//            if (i > 0) sb.append(FIELD_SEPARATOR);
//            appendCleanedTo(fields[i], sb);
//        }
//
//        return new Fingerprint(HASHER1.hashChars(sb), HASHER2.hashChars(sb));
//    }
//
//    private void appendCleanedTo(String input, StringBuilder target) {
//        if (input == null) return;
//        for (int i = 0; i < input.length(); i++) {
//            char c = input.charAt(i);
//            if ((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9')) {
//                target.append(c);
//            } else if (c >= 'A' && c <= 'Z') {
//                target.append((char) (c + 32));
//            } else if (Character.isLetterOrDigit(c)) {
//                target.append(Character.toLowerCase(c));
//            }
//        }
//    }
//
//    // ================= 存储操作 =================
//
//    /**
//     * 判断是否存在（查重）
//     */
//    public boolean isDuplicate(Fingerprint fp) {
//        if (fp == null) return false;
//        try {
//            // RocksDB 的 get 返回 null 表示不存在
//            return db.get(fp.toBytes()) != null;
//        } catch (RocksDBException e) {
//            log.error("RocksDB 查询异常", e);
//            return false;
//        }
//    }
//
//    /**
//     * 存储指纹及其关联 ID
//     */
//    public void saveFingerprint(Fingerprint fp, String targetId) {
//        if (fp == null || targetId == null) return;
//        try {
//            db.put(fp.toBytes(), targetId.getBytes(StandardCharsets.UTF_8));
//        } catch (RocksDBException e) {
//            log.error("RocksDB 写入异常", e);
//        }
//    }
//
//    /**
//     * 通过指纹反查原始 ID
//     */
//    public String getTargetId(Fingerprint fp) {
//        if (fp == null) return null;
//        try {
//            byte[] data = db.get(fp.toBytes());
//            return data == null ? null : new String(data, StandardCharsets.UTF_8);
//        } catch (RocksDBException e) {
//            log.error("RocksDB 读取异常", e);
//            return null;
//        }
//    }
//
//    // ================= 内部数据结构 =================
//
//    /**
//     * 128 位指纹，提供与 byte[] 转换的方法
//     */
//    @Getter
//    public static class Fingerprint {
//        private final long h1;
//        private final long h2;
//
//        public Fingerprint(long h1, long h2) {
//            this.h1 = h1;
//            this.h2 = h2;
//        }
//
//        /**
//         * 将两个 long 转为 16 字节数组供 RocksDB 使用
//         */
//        public byte[] toBytes() {
//            byte[] bytes = new byte[16];
//            ByteBuffer.wrap(bytes).putLong(h1).putLong(h2);
//            return bytes;
//        }
//
//        @Override
//        public String toString() {
//            return Long.toHexString(h1) + Long.toHexString(h2);
//        }
//    }
//}