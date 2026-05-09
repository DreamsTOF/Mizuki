package cn.dreamtof.content.application.assembler;

import cn.dreamtof.blog.content.domain.model.entity.Archives;
import cn.dreamtof.content.infrastructure.persistence.po.ArchivesPO;
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
public interface ArchivesAssembler {

    /**
     * Entity 转 PO (入库)
     */
    ArchivesPO toPO(Archives entity);

    /**
     * PO 转 Entity (出库)
     */
    Archives toEntity(ArchivesPO po);

    /**
     * 集合转换
     * PO 转 Entity (出库)
     */
    List<Archives> toEntityList(List<ArchivesPO> poList);

    /**
     * 集合转换
     * Entity 转 PO (入库)
     */
    List<ArchivesPO> toPOList(List<Archives> entityList);
}