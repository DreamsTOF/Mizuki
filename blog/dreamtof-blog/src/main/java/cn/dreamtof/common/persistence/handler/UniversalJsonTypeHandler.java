package cn.dreamtof.common.persistence.handler;

import cn.dreamtof.core.utils.JsonUtils;
import com.fasterxml.jackson.databind.JavaType;
import com.mybatisflex.core.handler.JacksonTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 通用 JSON 类型处理器 (优化版)
 * 适配 PostgreSQL (jsonb/json) 与 MySQL/Oracle (String)
 */
@MappedTypes(Object.class)
@MappedJdbcTypes(JdbcType.OTHER)
public class UniversalJsonTypeHandler extends JacksonTypeHandler {

    private static final Logger log = LoggerFactory.getLogger(UniversalJsonTypeHandler.class);

    // 运行时的完整泛型类型
    private final Type runtimeType;

    // 缓存数据库是否为 PostgreSQL，避免高频 IO 操作
    private Boolean isPostgres = null;

    public UniversalJsonTypeHandler(Class<?> type) {
        super(type);
        this.runtimeType = type;
    }

    /**
     * MyBatis 在映射带泛型的集合（如 List<User>）时会优先调用此构造函数
     */
    public UniversalJsonTypeHandler(Class<?> type, Type genericType) {
        super(type);
        this.runtimeType = (genericType != null) ? genericType : type;
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType) throws SQLException {
        if (ps == null || parameter == null) {
            return;
        }

        String jsonStr = JsonUtils.toJsonString(parameter); // 使用全局统一序列化工具

        // 仅在第一次执行时判断数据库类型，提升性能
        if (isPostgres == null) {
            String dbName = ps.getConnection().getMetaData().getDatabaseProductName().toLowerCase();
            isPostgres = dbName.contains("postgresql");
        }

        if (isPostgres) {
            PGobject pgObject = new PGobject();
            // 默认映射为 jsonb，PG 中性能更优
            pgObject.setType("jsonb");
            pgObject.setValue(jsonStr);
            ps.setObject(i, pgObject);
        } else {
            ps.setString(i, jsonStr);
        }
    }

    public Object parse(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            // 核心修复：通过 JsonUtils.MAPPER 构造包含泛型信息的 JavaType
            JavaType javaType = JsonUtils.MAPPER.constructType(this.runtimeType);
            return JsonUtils.MAPPER.readValue(json, javaType);
        } catch (Exception e) {
            log.error("JSON 反序列化失败! 目标类型: {}, 原始数据: {}",
                    this.runtimeType.getTypeName(), json, e);
            // 建议：在生产环境若对数据一致性要求极高，此处可选择抛出 RuntimeException
            return null;
        }
    }

    @Override
    public Object getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return convertDbToJavaObject(rs.getObject(columnName));
    }

    @Override
    public Object getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return convertDbToJavaObject(rs.getObject(columnIndex));
    }

    @Override
    public Object getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return convertDbToJavaObject(cs.getObject(columnIndex));
    }

    private Object convertDbToJavaObject(Object v) {
        if (v == null) return null;

        // 处理 PostgreSQL 驱动返回的特有对象
        if (v instanceof PGobject pg) {
            return parse(pg.getValue());
        }

        // 处理其他数据库返回的 String 或 Clob 转 String
        return parse(v.toString());
    }
}