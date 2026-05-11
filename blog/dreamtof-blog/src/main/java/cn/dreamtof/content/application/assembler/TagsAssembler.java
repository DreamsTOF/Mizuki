package cn.dreamtof.content.application.assembler;

import cn.dreamtof.content.api.vo.TagVO;
import cn.dreamtof.content.domain.model.entity.Tags;
import cn.dreamtof.content.infrastructure.persistence.po.TagsPO;
import org.mapstruct.Mapper;
import java.util.List;

@Mapper(componentModel = "spring")
public interface TagsAssembler {

    TagsPO toPO(Tags entity);

    Tags toEntity(TagsPO po);

    List<Tags> toEntityList(List<TagsPO> poList);

    List<TagsPO> toPOList(List<Tags> entityList);

    TagVO toVO(Tags entity);

    List<TagVO> toVOList(List<Tags> entities);
}
