package cn.dreamtof.social.domain.service;

import cn.dreamtof.social.domain.repository.CommentsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentCountService {

    private final CommentsRepository commentsRepository;

    public long countByTargetId(UUID targetId) {
        return commentsRepository.countByTargetId(targetId);
    }

    public long countByTargetIdAndType(UUID targetId, String type) {
        return commentsRepository.countByTargetIdAndType(targetId, type);
    }

    public Map<UUID, Long> batchCountByTargetIds(List<UUID> targetIds) {
        Map<UUID, Long> result = new HashMap<>();
        for (UUID targetId : targetIds) {
            result.put(targetId, commentsRepository.countByTargetId(targetId));
        }
        return result;
    }
}
