package cn.dreamtof.content.application.service;

import cn.dreamtof.content.api.vo.TagVO;
import cn.dreamtof.content.application.assembler.TagsAssembler;
import cn.dreamtof.content.domain.model.entity.Tags;
import cn.dreamtof.content.domain.service.TagDomainService;
import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.content.domain.repository.TagsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TagsAppService {

    private final TagDomainService tagDomainService;
    private final TagsRepository tagsRepository;
    private final TagsAssembler assembler;

    public TagVO createTag(String name, String slug) {
        Tags created = tagDomainService.createTag(name, slug);
        log.info("标签创建完成, tagId={}, name={}", created.getId(), name);
        return assembler.toVO(created);
    }

    public TagVO updateTag(UUID id, String name, String slug) {
        Tags updated = tagDomainService.updateTag(id, name, slug);
        log.info("标签更新完成, tagId={}", id);
        return assembler.toVO(updated);
    }

    public boolean deleteTag(UUID id) {
        boolean result = tagDomainService.removeById(id);
        log.info("标签删除完成, tagId={}", id);
        return result;
    }

    public TagVO getById(UUID id) {
        Tags entity = tagDomainService.getById(id);
        return assembler.toVO(entity);
    }

    public TagVO getBySlug(String slug) {
        Tags entity = tagDomainService.findBySlug(slug);
        return entity != null ? assembler.toVO(entity) : null;
    }

    public List<TagVO> listAll() {
        List<Tags> entities = tagsRepository.listAll();
        return assembler.toVOList(entities);
    }

    public PageResult<TagVO> pageTags(PageReq pageReq) {
        PageResult<Tags> pageResult = tagsRepository.page(pageReq);
        List<TagVO> voList = assembler.toVOList(pageResult.getRecords());
        return PageResult.of(voList, pageResult.getTotal(), pageResult.getPages(), pageResult.getPageNum(), pageResult.getPageSize());
    }
}
