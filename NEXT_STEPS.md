# Next Steps

You recently updated the database schema in `src/main/resources/sql/schema.sql` to include additional entities required for the full E-Commerce system.

The following tables were added/updated:
- `Inventory`
- `Orders`
- `OrderItems`
- `Reviews`

## To Do List

To fully integrate these new tables into the Java application, you need to implement the corresponding Models and DAOs.

### 1. Create Missing Models (`src/main/java/com/smecs/model/`)
- [ ] **Inventory.java**
    - Fields: `inventoryId`, `productId`, `quantity`, `lastUpdated`
- [ ] **Order.java** (corresponding to `Orders` table)
    - Fields: `orderId`, `userId`, `totalAmount`, `status`, `createdAt`
- [ ] **OrderItem.java**
    - Fields: `orderItemId`, `orderId`, `productId`, `quantity`, `price`
- [ ] **Review.java**
    - Fields: `reviewId`, `userId`, `productId`, `rating`, `comment`, `createdAt`

### 2. Create Missing DAOs (`src/main/java/com/smecs/dao/`)
- [ ] **InventoryDAO.java**
    - Methods: `updateStock(int productId, int quantity)`, `getStock(int productId)`, `checkAvailability(int productId, int quantity)`
- [ ] **OrderDAO.java**
    - Methods: `createOrder(Order order)`, `getOrderById(int orderId)`, `getOrdersByUserId(int userId)`, `updateOrderStatus(int orderId, String status)`
- [ ] **OrderItemDAO.java**
    - Methods: `addOrderItem(OrderItem item)`, `getItemsByOrderId(int orderId)`
- [ ] **ReviewDAO.java**
    - Methods: `addReview(Review review)`, `getReviewsByProductId(int productId)`

### 3. Update Services (`src/main/java/com/smecs/service/`)
Once the DAOs are in place, you may need to update or create services to handle business logic using these new data access objects.
- [ ] Create `OrderService` (handle order placement, inventory checking)
- [ ] Create `InventoryService` (manage stock updates)
- [ ] Create `ReviewService`

### 4. Database Setup
- [ ] Run the updated `src/main/resources/sql/schema.sql` script against your database to ensure tables are created.

