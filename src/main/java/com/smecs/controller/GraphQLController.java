package com.smecs.controller;

import com.smecs.annotation.RequireRole;
import com.smecs.dto.*;
import com.smecs.entity.User;
import com.smecs.service.CategoryService;
import com.smecs.service.InventoryService;
import com.smecs.service.ProductService;
import com.smecs.service.ReviewService;
import com.smecs.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
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
    private final InventoryService inventoryService;
    private final UserService userService;
    private final ReviewService reviewService;

    // --- Products ---

    @QueryMapping
    public PagedResponseDTO<ProductDTO> products(@Argument String categoryId, @Argument Integer page,
            @Argument Integer size,
            @Argument String sort) {
        int pageNo = (page != null) ? page : 1;
        int pageSize = (size != null) ? size : 10;
        String sortStr = (sort != null) ? sort : "id,asc";

         Long catId = (categoryId != null) ? Long.parseLong(categoryId) : null;
         ProductQuery productQuery = ProductQuery.builder()
                 .categoryId(catId)
                 .page(pageNo)
                 .size(pageSize)
                 .sort(sortStr)
                 .build();
        return productService.getProducts(productQuery);
    }

    @QueryMapping
    public ProductDTO productById(@Argument String id) {
        return productService.getProductById(Long.parseLong(id));
    }

    @SchemaMapping(typeName = "Product", field = "category")
    public GqlCategory category(ProductDTO product) {
        if (product.getCategoryId() == null)
            return null;
        CategoryDTO dto = categoryService.getCategoryById(product.getCategoryId(), false);
        return toGqlCategory(dto);
    }

    @MutationMapping
    @RequireRole("admin")
    public ProductDTO createProduct(@Argument String name, @Argument String description, @Argument Double price,
            @Argument String categoryId) {
        CreateProductRequestDTO request = new CreateProductRequestDTO();
        request.setName(name);
        request.setDescription(description);
        request.setPrice(price);
        request.setCategoryId(Long.parseLong(categoryId));
        return productService.createProduct(request);
    }

    // --- Inventories ---

    @QueryMapping
    @RequireRole("admin")
    public PagedResponseDTO<InventoryDTO> inventories(@Argument Integer page, @Argument Integer size,
            @Argument String sort,
            @Argument String query) {
        int pageNo = (page != null) ? page : 1;
        int pageSize = (size != null) ? size : 10;
        String sortStr = (sort != null) ? sort : "id,asc";

        InventoryQuery inventoryQuery = InventoryQuery.builder()
                .query(query)
                .page(pageNo)
                .size(pageSize)
                .sort(sortStr)
                .build();

        return inventoryService.searchInventory(inventoryQuery);
    }

    @QueryMapping
    @RequireRole("admin")
    public InventoryDTO inventoryById(@Argument String id) {
        return inventoryService.getInventoryById(Long.parseLong(id));
    }

    @SchemaMapping(typeName = "Inventory", field = "product")
    public ProductDTO product(InventoryDTO inventory) {
        if (inventory.getProductId() == null)
            return null;
        return productService.getProductById(inventory.getProductId());
    }

    @MutationMapping
    @RequireRole("admin")
    public InventoryDTO createInventory(@Argument CreateInventoryInput input) {
        CreateInventoryRequestDTO request = new CreateInventoryRequestDTO();
        request.setQuantity(input.quantity());

        // Map product input to nested ProductRequest
        CreateInventoryRequestDTO.ProductRequest productRequest = new CreateInventoryRequestDTO.ProductRequest();
        productRequest.setName(input.product().name());
        productRequest.setDescription(input.product().description());
        productRequest.setPrice(input.product().price());
        productRequest.setCategoryId(Long.parseLong(input.product().categoryId()));
        productRequest.setImageUrl(input.product().imageUrl());

        request.setProduct(productRequest);

        return inventoryService.createInventory(request);
    }

    // --- Categories ---

    @QueryMapping
    public PagedResponseDTO<GqlCategory> categories(@Argument Integer page, @Argument Integer size,
            @Argument String sort,
            @Argument String query) {
        int pageNo = (page != null) ? page : 1;
        int pageSize = (size != null) ? size : 10;
        String sortStr = (sort != null) ? sort : "id,asc";
        String searchQuery = (query != null) ? query : "";

        CategoryQuery categoryQuery = CategoryQuery.builder()
                .name(searchQuery)
                .description(searchQuery)
                .page(pageNo)
                .size(pageSize)
                .sort(sortStr)
                .includeRelatedImages(false)
                .build();
        PagedResponseDTO<CategoryDTO> serviceResponse = categoryService.getCategories(categoryQuery);

        PagedResponseDTO<GqlCategory> response = new PagedResponseDTO<>();
        response.setPage(serviceResponse.getPage());
        response.setContent(serviceResponse.getContent().stream()
                .map(this::toGqlCategory)
                .collect(Collectors.toList()));

        return response;
    }

    @QueryMapping
    public GqlCategory categoryById(@Argument String id) {
        return toGqlCategory(categoryService.getCategoryById(Long.parseLong(id), false));
    }

    @MutationMapping
    @RequireRole("admin")
    public GqlCategory createCategory(@Argument String name, @Argument String description, @Argument String imageUrl) {
        CategoryDTO dto = new CategoryDTO();
        dto.setCategoryName(name);
        dto.setDescription(description);
        dto.setImageUrl(imageUrl);

        CategoryDTO created = categoryService.createCategory(dto);
        return toGqlCategory(created);
    }

    // --- Users ---

    @QueryMapping
    @RequireRole("admin")
    public List<GqlUser> users() {
        return userService.getAllUsers().stream()
                .map(this::toGqlUser)
                .collect(Collectors.toList());
    }

    @QueryMapping
    @RequireRole("admin")
    public GqlUser userById(@Argument String id) {
        return toGqlUser(userService.findById(Long.parseLong(id)));
    }

    // --- Reviews ---

    @QueryMapping
    public PagedResponseDTO<GqlReview> reviewsByProduct(@Argument String productId, @Argument Integer page,
            @Argument Integer size) {
        int pageNo = (page != null) ? page : 1;
        int pageSize = (size != null) ? size : 10;

        Pageable pageable = PageRequest.of(pageNo - 1, pageSize);
        PagedResponseDTO<com.smecs.dto.ReviewDTO> serviceResponse = reviewService.getReviewsByProduct(
                Long.parseLong(productId), pageable);

        PagedResponseDTO<GqlReview> response = new PagedResponseDTO<>();
        response.setPage(serviceResponse.getPage());
        response.setContent(serviceResponse.getContent().stream()
                .map(this::toGqlReview)
                .collect(Collectors.toList()));

        return response;
    }

    @SchemaMapping(typeName = "Review", field = "user")
    public GqlUser user(GqlReview review) {
        if (review.userId() == null)
            return null;
        return toGqlUser(userService.findById(Long.parseLong(review.userId())));
    }

    // Helper
    private GqlCategory toGqlCategory(CategoryDTO dto) {
        if (dto == null)
            return null;
        return new GqlCategory(
                String.valueOf(dto.getCategoryId()),
                dto.getCategoryName(),
                dto.getDescription(),
                dto.getImageUrl());
    }

    private GqlUser toGqlUser(User user) {
        if (user == null)
            return null;
        return new GqlUser(
                String.valueOf(user.getId()),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt() != null ? user.getCreatedAt().toString() : null);
    }

    private GqlReview toGqlReview(com.smecs.dto.ReviewDTO dto) {
        if (dto == null)
            return null;
        return new GqlReview(
                String.valueOf(dto.getId()),
                String.valueOf(dto.getUserId()),
                String.valueOf(dto.getProductId()),
                dto.getRating(),
                dto.getComment(),
                dto.getCreatedAt() != null ? dto.getCreatedAt().toString() : null);
    }

    public record GqlCategory(String id, String name, String description, String imageUrl) {
    }

    public record GqlUser(String id, String username, String email, String role, String createdAt) {
    }

    public record ProductInput(String name, String description, Double price, String categoryId, String imageUrl) {
    }

    public record CreateInventoryInput(Integer quantity, ProductInput product) {
    }

    public record GqlReview(String id, String userId, String productId, Integer rating, String comment,
            String createdAt) {
    }
}
