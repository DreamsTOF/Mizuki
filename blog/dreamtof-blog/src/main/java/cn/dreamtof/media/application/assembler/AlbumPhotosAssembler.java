package cn.dreamtof.media.application.assembler;

import cn.dreamtof.media.domain.model.entity.AlbumPhotos;
import cn.dreamtof.media.infrastructure.persistence.po.AlbumPhotosPO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import java.util.List;

/**
 * 相册图片表 转换器
 * <p>
 * 职责：实现DTO ,VO, Entity 与 PO 之间的无损映射。
 * </p>
 */
@Mapper(componentModel = "spring")
public interface AlbumPhotosAssembler {

    /**
     * Entity 转 PO (入库)
     */
    AlbumPhotosPO toPO(AlbumPhotos entity);

    /**
     * PO 转 Entity (出库)
     */
    AlbumPhotos toEntity(AlbumPhotosPO po);

    /**
     * 集合转换
     * PO 转 Entity (出库)
     */
    List<AlbumPhotos> toEntityList(List<AlbumPhotosPO> poList);

    /**
     * 集合转换
     * Entity 转 PO (入库)
     */
    List<AlbumPhotosPO> toPOList(List<AlbumPhotos> entityList);
}