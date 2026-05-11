package cn.dreamtof.portfolio.domain.event;

import cn.dreamtof.core.event.ProjectCreatedEvent;
import cn.dreamtof.core.event.ProjectDeletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 项目领域事件监听器
 * <p>
 * 处理项目创建/删除事件，同步技术栈等关联数据。
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProjectEventListener {

    /**
     * 项目创建时，同步初始技术栈
     */
    @EventListener
    public void onProjectCreated(ProjectCreatedEvent event) {
        log.info("监听到 ProjectCreatedEvent, projectId={}, category={}",
                event.getProjectId(), event.getCategory());
        // 预留：技术栈预填充、项目统计更新等
        log.info("项目已创建, projectId={}, 技术栈同步由业务侧处理", event.getProjectId());
    }

    /**
     * 项目删除时，清理关联数据
     */
    @EventListener
    public void onProjectDeleted(ProjectDeletedEvent event) {
        log.info("监听到 ProjectDeletedEvent, projectId={}", event.getProjectId());
        // 关联的技术栈、标签数据已在业务侧同步删除，此处仅记录
        log.info("项目已删除, projectId={}, 关联数据已清理", event.getProjectId());
    }
}
