package cn.dreamtof.media.application.assembler;

import cn.dreamtof.media.domain.model.entity.MusicPlaylists;
import cn.dreamtof.media.infrastructure.persistence.po.MusicPlaylistsPO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import java.util.List;

/**
 * 音乐播放列表表 转换器
 * <p>
 * 职责：实现DTO ,VO, Entity 与 PO 之间的无损映射。
 * </p>
 */
@Mapper(componentModel = "spring")
public interface MusicPlaylistsAssembler {

    /**
     * Entity 转 PO (入库)
     */
    MusicPlaylistsPO toPO(MusicPlaylists entity);

    /**
     * PO 转 Entity (出库)
     */
    MusicPlaylists toEntity(MusicPlaylistsPO po);

    /**
     * 集合转换
     * PO 转 Entity (出库)
     */
    List<MusicPlaylists> toEntityList(List<MusicPlaylistsPO> poList);

    /**
     * 集合转换
     * Entity 转 PO (入库)
     */
    List<MusicPlaylistsPO> toPOList(List<MusicPlaylists> entityList);
}