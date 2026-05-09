package cn.dreamtof.media.infrastructure.persistence.mapper;


import org.apache.ibatis.annotations.Mapper;
import com.mybatisflex.core.BaseMapper;
import cn.dreamtof.blog.media.infrastructure.persistence.po.AlbumPhotosPO;


/**
 * 相册图片表 Mapper 数据库访问接口
 * <p>
 * 继承 BaseMapper 以获得 MyBatis-Flex 提供的基础 CRUD 能力。
 * </p>
 *
 * @author dream
 * @since 2026-05-08
 */
@Mapper
public interface AlbumPhotosMapper extends BaseMapper<AlbumPhotosPO> {

}
