package com.smecs.dao.impl;

import com.smecs.dao.ProductDAO;
import com.smecs.entity.Product;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Repository
public class ProductDAOImpl implements ProductDAO {

    @PersistenceContext
    private EntityManager entityManager;

    // Static maps for property-to-column mapping
    private static final Map<String, String> PROPERTY_TO_COLUMN = Map.of(
            "id", "id",
            "name", "name",
            "description", "description",
            "price", "price",
            "imageUrl", "image_url",
            "categoryId", "category_id");

    private static final Set<String> VALID_COLUMNS = Set.of(
            "id", "name", "description", "price", "image_url", "category_id");

    @Override
    @Transactional
    public Product save(Product product) {
        if (product.getId() == null) {
            entityManager.persist(product);
            return product;
        } else {
            return entityManager.merge(product);
        }
    }

    @Override
    public Optional<Product> findById(Long id) {
        return Optional.ofNullable(entityManager.find(Product.class, id));
    }

    @Override
    public boolean existsById(Long id) {
        return entityManager.find(Product.class, id) != null;
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        Product product = entityManager.find(Product.class, id);
        if (product != null) {
            entityManager.remove(product);
        }
    }

    @Override
    public Page<Product> findAll(Specification<Product> spec, Pageable pageable) {
        // For backward compatibility, delegate to the native SQL implementation
        // Since specifications are complex to parse, we'll do a simple search
        // This method is kept for interface compatibility but searchProducts is
        // preferred
        return searchProducts("", "", null, pageable);
    }

    /**
     * Builds the ORDER BY clause from Spring's Sort object.
     * This demonstrates manual SQL construction for sorting.
     *
     * @param sort Sort object from Pageable
     * @return SQL ORDER BY clause string
     */
    private String buildOrderByClause(Sort sort) {
        if (sort.isUnsorted()) {
            return " ORDER BY p.id ASC"; // Default sorting
        }

        StringBuilder orderBy = new StringBuilder(" ORDER BY ");
        List<String> orderClauses = new ArrayList<>();

        for (Sort.Order order : sort) {
            String property = order.getProperty();
            String direction = order.getDirection().name(); // ASC or DESC

            // Map entity property names to database column names
            String columnName = mapPropertyToColumn(property);

            // Prevent SQL injection by validating column names
            if (isValidColumn(columnName)) {
                orderClauses.add("p." + columnName + " " + direction);
            }
        }

        if (orderClauses.isEmpty()) {
            return " ORDER BY p.id ASC";
        }

        orderBy.append(String.join(", ", orderClauses));
        return orderBy.toString();
    }

    /**
     * Maps Java entity property names to database column names.
     * This handles the difference between camelCase and snake_case.
     */
    private String mapPropertyToColumn(String property) {
        return PROPERTY_TO_COLUMN.getOrDefault(property, "id");
    }

    /**
     * Validates that the column name is one we allow for sorting.
     * This prevents SQL injection attacks.
     */
    private boolean isValidColumn(String columnName) {
        return VALID_COLUMNS.contains(columnName);
    }

    /**
     * Search products using native SQL with manual pagination and sorting.
     * This method demonstrates low-level SQL implementation for academic purposes.
     *
     * @param nameQuery        Search term for product name
     * @param descriptionQuery Search term for product description
     * @param categoryId       Category ID filter (optional)
     * @param pageable         Contains pagination and sorting parameters
     * @return Page of products matching the search criteria
     */
    @Override
    public Page<Product> searchProducts(String nameQuery, String descriptionQuery, Long categoryId, Pageable pageable) {
        // Step 1: Build WHERE clause for filtering
        List<Object> parameters = new ArrayList<>();
        String whereClause = buildWhereClause(nameQuery, descriptionQuery, categoryId, parameters);

        // Step 2: Build ORDER BY clause from Sort specification
        String orderByClause = buildOrderByClause(pageable.getSort());

        // Step 3: Calculate LIMIT and OFFSET for pagination
        int limit = pageable.getPageSize();
        int offset = (int) pageable.getOffset();

        // Step 4: Construct and execute the main SELECT query
        String dataQuery = "SELECT p.id, p.name, p.description, p.price, p.image_url, p.category_id " +
                "FROM products p" + whereClause + orderByClause + " LIMIT ? OFFSET ?";

        // System.out.println("Generated SQL Query: " + dataQuery);
        // System.out.println("Pagination: LIMIT=" + limit + ", OFFSET=" + offset);

        Query query = entityManager.createNativeQuery(dataQuery);
        setQueryParameters(query, parameters, limit, offset);

        // Step 5: Map results to Product entities
        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();
        List<Product> products = mapResultsToProducts(results);

        // Step 6: Execute COUNT query
        long total = executeCountQuery(whereClause, parameters);

        // Step 7: Return paginated results
        return new PageImpl<>(products, pageable, total);
    }

