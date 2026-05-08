package cn.dreamtof.log.infrastructure.persistence.mapper;


import org.apache.ibatis.annotations.Mapper;
import com.mybatisflex.core.BaseMapper;
import cn.dreamtof.log.domain.model.AppLogs;


/**
 *  Mapper 数据库访问接口
 * <p>
 * 继承 BaseMapper 以获得 MyBatis-Flex 提供的基础 CRUD 能力。
 * </p>
 *
 * @author dream
 * @since 
 */
@Mapper
public interface AppLogsMapper extends BaseMapper<AppLogs> {

}
