package cn.dreamtof.content.infrastructure.persistence.mapper;

import cn.dreamtof.blog.content.infrastructure.persistence.po.ArchivesPO;
import cn.dreamtof.blog.content.infrastructure.persistence.po.ArchivesPOPO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import java.util.List;

/**
 * 文章归档索引表 转换器
 * <p>
 * 职责：实现DTO ,VO, Entity 与 PO 之间的无损映射。
 * </p>
 */
@Mapper(componentModel = "spring")
public interface ArchivesPOAssembler {

    /**
     * Entity 转 PO (入库)
     */
    ArchivesPOPO toPO(ArchivesPO entity);

    /**
     * PO 转 Entity (出库)
     */
    ArchivesPO toEntity(ArchivesPOPO po);

    /**
     * 集合转换
     * PO 转 Entity (出库)
     */
    List<ArchivesPO> toEntityList(List<ArchivesPOPO> poList);

    /**
     * 集合转换
     * Entity 转 PO (入库)
     */
    List<ArchivesPOPO> toPOList(List<ArchivesPO> entityList);
}