package com.smecs.controller;

import com.smecs.dto.CategoryDTO;
import com.smecs.dto.CreateProductRequestDTO;
import com.smecs.dto.ProductDTO;
import com.smecs.entity.User;
import com.smecs.service.CategoryService;
import com.smecs.service.ProductService;
import com.smecs.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class GraphQLController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final UserService userService;

    // --- Products ---

    @QueryMapping
    public List<ProductDTO> products() {
        return productService.getProducts(null, null, Pageable.unpaged()).getContent();
    }

    @QueryMapping
    public ProductDTO productById(@Argument String id) {
        return productService.getProductById(Long.parseLong(id));
    }

    @SchemaMapping(typeName = "Product", field = "category")
    public GqlCategory category(ProductDTO product) {
        if (product.getCategoryId() == null) return null;
        CategoryDTO dto = categoryService.getCategoryById(product.getCategoryId(), false);
        return toGqlCategory(dto);
    }

    @MutationMapping
    public ProductDTO createProduct(@Argument String name, @Argument String description, @Argument Double price, @Argument String categoryId) {
        CreateProductRequestDTO request = new CreateProductRequestDTO();
        request.setName(name);
        request.setDescription(description);
        request.setPrice(price);
        request.setCategoryId(Long.parseLong(categoryId));
        return productService.createProduct(request);
    }

    // --- Categories ---

    @QueryMapping
    public List<GqlCategory> categories() {
        return categoryService.getCategories(null, null, false, Pageable.unpaged())
                .getContent().stream()
                .map(this::toGqlCategory)
                .collect(Collectors.toList());
    }

    @QueryMapping
    public GqlCategory categoryById(@Argument String id) {
        return toGqlCategory(categoryService.getCategoryById(Long.parseLong(id), false));
    }

    @MutationMapping
    public GqlCategory createCategory(@Argument String name, @Argument String description) {
         CategoryDTO dto = new CategoryDTO();
         dto.setCategoryName(name);
         dto.setDescription(description);
         dto.setImageUrl(""); // Default

         CategoryDTO created = categoryService.createCategory(dto);
         return toGqlCategory(created);
    }

    // --- Users ---

    @QueryMapping
    public List<GqlUser> users() {
        return userService.getAllUsers().stream()
                .map(this::toGqlUser)
                .collect(Collectors.toList());
    }

    @QueryMapping
    public GqlUser userById(@Argument String id) {
        return toGqlUser(userService.findById(Long.parseLong(id)));
    }

    // Helper
    private GqlCategory toGqlCategory(CategoryDTO dto) {
        if (dto == null) return null;
        return new GqlCategory(
            String.valueOf(dto.getCategoryId()),
            dto.getCategoryName(),
            dto.getDescription(),
            dto.getImageUrl()
        );
    }

    private GqlUser toGqlUser(User user) {
        if (user == null) return null;
        return new GqlUser(
            String.valueOf(user.getId()),
            user.getUsername(),
            user.getEmail(),
            user.getRole(),
            user.getCreatedAt() != null ? user.getCreatedAt().toString() : null
        );
    }

    public record GqlCategory(String id, String name, String description, String imageUrl) {}
    public record GqlUser(String id, String username, String email, String role, String createdAt) {}
}