    /**
     * Builds the WHERE clause for filtering by name and description.
     */
    private String buildWhereClause(String nameQuery, String descriptionQuery, Long categoryId, List<Object> parameters) {
        List<String> andConditions = new ArrayList<>();

        // 1. Text Search Group (OR logic between name and description)
        List<String> orConditions = new ArrayList<>();
        if (nameQuery != null && !nameQuery.isBlank()) {
            orConditions.add("LOWER(p.name) LIKE LOWER(?)");
            parameters.add("%" + nameQuery + "%");
        }
        if (descriptionQuery != null && !descriptionQuery.isBlank()) {
            orConditions.add("LOWER(p.description) LIKE LOWER(?)");
            parameters.add("%" + descriptionQuery + "%");
        }

        if (!orConditions.isEmpty()) {
            andConditions.add("(" + String.join(" OR ", orConditions) + ")");
        }

        // 2. Category Filter (AND logic)
        if (categoryId != null) {
            andConditions.add("p.category_id = ?");
            parameters.add(categoryId);
        }

        if (andConditions.isEmpty()) {
            return "";
        }

        return " WHERE " + String.join(" AND ", andConditions);
    }

    /**
     * Sets query parameters for data query.
     */
    private void setQueryParameters(Query query, List<Object> parameters, int limit, int offset) {
        for (int i = 0; i < parameters.size(); i++) {
            query.setParameter(i + 1, parameters.get(i));
        }
        query.setParameter(parameters.size() + 1, limit);
        query.setParameter(parameters.size() + 2, offset);
    }

    /**
     * Executes the COUNT query to get total matching records.
     */
    private long executeCountQuery(String whereClause, List<Object> parameters) {
        String countQuery = "SELECT COUNT(*) FROM products p" + whereClause;
        Query countQ = entityManager.createNativeQuery(countQuery);

        for (int i = 0; i < parameters.size(); i++) {
            countQ.setParameter(i + 1, parameters.get(i));
        }

        return ((Number) countQ.getSingleResult()).longValue();
    }

    /**
     * Maps raw SQL result rows to Product entity objects.
     * Each row contains: [id, name, description, price, image_url, category_id]
     */
    private List<Product> mapResultsToProducts(List<Object[]> results) {
        List<Product> products = new ArrayList<>();

        for (Object[] row : results) {
            Product product = new Product();
            product.setId(toLong(row[0]));
            product.setName((String) row[1]);
            product.setDescription((String) row[2]);
            product.setPrice(toDouble(row[3]));
            product.setImageUrl((String) row[4]);
            product.setCategoryId(toLong(row[5]));
            products.add(product);
        }
        return products;
    }

    /**
     * Safely converts Object to Long.
     */
    private Long toLong(Object value) {
        return value != null ? ((Number) value).longValue() : null;
    }

    /**
     * Safely converts Object to Double.
     */
    private Double toDouble(Object value) {
        return value != null ? ((Number) value).doubleValue() : null;
    }

    @Override
    public List<Product> findTop5ByCategory(Long categoryId) {
        String sql = "SELECT id, name, description, price, image_url, category_id FROM products WHERE category_id = ? LIMIT 5";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter(1, categoryId);

        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();
        return mapResultsToProducts(results);
    }

    @Override
    public long countByCategoryId(Long categoryId) {
        String sql = "SELECT COUNT(*) FROM products WHERE category_id = ?";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter(1, categoryId);
        return ((Number) query.getSingleResult()).longValue();
    }
}
