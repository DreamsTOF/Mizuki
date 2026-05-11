package cn.dreamtof.content.domain.service;

import cn.dreamtof.content.domain.repository.CategoriesRepository;
import cn.dreamtof.content.domain.repository.PostsRepository;
import cn.dreamtof.content.domain.repository.TagsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class SlugService {

    private static final Pattern NON_LATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]+");
    private static final Pattern EDGES_HYPHEN = Pattern.compile("^-|-$");

    private final PostsRepository postsRepository;
    private final TagsRepository tagsRepository;
    private final CategoriesRepository categoriesRepository;

    public String generateSlug(String title) {
        if (title == null || title.isBlank()) {
            return "untitled";
        }
        String slug = title.trim().toLowerCase();
        slug = slug.replaceAll("[\\s]+", "-");
        slug = slug.replaceAll("[^a-z0-9\\u4e00-\\u9fa5-]", "");
        slug = slug.replaceAll("-{2,}", "-");
        slug = slug.replaceAll("^-|-$", "");
        if (slug.isEmpty()) {
            slug = "untitled";
        }
        return slug;
    }

    public String ensureUnique(String slug, UUID excludeId) {
        if (slug == null || slug.isBlank()) {
            slug = "untitled";
        }
        String candidate = slug;
        int suffix = 1;
        while (isSlugOccupied(candidate, excludeId)) {
            suffix++;
            candidate = slug + "-" + suffix;
        }
        return candidate;
    }

    private boolean isSlugOccupied(String slug, UUID excludeId) {
        cn.dreamtof.content.domain.model.entity.Posts post = postsRepository.findBySlug(slug);
        if (post != null && !post.getId().equals(excludeId)) {
            return true;
        }
        cn.dreamtof.content.domain.model.entity.Tags tag = tagsRepository.findBySlug(slug);
        if (tag != null && !tag.getId().equals(excludeId)) {
            return true;
        }
        cn.dreamtof.content.domain.model.entity.Categories cat = categoriesRepository.findBySlug(slug);
        if (cat != null && !cat.getId().equals(excludeId)) {
            return true;
        }
        return false;
    }
}
