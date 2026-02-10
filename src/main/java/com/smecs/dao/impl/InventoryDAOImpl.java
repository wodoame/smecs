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
    public Page<Inventory> searchInventory(Pageable pageable) {
        // Step 1: Build ORDER BY clause
        String orderByClause = buildOrderByClause(pageable.getSort());

        // Step 2: Calculate LIMIT and OFFSET
        int limit = pageable.getPageSize();
        int offset = (int) pageable.getOffset();

        // Step 3: Construct and execute query
        // Simple select from inventory table
        String baseQuery = "FROM inventory i";

        String dataQuery = "SELECT i.id, i.product_id, i.quantity " +
                           baseQuery + orderByClause + " LIMIT ? OFFSET ?";

        System.out.println("Generated SQL Query: " + dataQuery);
        System.out.println("Pagination: LIMIT=" + limit + ", OFFSET=" + offset);

        Query query = entityManager.createNativeQuery(dataQuery);
        setQueryParameters(query, limit, offset);

        // Step 4: Map results
        List<Object[]> results = query.getResultList();
        List<Inventory> inventories = mapResultsToInventory(results);

        // Step 5: Execute count query
        long total = executeCountQuery(baseQuery);

        // Step 6: Return paginated results
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

    private void setQueryParameters(Query query, int limit, int offset) {
        query.setParameter(1, limit);
        query.setParameter(2, offset);
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

    private long executeCountQuery(String baseQuery) {
        String countQuery = "SELECT COUNT(*) " + baseQuery;
        Query countQ = entityManager.createNativeQuery(countQuery);

        return ((Number) countQ.getSingleResult()).longValue();
    }

    private Long toLong(Object value) {
        return value != null ? ((Number) value).longValue() : null;
    }
}

