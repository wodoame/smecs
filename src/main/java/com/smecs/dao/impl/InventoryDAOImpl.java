package com.smecs.dao.impl;

import com.smecs.dao.InventoryDAO;
import com.smecs.entity.Inventory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Repository
public class InventoryDAOImpl implements InventoryDAO {

    @PersistenceContext
    private EntityManager entityManager;

    // Static maps for property-to-column mapping
    private static final Map<String, String> PROPERTY_TO_COLUMN = Map.of(
            "id", "i.id",
            "productId", "i.product_id",
            "quantity", "i.quantity"
    );

    private static final Set<String> VALID_COLUMNS = Set.of(
            "i.id", "i.product_id", "i.quantity"
    );

    @Override
    @Transactional
    public Inventory save(Inventory inventory) {
        if (inventory.getId() == null) {
            entityManager.persist(inventory);
            return inventory;
        } else {
            return entityManager.merge(inventory);
        }
    }

    @Override
    public Optional<Inventory> findById(Long id) {
        return Optional.ofNullable(entityManager.find(Inventory.class, id));
    }

    @Override
    public Optional<Inventory> findByProductId(Long productId) {
        List<Inventory> results = entityManager.createQuery("SELECT i FROM Inventory i WHERE i.productId = :productId", Inventory.class)
                .setParameter("productId", productId)
                .getResultList();
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public boolean existsById(Long id) {
        return entityManager.find(Inventory.class, id) != null;
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        Inventory inventory = entityManager.find(Inventory.class, id);
        if (inventory != null) {
            entityManager.remove(inventory);
        }
    }

    @Override
    public Page<Inventory> searchInventory(String searchQuery, Pageable pageable) {
        // Step 1: Build ORDER BY clause
        String orderByClause = buildOrderByClause(pageable.getSort());

        // Step 2: Calculate LIMIT and OFFSET
        int limit = pageable.getPageSize();
        int offset = (int) pageable.getOffset();

        // Step 3: Build base query with Joins
        StringBuilder baseQuery = new StringBuilder("FROM inventory i ")
                .append("JOIN products p ON i.product_id = p.id ")
                .append("LEFT JOIN categories c ON p.category_id = c.category_id ");

        List<Object> params = new ArrayList<>();
        if (searchQuery != null && !searchQuery.isBlank()) {
            baseQuery.append("WHERE LOWER(p.name) LIKE LOWER(?) OR ")
                    .append("LOWER(p.description) LIKE LOWER(?) OR ")
                    .append("LOWER(c.name) LIKE LOWER(?) OR ")
                    .append("LOWER(c.description) LIKE LOWER(?) ");
            String likePattern = "%" + searchQuery + "%";
            params.add(likePattern); // for p.name
            params.add(likePattern); // for p.description
            params.add(likePattern); // for c.name
            params.add(likePattern); // for c.description
        }

        // Step 4: Construct and execute data query
        String dataQuery = "SELECT i.id, i.product_id, i.quantity " +
                baseQuery.toString() +
                orderByClause + " LIMIT ? OFFSET ?";

        // System.out.println("Generated SQL Query: " + dataQuery);
        // System.out.println("Pagination: LIMIT=" + limit + ", OFFSET=" + offset);

        Query query = entityManager.createNativeQuery(dataQuery);

        // Set search params (1-based index)
        int paramIndex = 1;
        for (Object param : params) {
            query.setParameter(paramIndex++, param);
        }

        // Set pagination params
        query.setParameter(paramIndex++, limit);
        query.setParameter(paramIndex++, offset);

        // Step 5: Map results
        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();
        List<Inventory> inventories = mapResultsToInventory(results);

        // Step 6: Execute count query
        long total = executeCountQuery(baseQuery.toString(), params);

        // Step 7: Return paginated results
        return new PageImpl<>(inventories, pageable, total);
    }


    private String buildOrderByClause(Sort sort) {
        if (sort.isUnsorted()) {
            return " ORDER BY i.id ASC";
        }

        StringBuilder orderBy = new StringBuilder(" ORDER BY ");
        List<String> orderClauses = new ArrayList<>();

        for (Sort.Order order : sort) {
            String property = order.getProperty();
            String direction = order.getDirection().name();
            String columnName = mapPropertyToColumn(property);

            if (isValidColumn(columnName)) {
                orderClauses.add(columnName + " " + direction);
            }
        }

        if (orderClauses.isEmpty()) {
            return " ORDER BY i.id ASC";
        }

        orderBy.append(String.join(", ", orderClauses));
        return orderBy.toString();
    }

    private String mapPropertyToColumn(String property) {
        return PROPERTY_TO_COLUMN.getOrDefault(property, "i.id");
    }

    private boolean isValidColumn(String columnName) {
        return VALID_COLUMNS.contains(columnName);
    }

    private List<Inventory> mapResultsToInventory(List<Object[]> results) {
        List<Inventory> inventories = new ArrayList<>();
        for (Object[] row : results) {
            Inventory inventory = new Inventory();
            inventory.setId(toLong(row[0]));
            inventory.setProductId(toLong(row[1]));
            inventory.setQuantity(row[2] != null ? ((Number) row[2]).intValue() : null);
            inventories.add(inventory);
        }
        return inventories;
    }

    private long executeCountQuery(String baseQuery, List<Object> params) {
        String countQuery = "SELECT COUNT(*) " + baseQuery;
        Query countQ = entityManager.createNativeQuery(countQuery);

        int paramIndex = 1;
        for (Object param : params) {
            countQ.setParameter(paramIndex++, param);
        }

        return ((Number) countQ.getSingleResult()).longValue();
    }

    private Long toLong(Object value) {
        return value != null ? ((Number) value).longValue() : null;
    }
}

