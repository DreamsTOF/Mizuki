package cn.dreamtof.system.domain.service;

import cn.dreamtof.core.exception.Asserts;
import cn.dreamtof.system.domain.model.entity.NavLinks;
import cn.dreamtof.system.domain.repository.NavLinksRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NavLinkDomainService {

    private final NavLinksRepository navLinksRepository;

    public NavLinks createNavLink(NavLinks entity) {
        Asserts.notNull(entity, "导航链接实体不能为空");
        return navLinksRepository.create(entity);
    }

    public NavLinks updateNavLink(NavLinks entity) {
        Asserts.notNull(entity, "导航链接实体不能为空");
        Asserts.notNull(entity.getId(), "导航链接ID不能为空");
        NavLinks existing = navLinksRepository.getById(entity.getId());
        Asserts.notNull(existing, "导航链接不存在");
        return navLinksRepository.update(entity);
    }

    public boolean deleteNavLink(UUID id) {
        NavLinks existing = navLinksRepository.getById(id);
        Asserts.notNull(existing, "导航链接不存在");
        existing.markDeleted();
        navLinksRepository.update(existing);
        log.info("导航链接软删除完成, navLinkId={}", id);
        return true;
    }

    public NavLinks toggleEnabled(UUID id, boolean enabled) {
        NavLinks existing = navLinksRepository.getById(id);
        Asserts.notNull(existing, "导航链接不存在");
        existing.toggleEnabled(enabled);
        return navLinksRepository.update(existing);
    }

    public NavLinks getById(UUID id) {
        NavLinks entity = navLinksRepository.getById(id);
        Asserts.notNull(entity, "导航链接不存在");
        return entity;
    }

    public List<NavLinks> listAll() {
        return navLinksRepository.listAll();
    }

    public List<NavLinks> listByPosition(String position) {
        return navLinksRepository.listByPosition(position);
    }

    public List<NavLinks> listEnabled() {
        return navLinksRepository.listEnabled();
    }

    /**
     * 构建树形结构：将扁平列表转换为父子嵌套结构
     */
    public List<NavLinks> buildTree(List<NavLinks> flatList) {
        Map<UUID, NavLinks> lookup = new LinkedHashMap<>();
        List<NavLinks> roots = new ArrayList<>();
        for (NavLinks link : flatList) {
            lookup.put(link.getId(), link);
        }
        for (NavLinks link : flatList) {
            if (link.isRoot()) {
                roots.add(link);
            } else {
                NavLinks parent = lookup.get(link.getParentId());
                if (parent != null) {
                    // 子节点由 VO 层的 children 字段承载
                    // 此处仅返回扁平结构，树形组装在 AppService 层完成
                } else {
                    roots.add(link);
                }
            }
        }
        return roots;
    }
}
