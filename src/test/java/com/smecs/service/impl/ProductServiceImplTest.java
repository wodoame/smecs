package com.smecs.service.impl;

import com.smecs.dto.CreateProductRequestDTO;
import com.smecs.dto.PagedResponseDTO;
import com.smecs.dto.ProductDTO;
import com.smecs.dto.ProductQuery;
import com.smecs.entity.Category;
import com.smecs.entity.Product;
import com.smecs.exception.ResourceNotFoundException;
import com.smecs.repository.CategoryRepository;
import com.smecs.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    void createProduct_shouldPersistAndReturnDto() {
        CreateProductRequestDTO request = new CreateProductRequestDTO();
        request.setName("Laptop");
        request.setDescription("Fast");
        request.setPrice(1200.0);
        request.setImageUrl("image.png");
        request.setCategoryId(3L);

        Category category = new Category();
        category.setId(3L);
        when(categoryRepository.findById(3L)).thenReturn(Optional.of(category));

        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product product = invocation.getArgument(0);
            product.setId(10L);
            return product;
        });

        ProductDTO result = productService.createProduct(request);

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());
        Product saved = captor.getValue();

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getName()).isEqualTo("Laptop");
        assertThat(saved.getDescription()).isEqualTo("Fast");
        assertThat(saved.getPrice()).isEqualTo(1200.0);
        assertThat(saved.getImageUrl()).isEqualTo("image.png");
        assertThat(saved.getCategory().getId()).isEqualTo(3L);
    }

    @Test
    void getProductById_shouldReturnDto_whenFound() {
        Product product = new Product();
        product.setId(5L);
        product.setName("Phone");
        product.setDescription("Smart");
        product.setPrice(699.0);
        product.setImageUrl("phone.png");
        Category category = new Category();
        category.setId(2L);
        product.setCategory(category);
        when(productRepository.findById(5L)).thenReturn(Optional.of(product));

        ProductDTO result = productService.getProductById(5L);

        assertThat(result.getId()).isEqualTo(5L);
        assertThat(result.getName()).isEqualTo("Phone");
        assertThat(result.getDescription()).isEqualTo("Smart");
        assertThat(result.getPrice()).isEqualTo(699.0);
        assertThat(result.getImageUrl()).isEqualTo("phone.png");
        assertThat(result.getCategoryId()).isEqualTo(2L);
    }

    @Test
    void getProductById_shouldThrow_whenMissing() {
        when(productRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.getProductById(404L));
    }

    @Test
    void getProducts_shouldApplyPagingAndMapping() {
        ProductQuery query = ProductQuery.builder()
                .name("phone")
                .description("nice")
                .categoryId(2L)
                .page(2)
                .size(5)
                .sort("price,desc")
                .build();

        Product product = new Product();
        product.setId(1L);
        product.setName("Phone");
        product.setDescription("nice phone");
        product.setPrice(500.0);
        Category category = new Category();
        category.setId(2L);
        product.setCategory(category);

        PageRequest pageRequest = PageRequest.of(1, 5, Sort.by(Sort.Direction.DESC, "price"));
        // totalElements = 6 makes sense for page 2 (first page had 5 items, second page has 1)
        when(productRepository.findAll(org.mockito.ArgumentMatchers.<Specification<Product>>any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(product), pageRequest, 6));

        PagedResponseDTO<ProductDTO> result = productService.getProducts(query);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(productRepository).findAll(org.mockito.ArgumentMatchers.<Specification<Product>>any(), pageableCaptor.capture());
        Pageable usedPageable = pageableCaptor.getValue();

        assertThat(usedPageable.getPageNumber()).isEqualTo(1);
        assertThat(usedPageable.getPageSize()).isEqualTo(5);
        assertThat(usedPageable.getSort()).isEqualTo(Sort.by(Sort.Direction.DESC, "price"));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Phone");
        assertThat(result.getPage().getPage()).isEqualTo(2);
        assertThat(result.getPage().getSize()).isEqualTo(5);
        assertThat(result.getPage().getTotalElements()).isEqualTo(6);
    }

    @Test
    void updateProduct_shouldSaveUpdatedFields() {
        CreateProductRequestDTO request = new CreateProductRequestDTO();
        request.setName("Updated");
        request.setDescription("New desc");
        request.setPrice(1500.0);
        request.setImageUrl("new.png");
        request.setCategoryId(9L);

        Category category = new Category();
        category.setId(9L);
        when(categoryRepository.findById(9L)).thenReturn(Optional.of(category));

        Product existing = new Product();
        existing.setId(7L);
        existing.setName("Old");
        when(productRepository.findById(7L)).thenReturn(Optional.of(existing));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductDTO result = productService.updateProduct(7L, request);

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());
        Product saved = captor.getValue();

        assertThat(saved.getId()).isEqualTo(7L);
        assertThat(saved.getName()).isEqualTo("Updated");
        assertThat(saved.getDescription()).isEqualTo("New desc");
        assertThat(saved.getPrice()).isEqualTo(1500.0);
        assertThat(saved.getImageUrl()).isEqualTo("new.png");
        assertThat(saved.getCategory().getId()).isEqualTo(9L);
        assertThat(result.getName()).isEqualTo("Updated");
    }

    @Test
    void deleteProduct_shouldDelete_whenExists() {
        when(productRepository.existsById(3L)).thenReturn(true);

        productService.deleteProduct(3L);

        verify(productRepository).deleteById(3L);
    }

    @Test
    void deleteProduct_shouldThrow_whenMissing() {
        when(productRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> productService.deleteProduct(99L));
        verify(productRepository, never()).deleteById(any());
    }

    @Test
    void deleteProduct_shouldThrow_whenReferencedByDbConstraint() {
        when(productRepository.existsById(8L)).thenReturn(true);
        // simulate DB foreign key violation on delete (void method)
        org.mockito.Mockito.doThrow(new DataIntegrityViolationException("FK violation"))
                .when(productRepository).deleteById(8L);

        assertThrows(IllegalStateException.class, () -> productService.deleteProduct(8L));
    }

    @Test
    void searchCacheKey_shouldIncludeDefaultsAndFields() {
        String defaultKey = ProductServiceImpl.searchCacheKey(null);
        assertThat(defaultKey).isEqualTo("name:|desc:|cat:|page:1|size:8|sort:id,asc");

        ProductQuery query = ProductQuery.builder()
                .name("Laptop")
                .description("fast")
                .categoryId(4L)
                .page(3)
                .size(15)
                .sort("price,desc")
                .build();

        String key = ProductServiceImpl.searchCacheKey(query);
        assertThat(key).isEqualTo("name:Laptop|desc:fast|cat:4|page:3|size:15|sort:price,desc");
    }
}
