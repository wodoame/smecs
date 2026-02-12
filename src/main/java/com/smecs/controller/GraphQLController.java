package com.smecs.controller;

import com.smecs.annotation.RequireRole;
import com.smecs.dto.CategoryDTO;
import com.smecs.dto.CreateInventoryRequestDTO;
import com.smecs.dto.CreateProductRequestDTO;
import com.smecs.dto.InventoryDTO;
import com.smecs.dto.PagedResponseDTO;
import com.smecs.dto.ProductDTO;
import com.smecs.entity.User;
import com.smecs.service.CategoryService;
import com.smecs.service.InventoryService;
import com.smecs.service.ProductService;
import com.smecs.service.UserService;
import com.smecs.util.PaginationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    // --- Products ---

    @QueryMapping
    public PagedResponseDTO<ProductDTO> products(@Argument String categoryId, @Argument Integer page,
            @Argument Integer size,
            @Argument String sort) {
        int pageNo = (page != null) ? page : 1;
        int pageSize = (size != null) ? size : 10;
        String sortStr = (sort != null) ? sort : "id,asc";

        Sort sortSpec = PaginationUtils.parseSort(sortStr, "id");
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize, sortSpec);

        Long catId = (categoryId != null) ? Long.parseLong(categoryId) : null;
        return productService.getProducts(null, null, catId, pageable);
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

        return inventoryService.searchInventory(query, pageNo, pageSize, sortStr);
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
    public PagedResponseDTO<GqlCategory> categories(@Argument Integer page, @Argument Integer size, @Argument String sort,
                                        @Argument String query) {
        int pageNo = (page != null) ? page : 1;
        int pageSize = (size != null) ? size : 10;
        String sortStr = (sort != null) ? sort : "id,asc";
        String searchQuery = (query != null) ? query : "";

        PagedResponseDTO<CategoryDTO> serviceResponse = categoryService.getCategories(searchQuery, pageNo, pageSize, sortStr, false);

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

    public record GqlCategory(String id, String name, String description, String imageUrl) {
    }

    public record GqlUser(String id, String username, String email, String role, String createdAt) {
    }

    public record ProductInput(String name, String description, Double price, String categoryId, String imageUrl) {
    }

    public record CreateInventoryInput(Integer quantity, ProductInput product) {
    }
}
