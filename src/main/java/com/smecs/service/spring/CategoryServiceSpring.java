package com.smecs.service.spring;

import com.smecs.dto.CategoryDTO;
import com.smecs.model.Category;
import com.smecs.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CategoryServiceSpring {

    private final CategoryRepository repository;

    public CategoryServiceSpring(CategoryRepository repository) {
        this.repository = repository;
    }

    public CategoryDTO create(CategoryDTO dto) {
        Category entity = new Category();
        entity.setCategoryName(dto.getCategoryName());
        entity.setDescription(dto.getDescription());
        Category saved = repository.save(entity);
        return new CategoryDTO(saved.getCategoryId(), saved.getCategoryName(), saved.getDescription());
    }

    public Optional<CategoryDTO> findById(Integer id) {
        return repository.findById(id)
                .map(c -> new CategoryDTO(c.getCategoryId(), c.getCategoryName(), c.getDescription()));
    }

    public List<CategoryDTO> findAll() {
        return repository.findAll().stream()
                .map(c -> new CategoryDTO(c.getCategoryId(), c.getCategoryName(), c.getDescription()))
                .collect(Collectors.toList());
    }

    public void delete(Integer id) {
        repository.deleteById(id);
    }

    public Optional<CategoryDTO> update(Integer id, CategoryDTO dto) {
        return repository.findById(id).map(existing -> {
            existing.setCategoryName(dto.getCategoryName());
            existing.setDescription(dto.getDescription());
            Category saved = repository.save(existing);
            return new CategoryDTO(saved.getCategoryId(), saved.getCategoryName(), saved.getDescription());
        });
    }
}
