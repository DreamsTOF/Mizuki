package cn.dreamtof.content.application.assembler;

import cn.dreamtof.content.api.vo.PostDetailVO;
import cn.dreamtof.content.api.vo.PostVO;
import cn.dreamtof.content.domain.model.entity.Posts;
import cn.dreamtof.content.infrastructure.persistence.po.PostsPO;
import org.mapstruct.Mapper;
import java.util.List;

@Mapper(componentModel = "spring")
public interface PostsAssembler {

    PostsPO toPO(Posts entity);

    Posts toEntity(PostsPO po);

    List<Posts> toEntityList(List<PostsPO> poList);

    List<PostsPO> toPOList(List<Posts> entityList);

    PostVO toVO(Posts entity);

    List<PostVO> toVOList(List<Posts> entities);

    PostDetailVO toDetailVO(Posts entity);
}
