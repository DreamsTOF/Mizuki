package cn.dreamtof.content.application.service;

import cn.dreamtof.content.application.assembler.PostTagsAssembler;
import cn.dreamtof.content.domain.model.entity.PostTags;
import cn.dreamtof.content.domain.repository.PostTagsRepository;
import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostTagsAppService {

    private final PostTagsRepository postTagsRepository;
    private final PostTagsAssembler assembler;

    public PageResult<PostTags> pagePostTags(PageReq pageReq) {
        return postTagsRepository.page(pageReq);
    }

    public List<PostTags> listByPostId(UUID postId) {
        return postTagsRepository.listByPostId(postId);
    }

    public List<PostTags> listByTagId(UUID tagId) {
        return postTagsRepository.listByTagId(tagId);
    }
}
