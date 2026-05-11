package cn.dreamtof.media.application.assembler;

import cn.dreamtof.media.domain.model.entity.MusicTracks;
import cn.dreamtof.media.infrastructure.persistence.po.MusicTracksPO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import java.util.List;

/**
 * 音乐曲目表 转换器
 * <p>
 * 职责：实现DTO ,VO, Entity 与 PO 之间的无损映射。
 * </p>
 */
@Mapper(componentModel = "spring")
public interface MusicTracksAssembler {

    /**
     * Entity 转 PO (入库)
     */
    MusicTracksPO toPO(MusicTracks entity);

    /**
     * PO 转 Entity (出库)
     */
    MusicTracks toEntity(MusicTracksPO po);

    /**
     * 集合转换
     * PO 转 Entity (出库)
     */
    List<MusicTracks> toEntityList(List<MusicTracksPO> poList);

    /**
     * 集合转换
     * Entity 转 PO (入库)
     */
    List<MusicTracksPO> toPOList(List<MusicTracks> entityList);
}