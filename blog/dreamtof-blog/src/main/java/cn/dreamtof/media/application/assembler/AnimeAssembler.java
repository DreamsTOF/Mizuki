package cn.dreamtof.media.application.assembler;

import cn.dreamtof.media.domain.model.entity.Anime;
import cn.dreamtof.media.infrastructure.persistence.po.AnimePO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import java.util.List;

/**
 * 番剧表 转换器
 * <p>
 * 职责：实现DTO ,VO, Entity 与 PO 之间的无损映射。
 * </p>
 */
@Mapper(componentModel = "spring")
public interface AnimeAssembler {

    /**
     * Entity 转 PO (入库)
     */
    AnimePO toPO(Anime entity);

    /**
     * PO 转 Entity (出库)
     */
    Anime toEntity(AnimePO po);

    /**
     * 集合转换
     * PO 转 Entity (出库)
     */
    List<Anime> toEntityList(List<AnimePO> poList);

    /**
     * 集合转换
     * Entity 转 PO (入库)
     */
    List<AnimePO> toPOList(List<Anime> entityList);
}