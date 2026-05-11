package cn.dreamtof.media.application.assembler;

import cn.dreamtof.media.domain.model.entity.Albums;
import cn.dreamtof.media.infrastructure.persistence.po.AlbumsPO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import java.util.List;

/**
 * 相册表 转换器
 * <p>
 * 职责：实现DTO ,VO, Entity 与 PO 之间的无损映射。
 * </p>
 */
@Mapper(componentModel = "spring")
public interface AlbumsAssembler {

    /**
     * Entity 转 PO (入库)
     */
    AlbumsPO toPO(Albums entity);

    /**
     * PO 转 Entity (出库)
     */
    Albums toEntity(AlbumsPO po);

    /**
     * 集合转换
     * PO 转 Entity (出库)
     */
    List<Albums> toEntityList(List<AlbumsPO> poList);

    /**
     * 集合转换
     * Entity 转 PO (入库)
     */
    List<AlbumsPO> toPOList(List<Albums> entityList);
}