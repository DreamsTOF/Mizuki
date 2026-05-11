package cn.dreamtof.system.application.service;

import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.system.api.vo.NavLinkVO;
import cn.dreamtof.system.application.assembler.NavLinksAssembler;
import cn.dreamtof.system.domain.model.entity.NavLinks;
import cn.dreamtof.system.domain.service.NavLinkDomainService;
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
public class NavLinksAppService {

    private final NavLinkDomainService navLinkDomainService;
    private final NavLinksAssembler assembler;

    public NavLinkVO createNavLink(String name, String url, String icon,
                                   Boolean hasExternal, Boolean hasNewWindow,
                                   UUID parentId, String position, Integer sortOrder) {
        NavLinks entity = NavLinks.create(name, url, icon, hasExternal, hasNewWindow,
                parentId, position, sortOrder);
        NavLinks created = navLinkDomainService.createNavLink(entity);
        log.info("导航链接创建完成, navLinkId={}, name={}", created.getId(), name);
        return assembler.toVO(created);
    }

    public NavLinkVO updateNavLink(UUID id, String name, String url, String icon,
                                   Boolean hasExternal, Boolean hasNewWindow,
                                   UUID parentId, String position, Integer sortOrder) {
        NavLinks existing = navLinkDomainService.getById(id);
        existing.update(name, url, icon, hasExternal, hasNewWindow, parentId, position, sortOrder);
        NavLinks updated = navLinkDomainService.updateNavLink(existing);
        log.info("导航链接更新完成, navLinkId={}", id);
        return assembler.toVO(updated);
    }

    public boolean deleteNavLink(UUID id) {
        return navLinkDomainService.deleteNavLink(id);
    }

    public NavLinkVO toggleEnabled(UUID id, boolean enabled) {
        NavLinks updated = navLinkDomainService.toggleEnabled(id, enabled);
        return assembler.toVO(updated);
    }

    public NavLinkVO getDetail(UUID id) {
        NavLinks entity = navLinkDomainService.getById(id);
        return assembler.toVO(entity);
    }

    public List<NavLinkVO> listAll() {
        List<NavLinks> entities = navLinkDomainService.listAll();
        return assembler.toVOList(entities);
    }

    public List<NavLinkVO> listByPosition(String position) {
        List<NavLinks> entities = navLinkDomainService.listByPosition(position);
        return assembler.toVOList(entities);
    }

    public List<NavLinkVO> listTree() {
        List<NavLinks> allLinks = navLinkDomainService.listEnabled();
        return buildTreeVO(allLinks);
    }

    public List<NavLinkVO> listTreeByPosition(String position) {
        List<NavLinks> links = navLinkDomainService.listByPosition(position);
        return buildTreeVO(links);
    }

    public PageResult<NavLinkVO> pageNavLinks(PageReq pageReq) {
        // NavLinks 使用树形展示，分页场景较少，此处提供扁平分页
        List<NavLinks> all = navLinkDomainService.listAll();
        List<NavLinkVO> voList = assembler.toVOList(all);
        return PageResult.of(voList, voList.size(), 1, 1, voList.size());
    }

    private List<NavLinkVO> buildTreeVO(List<NavLinks> flatList) {
        Map<UUID, NavLinkVO> lookup = new LinkedHashMap<>();
        List<NavLinkVO> roots = new ArrayList<>();
        for (NavLinks link : flatList) {
            lookup.put(link.getId(), assembler.toVO(link));
        }
        for (NavLinks link : flatList) {
            NavLinkVO vo = lookup.get(link.getId());
            if (link.isRoot()) {
                roots.add(vo);
            } else {
                NavLinkVO parent = lookup.get(link.getParentId());
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(vo);
                } else {
                    roots.add(vo);
                }
            }
        }
        return roots;
    }
}
