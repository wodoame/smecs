package com.smecs.service.impl;

import com.smecs.dto.CategoryDTO;
import com.smecs.entity.Category;
import com.smecs.exception.CategoryInUseException;
import com.smecs.exception.ResourceNotFoundException;
import com.smecs.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Test
    void createCategory_savesAndReturnsDto() {
        CategoryDTO request = new CategoryDTO();
        request.setCategoryName("Electronics");
        request.setDescription("Gadgets");
        request.setImageUrl("img.png");

        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
            Category category = invocation.getArgument(0);
            category.setId(2L);
            return category;
        });

        CategoryDTO result = categoryService.createCategory(request);

        assertThat(result.getCategoryId()).isEqualTo(2);
        assertThat(result.getCategoryName()).isEqualTo("Electronics");
        assertThat(result.getImageUrl()).isEqualTo("img.png");
    }

    @Test
    void getCategoryById_includesRelatedImagesWhenRequested() {
        Category category = new Category();
        category.setId(4L);
        category.setName("Books");
        category.setDescription("desc");
        category.setImageUrl("img.png");
        when(categoryRepository.findById(4L)).thenReturn(Optional.of(category));
        when(categoryRepository.findTop5ProductImageUrlsByCategoryId(4L))
                .thenReturn(List.of("a.png", "b.png"));

        CategoryDTO result = categoryService.getCategoryById(4L, true);

        assertThat(result.getRelatedImageUrls()).containsExactly("a.png", "b.png");
    }

    @Test
    void deleteCategory_throwsWhenInUse() {
        when(categoryRepository.existsById(8L)).thenReturn(true);
        when(categoryRepository.countProductsByCategoryId(8L)).thenReturn(3L);

        assertThrows(CategoryInUseException.class, () -> categoryService.deleteCategory(8L));
    }

    @Test
    void deleteCategory_throwsWhenMissing() {
        when(categoryRepository.existsById(9L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> categoryService.deleteCategory(9L));
    }
}

