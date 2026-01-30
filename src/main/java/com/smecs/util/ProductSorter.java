//package com.smecs.util;
//
//import com.smecs.model.Product;
//
//import java.util.Comparator;
//import java.util.List;
//
///**
// * Utility class for sorting products using various algorithms and criteria.
// * Provides multiple sorting strategies optimized for different use cases.
// */
//public class ProductSorter {
//
//    /**
//     * Enum for different sort criteria.
//     */
//    public enum SortCriteria {
//        NAME_ASC("Name (A-Z)"),
//        NAME_DESC("Name (Z-A)"),
//        PRICE_ASC("Price (Low to High)"),
//        PRICE_DESC("Price (High to Low)"),
//        CATEGORY_ASC("Category (A-Z)"),
//        CATEGORY_DESC("Category (Z-A)"),
//        DATE_ASC("Oldest First"),
//        DATE_DESC("Newest First");
//
//        private final String displayName;
//
//        SortCriteria(String displayName) {
//            this.displayName = displayName;
//        }
//
//        public String getDisplayName() {
//            return displayName;
//        }
//
//        @Override
//        public String toString() {
//            return displayName;
//        }
//    }
//
//    /**
//     * Sort products in-place using the specified criteria.
//     * Uses optimized Java Collections.sort which implements TimSort.
//     */
//    public static void sort(List<Product> products, SortCriteria criteria) {
//        if (products == null || products.isEmpty()) {
//            return;
//        }
//
//        Comparator<Product> comparator = getComparator(criteria);
//        products.sort(comparator);
//    }
//
//    /**
//     * Get comparator for the specified criteria.
//     */
//    public static Comparator<Product> getComparator(SortCriteria criteria) {
//        switch (criteria) {
//            case NAME_ASC:
//                return Comparator.comparing(
//                        p -> p.getProductName() != null ? p.getProductName().toLowerCase() : "",
//                        Comparator.nullsLast(Comparator.naturalOrder())
//                );
//            case NAME_DESC:
//                return Comparator.comparing(
//                        (Product p) -> p.getProductName() != null ? p.getProductName().toLowerCase() : "",
//                        Comparator.nullsLast(Comparator.reverseOrder())
//                );
//            case PRICE_ASC:
//                return Comparator.comparing(
//                        Product::getPrice,
//                        Comparator.nullsLast(Comparator.naturalOrder())
//                );
//            case PRICE_DESC:
//                return Comparator.comparing(
//                        Product::getPrice,
//                        Comparator.nullsLast(Comparator.reverseOrder())
//                );
//            case CATEGORY_ASC:
//                return Comparator.comparing(
//                        p -> p.getCategoryName() != null ? p.getCategoryName().toLowerCase() : "",
//                        Comparator.nullsLast(Comparator.naturalOrder())
//                );
//            case CATEGORY_DESC:
//                return Comparator.comparing(
//                        (Product p) -> p.getCategoryName() != null ? p.getCategoryName().toLowerCase() : "",
//                        Comparator.nullsLast(Comparator.reverseOrder())
//                );
//            case DATE_ASC:
//                return Comparator.comparing(
//                        Product::getCreatedAt,
//                        Comparator.nullsLast(Comparator.naturalOrder())
//                );
//            case DATE_DESC:
//                return Comparator.comparing(
//                        Product::getCreatedAt,
//                        Comparator.nullsLast(Comparator.reverseOrder())
//                );
//            default:
//                return Comparator.comparing(Product::getProductId);
//        }
//    }
//
//    /**
//     * Quick Sort implementation for educational purposes.
//     * Demonstrates a common DSA sorting algorithm.
//     */
//    public static void quickSort(List<Product> products, SortCriteria criteria) {
//        if (products == null || products.size() <= 1) {
//            return;
//        }
//
//        Comparator<Product> comparator = getComparator(criteria);
//        quickSortHelper(products, 0, products.size() - 1, comparator);
//    }
//
//    private static void quickSortHelper(List<Product> products, int low, int high,
//                                        Comparator<Product> comparator) {
//        if (low < high) {
//            int pivotIndex = partition(products, low, high, comparator);
//            quickSortHelper(products, low, pivotIndex - 1, comparator);
//            quickSortHelper(products, pivotIndex + 1, high, comparator);
//        }
//    }
//
//    private static int partition(List<Product> products, int low, int high,
//                                 Comparator<Product> comparator) {
//        Product pivot = products.get(high);
//        int i = low - 1;
//
//        for (int j = low; j < high; j++) {
//            if (comparator.compare(products.get(j), pivot) <= 0) {
//                i++;
//                swap(products, i, j);
//            }
//        }
//
//        swap(products, i + 1, high);
//        return i + 1;
//    }
//
//    /**
//     * Merge Sort implementation for educational purposes.
//     * Stable sort with O(n log n) time complexity.
//     */
//    public static void mergeSort(List<Product> products, SortCriteria criteria) {
//        if (products == null || products.size() <= 1) {
//            return;
//        }
//
//        Comparator<Product> comparator = getComparator(criteria);
//        mergeSortHelper(products, 0, products.size() - 1, comparator);
//    }
//
//    private static void mergeSortHelper(List<Product> products, int left, int right,
//                                        Comparator<Product> comparator) {
//        if (left < right) {
//            int mid = left + (right - left) / 2;
//            mergeSortHelper(products, left, mid, comparator);
//            mergeSortHelper(products, mid + 1, right, comparator);
//            merge(products, left, mid, right, comparator);
//        }
//    }
//
//    private static void merge(List<Product> products, int left, int mid, int right,
//                              Comparator<Product> comparator) {
//        int n1 = mid - left + 1;
//        int n2 = right - mid;
//
//        Product[] leftArray = new Product[n1];
//        Product[] rightArray = new Product[n2];
//
//        for (int i = 0; i < n1; i++) {
//            leftArray[i] = products.get(left + i);
//        }
//        for (int j = 0; j < n2; j++) {
//            rightArray[j] = products.get(mid + 1 + j);
//        }
//
//        int i = 0, j = 0, k = left;
//
//        while (i < n1 && j < n2) {
//            if (comparator.compare(leftArray[i], rightArray[j]) <= 0) {
//                products.set(k, leftArray[i]);
//                i++;
//            } else {
//                products.set(k, rightArray[j]);
//                j++;
//            }
//            k++;
//        }
//
//        while (i < n1) {
//            products.set(k, leftArray[i]);
//            i++;
//            k++;
//        }
//
//        while (j < n2) {
//            products.set(k, rightArray[j]);
//            j++;
//            k++;
//        }
//    }
//
//    private static void swap(List<Product> products, int i, int j) {
//        Product temp = products.get(i);
//        products.set(i, products.get(j));
//        products.set(j, temp);
//    }
//}

