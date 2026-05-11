package cn.dreamtof.content.application.service;

import cn.dreamtof.content.api.vo.PostDetailVO;
import cn.dreamtof.content.api.vo.PostVO;
import cn.dreamtof.content.application.assembler.PostsAssembler;
import cn.dreamtof.content.domain.model.entity.Posts;
import cn.dreamtof.content.domain.model.entity.Tags;
import cn.dreamtof.content.domain.service.ArchiveDomainService;
import cn.dreamtof.content.domain.service.PostDomainService;
import cn.dreamtof.content.domain.service.SearchService;
import cn.dreamtof.content.domain.service.TagDomainService;
import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostsAppService {

    private final PostDomainService postDomainService;
    private final TagDomainService tagDomainService;
    private final ArchiveDomainService archiveDomainService;
    private final SearchService searchService;
    private final PostsAssembler assembler;
    private final TransactionTemplate transactionTemplate;

    public PostDetailVO createPost(String title, String content, String slug,
                                   String description, String author, String category,
                                   String lang, Boolean draft, String image,
                                   Boolean comment, String licenseName,
                                   String licenseUrl, String sourceLink, String alias,
                                   String permalink, String passwordHint,
                                   List<String> tagNames) {
        Posts entity = Posts.create(title, content, slug, description, author, category,
                lang, draft, image, comment, licenseName,
                licenseUrl, sourceLink, alias, permalink, passwordHint);
        Posts created = postDomainService.createPost(entity);
        List<UUID> tagIds = new ArrayList<>();
        if (tagNames != null && !tagNames.isEmpty()) {
            tagIds = tagDomainService.upsertTags(tagNames);
            postDomainService.syncPostTags(created.getId(), tagIds);
        }
        if (!Boolean.TRUE.equals(draft)) {
            archiveDomainService.updateArchiveForPost(created.getPublished(), created.getId(), true);
        }
        PostDetailVO vo = assembler.toDetailVO(created);
        List<String> names = new ArrayList<>(tagNames != null ? tagNames : List.of());
        vo.setTagNames(names);
        log.info("文章创建完成, postId={}, title={}", created.getId(), title);
        return vo;
    }

    public PostDetailVO updatePost(UUID id, String title, String content, String slug,
                                   String description, String author, String category,
                                   String lang, String coverImage, Boolean hasCommentEnabled,
                                   String licenseName, String licenseUrl, String sourceLink,
                                   String alias, String permalink, String passwordHint,
                                   List<String> tagNames) {
        Posts existing = postDomainService.getById(id);
        existing.update(title, content, slug, description, author, category,
                lang, coverImage, hasCommentEnabled, licenseName, licenseUrl,
                sourceLink, alias, permalink, passwordHint);
        Posts updated = postDomainService.updatePost(existing);
        List<UUID> tagIds = new ArrayList<>();
        if (tagNames != null) {
            tagIds = tagDomainService.upsertTags(tagNames);
            postDomainService.syncPostTags(updated.getId(), tagIds);
        }
        PostDetailVO vo = assembler.toDetailVO(updated);
        List<Tags> tags = postDomainService.getPostTags(updated.getId(), tagDomainService);
        List<String> names = new ArrayList<>();
        for (Tags tag : tags) {
            names.add(tag.getName());
        }
        vo.setTagNames(names);
        log.info("文章更新完成, postId={}", id);
        return vo;
    }

    public boolean deletePost(UUID id) {
        return transactionTemplate.execute(status -> {
            Posts existing = postDomainService.getById(id);
            if (existing.getPublished() != null) {
                archiveDomainService.updateArchiveForPost(existing.getPublished(), id, false);
            }
            return postDomainService.deletePost(id);
        });
    }

    public PostDetailVO publishDraft(UUID id) {
        Posts published = postDomainService.publishDraft(id);
        archiveDomainService.updateArchiveForPost(published.getPublished(), id, true);
        PostDetailVO vo = assembler.toDetailVO(published);
        enrichTagNames(vo);
        log.info("文章发布完成, postId={}", id);
        return vo;
    }

    public PostDetailVO unpublish(UUID id) {
        Posts unpublished = postDomainService.unpublish(id);
        PostDetailVO vo = assembler.toDetailVO(unpublished);
        enrichTagNames(vo);
        log.info("文章取消发布完成, postId={}", id);
        return vo;
    }

    public PostDetailVO togglePin(UUID id, boolean pinned, Integer priority) {
        Posts updated = postDomainService.togglePin(id, pinned, priority);
        PostDetailVO vo = assembler.toDetailVO(updated);
        enrichTagNames(vo);
        return vo;
    }

    public long incrementViewCount(UUID id) {
        return postDomainService.incrementViewCount(id);
    }

    public boolean validatePassword(UUID id, String password) {
        return postDomainService.validatePassword(id, password);
    }

    public PostDetailVO getDetail(UUID id) {
        Posts entity = postDomainService.getById(id);
        PostDetailVO vo = assembler.toDetailVO(entity);
        enrichTagNames(vo);
        return vo;
    }

    public PostDetailVO getBySlug(String slug) {
        Posts entity = postDomainService.getBySlug(slug);
        if (entity == null) {
            return null;
        }
        PostDetailVO vo = assembler.toDetailVO(entity);
        enrichTagNames(vo);
        return vo;
    }

    public PageResult<PostVO> pagePosts(PageReq pageReq) {
        PageResult<Posts> pageResult = searchService.searchPosts(null, pageReq);
        List<PostVO> voList = assembler.toVOList(pageResult.getRecords());
        return PageResult.of(voList, pageResult.getTotal(), pageResult.getPages(), pageResult.getPageNum(), pageResult.getPageSize());
    }

    public PageResult<PostVO> searchPosts(String keyword, PageReq pageReq) {
        PageResult<Posts> pageResult = searchService.searchPosts(keyword, pageReq);
        List<PostVO> voList = assembler.toVOList(pageResult.getRecords());
        return PageResult.of(voList, pageResult.getTotal(), pageResult.getPages(), pageResult.getPageNum(), pageResult.getPageSize());
    }

    private void enrichTagNames(PostDetailVO vo) {
        List<Tags> tags = postDomainService.getPostTags(vo.getId(), tagDomainService);
        List<String> names = new ArrayList<>();
        for (Tags tag : tags) {
            names.add(tag.getName());
        }
        vo.setTagNames(names);
    }
}
