package cn.dreamtof.content.domain.service;

import cn.dreamtof.content.domain.model.entity.Posts;
import cn.dreamtof.content.domain.repository.PostsRepository;
import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchService {

    private final PostsRepository postsRepository;

    public PageResult<Posts> searchPosts(String keyword, PageReq pageReq) {
        if (keyword == null || keyword.isBlank()) {
            return postsRepository.page(pageReq);
        }
        return postsRepository.searchByKeyword(keyword.trim(), pageReq);
    }
}
