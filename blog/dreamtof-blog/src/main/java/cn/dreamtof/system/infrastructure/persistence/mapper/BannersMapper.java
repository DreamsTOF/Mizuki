package cn.dreamtof.system.infrastructure.persistence.mapper;


import org.apache.ibatis.annotations.Mapper;
import com.mybatisflex.core.BaseMapper;
import cn.dreamtof.system.infrastructure.persistence.po.BannersPO;


/**
 * 横幅图片表 Mapper 数据库访问接口
 * <p>
 * 继承 BaseMapper 以获得 MyBatis-Flex 提供的基础 CRUD 能力。
 * </p>
 *
 * @author lyl
 * @since 2026-05-09
 */
@Mapper
public interface BannersMapper extends BaseMapper<BannersPO> {

}
