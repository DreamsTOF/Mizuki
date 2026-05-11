package cn.dreamtof.content.domain.service;

import cn.dreamtof.content.domain.model.entity.Archives;
import cn.dreamtof.content.domain.model.entity.Posts;
import cn.dreamtof.content.domain.repository.ArchivesRepository;
import cn.dreamtof.content.domain.repository.PostsRepository;
import cn.dreamtof.core.exception.Asserts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ArchiveDomainService {

    private final ArchivesRepository archivesRepository;
    private final PostsRepository postsRepository;

    public void rebuildArchives() {
        archivesRepository.deleteAll();
        List<Posts> publishedPosts = postsRepository.listPublished();
        for (Posts post : publishedPosts) {
            if (post.getPublished() != null) {
                updateArchiveForPost(post.getPublished(), post.getId(), true);
            }
        }
        log.info("归档索引重建完成, 共处理{}篇已发布文章", publishedPosts.size());
    }

    public void updateArchiveForPost(OffsetDateTime publishedAt, UUID postId, boolean increment) {
        Asserts.notNull(publishedAt, "发布时间不能为空");
        int year = publishedAt.getYear();
        int month = publishedAt.getMonthValue();
        Archives archive = archivesRepository.findByYearAndMonth(year, month);
        if (archive == null) {
            archive = Archives.create(year, month);
            archivesRepository.create(archive);
        }
        if (increment) {
            archive.addPost(postId);
        } else {
            archive.removePost(postId);
        }
        archivesRepository.update(archive);
    }

    public List<Archives> listByYear(Integer year) {
        Asserts.notNull(year, "年份不能为空");
        return archivesRepository.listByYear(year);
    }

    public List<Archives> listAll() {
        return archivesRepository.listAll();
    }
}
