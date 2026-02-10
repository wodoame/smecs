package com.smecs.dao.impl;

import com.smecs.dao.CategoryDAO;
import com.smecs.entity.Category;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Repository
public class CategoryDAOImpl implements CategoryDAO {

    @PersistenceContext
    private EntityManager entityManager;

    // Static maps for property-to-column mapping (performance optimization)
    private static final Map<String, String> PROPERTY_TO_COLUMN = Map.of(
            "id", "id",
            "name", "name",
            "description", "description",
            "imageUrl", "image_url"
    );

    private static final Set<String> VALID_COLUMNS = Set.of(
            "id", "name", "description", "image_url"
    );

    @Override
    @Transactional
    public Category save(Category category) {
        if (category.getId() == null) {
            entityManager.persist(category);
            return category;
        } else {
            return entityManager.merge(category);
        }
    }

    @Override
    public Optional<Category> findById(Long id) {
        return Optional.ofNullable(entityManager.find(Category.class, id));
    }

    @Override
    public boolean existsById(Long id) {
        return entityManager.find(Category.class, id) != null;
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        Category category = entityManager.find(Category.class, id);
        if (category != null) {
            entityManager.remove(category);
        }
    }

    @Override
    public Page<Category> findAll(Specification<Category> spec, Pageable pageable) {
        // For backward compatibility, delegate to the native SQL implementation
        return searchCategories("", "", pageable);
    }

    @Override
    public Page<Category> searchCategories(String nameQuery, String descriptionQuery, Pageable pageable) {
        // Step 1: Build WHERE clause for filtering
        List<Object> parameters = new ArrayList<>();
        String whereClause = buildWhereClause(nameQuery, descriptionQuery, parameters);

        // Step 2: Build ORDER BY clause from Sort specification
        String orderByClause = buildOrderByClause(pageable.getSort());

        // Step 3: Calculate LIMIT and OFFSET for pagination
        int limit = pageable.getPageSize();
        int offset = (int) pageable.getOffset();

        // Step 4: Construct and execute the main SELECT query
        String dataQuery = "SELECT c.id, c.name, c.description, c.image_url " +
                          "FROM categories c" + whereClause + orderByClause + " LIMIT ? OFFSET ?";

        System.out.println("Generated SQL Query: " + dataQuery);
        System.out.println("Pagination: LIMIT=" + limit + ", OFFSET=" + offset);

        Query query = entityManager.createNativeQuery(dataQuery);
        setQueryParameters(query, parameters, limit, offset);

        // Step 5: Map results to Category entities
        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();
        List<Category> categories = mapResultsToCategories(results);

        // Step 6: Execute COUNT query
        long total = executeCountQuery(whereClause, parameters);

        // Step 7: Return paginated results
        return new PageImpl<>(categories, pageable, total);
    }

    /**
     * Builds the WHERE clause for filtering by name and description.
     */
    private String buildWhereClause(String nameQuery, String descriptionQuery, List<Object> parameters) {
        if ((nameQuery == null || nameQuery.isBlank()) &&
            (descriptionQuery == null || descriptionQuery.isBlank())) {
            return "";
        }

        List<String> conditions = new ArrayList<>();

        if (nameQuery != null && !nameQuery.isBlank()) {
            conditions.add("LOWER(c.name) LIKE LOWER(?)");
            parameters.add("%" + nameQuery + "%");
        }

        if (descriptionQuery != null && !descriptionQuery.isBlank()) {
            conditions.add("LOWER(c.description) LIKE LOWER(?)");
            parameters.add("%" + descriptionQuery + "%");
        }

        return " WHERE " + String.join(" OR ", conditions);
    }

    /**
     * Builds the ORDER BY clause from Spring's Sort object.
     */
    private String buildOrderByClause(Sort sort) {
        if (sort.isUnsorted()) {
            return " ORDER BY c.id ASC"; // Default sorting
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
                orderClauses.add("c." + columnName + " " + direction);
            }
        }

        if (orderClauses.isEmpty()) {
            return " ORDER BY c.id ASC";
        }

        orderBy.append(String.join(", ", orderClauses));
        return orderBy.toString();
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
        String countQuery = "SELECT COUNT(*) FROM categories c" + whereClause;
        Query countQ = entityManager.createNativeQuery(countQuery);

        for (int i = 0; i < parameters.size(); i++) {
            countQ.setParameter(i + 1, parameters.get(i));
        }

        return ((Number) countQ.getSingleResult()).longValue();
    }

    /**
     * Maps raw SQL result rows to Category entity objects.
     */
    private List<Category> mapResultsToCategories(List<Object[]> results) {
        List<Category> categories = new ArrayList<>();

        for (Object[] row : results) {
            Category category = new Category();
            category.setId(toLong(row[0]));
            category.setName((String) row[1]);
            category.setDescription((String) row[2]);
            category.setImageUrl((String) row[3]);
            categories.add(category);
        }
        return categories;
    }

    /**
     * Maps Java entity property names to database column names.
     */
    private String mapPropertyToColumn(String property) {
        return PROPERTY_TO_COLUMN.getOrDefault(property, "id");
    }

    /**
     * Validates that the column name is one we allow for sorting.
     */
    private boolean isValidColumn(String columnName) {
        return VALID_COLUMNS.contains(columnName);
    }

    /**
     * Safely converts Object to Long.
     */
    private Long toLong(Object value) {
        return value != null ? ((Number) value).longValue() : null;
    }
}

