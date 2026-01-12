package com.smecs.util;

import com.smecs.cache.ProductCache;
import com.smecs.model.Product;
import com.smecs.service.ProductService;

import java.util.ArrayList;
import java.util.List;

/**
 * Performance benchmarking utility for Epic 3.
 * Compares search and retrieval performance with and without caching/indexing.
 */
public class PerformanceBenchmark {
    
    private final ProductService productService;
    
    public PerformanceBenchmark() {
        this.productService = new ProductService();
    }
    
    /**
     * Run all benchmarks and return results as a formatted string.
     */
    public String runAllBenchmarks() {
        StringBuilder report = new StringBuilder();
        report.append("=== EPIC 3: Performance Benchmark Report ===\n\n");
        
        // Clear cache and stats before benchmarking
        ProductCache.getInstance().invalidateAll();
        productService.resetCacheStats();
        
        // Benchmark 1: getAllProducts without cache (cold)
        report.append("1. GET ALL PRODUCTS (Cold Cache vs. Warm Cache)\n");
        report.append("-".repeat(50)).append("\n");
        
        long coldStart = System.nanoTime();
        List<Product> coldProducts = productService.getAllProducts();
        long coldTime = System.nanoTime() - coldStart;
        report.append(String.format("   Cold cache: %d products in %.3f ms\n", 
                coldProducts.size(), coldTime / 1_000_000.0));
        
        // Warm cache call
        long warmStart = System.nanoTime();
        List<Product> warmProducts = productService.getAllProducts();
        long warmTime = System.nanoTime() - warmStart;
        report.append(String.format("   Warm cache: %d products in %.3f ms\n", 
                warmProducts.size(), warmTime / 1_000_000.0));
        
        double speedup1 = coldTime > 0 ? (double) coldTime / warmTime : 0;
        report.append(String.format("   Speedup: %.1fx faster with cache\n\n", speedup1));
        
        // Benchmark 2: Search performance
        report.append("2. SEARCH PERFORMANCE\n");
        report.append("-".repeat(50)).append("\n");
        
        String[] searchQueries = {"laptop", "phone", "electronics", "gaming"};
        
        for (String query : searchQueries) {
            // Clear search cache for this query
            ProductCache.getInstance().invalidateAll();
            productService.getAllProducts(); // Re-warm the base cache
            
            // Cold search
            long searchColdStart = System.nanoTime();
            List<Product> coldResults = productService.searchProducts(query);
            long searchColdTime = System.nanoTime() - searchColdStart;
            
            // Warm search (same query, should be cached)
            long searchWarmStart = System.nanoTime();
            List<Product> warmResults = productService.searchProducts(query);
            long searchWarmTime = System.nanoTime() - searchWarmStart;
            
            report.append(String.format("   Query '%s': %d results\n", query, coldResults.size()));
            report.append(String.format("      Cold: %.3f ms | Warm: %.3f ms | ", 
                    searchColdTime / 1_000_000.0, searchWarmTime / 1_000_000.0));
            
            double searchSpeedup = searchColdTime > 0 ? (double) searchColdTime / searchWarmTime : 0;
            report.append(String.format("Speedup: %.1fx\n", searchSpeedup));
        }
        report.append("\n");
        
        // Benchmark 3: Sorting performance
        report.append("3. SORTING PERFORMANCE\n");
        report.append("-".repeat(50)).append("\n");
        
        List<Product> productsToSort = new ArrayList<>(coldProducts);
        
        for (ProductSorter.SortCriteria criteria : ProductSorter.SortCriteria.values()) {
            List<Product> sortCopy = new ArrayList<>(productsToSort);
            
            long sortStart = System.nanoTime();
            ProductSorter.sort(sortCopy, criteria);
            long sortTime = System.nanoTime() - sortStart;
            
            report.append(String.format("   %s: %.3f ms\n", 
                    criteria.getDisplayName(), sortTime / 1_000_000.0));
        }
        report.append("\n");
        
        // Benchmark 4: Algorithm comparison (QuickSort vs MergeSort vs TimSort)
        report.append("4. SORTING ALGORITHM COMPARISON\n");
        report.append("-".repeat(50)).append("\n");
        
        ProductSorter.SortCriteria testCriteria = ProductSorter.SortCriteria.PRICE_DESC;
        
        // TimSort (Java Collections.sort)
        List<Product> timSortList = new ArrayList<>(productsToSort);
        long timSortStart = System.nanoTime();
        ProductSorter.sort(timSortList, testCriteria);
        long timSortTime = System.nanoTime() - timSortStart;
        
        // QuickSort
        List<Product> quickSortList = new ArrayList<>(productsToSort);
        long quickSortStart = System.nanoTime();
        ProductSorter.quickSort(quickSortList, testCriteria);
        long quickSortTime = System.nanoTime() - quickSortStart;
        
        // MergeSort
        List<Product> mergeSortList = new ArrayList<>(productsToSort);
        long mergeSortStart = System.nanoTime();
        ProductSorter.mergeSort(mergeSortList, testCriteria);
        long mergeSortTime = System.nanoTime() - mergeSortStart;
        
        report.append(String.format("   TimSort (default): %.3f ms\n", timSortTime / 1_000_000.0));
        report.append(String.format("   QuickSort: %.3f ms\n", quickSortTime / 1_000_000.0));
        report.append(String.format("   MergeSort: %.3f ms\n", mergeSortTime / 1_000_000.0));
        report.append("\n");
        
        // Benchmark 5: Hash-based vs Linear search
        report.append("5. SEARCH ALGORITHM COMPARISON\n");
        report.append("-".repeat(50)).append("\n");
        
        if (!productsToSort.isEmpty()) {
            String targetName = productsToSort.get(productsToSort.size() / 2).getProductName();
            
            // Linear search
            long linearStart = System.nanoTime();
            Product linearResult = ProductSearcher.linearSearch(productsToSort, targetName);
            long linearTime = System.nanoTime() - linearStart;
            
            // Build index and hash search
            ProductSearcher searcher = new ProductSearcher();
            long indexBuildStart = System.nanoTime();
            searcher.buildIndex(productsToSort);
            long indexBuildTime = System.nanoTime() - indexBuildStart;
            
            long hashSearchStart = System.nanoTime();
            List<Product> hashResults = searcher.search(targetName);
            long hashSearchTime = System.nanoTime() - hashSearchStart;
            
            report.append(String.format("   Target: '%s'\n", targetName));
            report.append(String.format("   Linear search: %.3f ms (found: %s)\n", 
                    linearTime / 1_000_000.0, linearResult != null));
            report.append(String.format("   Index build: %.3f ms\n", indexBuildTime / 1_000_000.0));
            report.append(String.format("   Hash search: %.3f ms (found: %d results)\n", 
                    hashSearchTime / 1_000_000.0, hashResults.size()));
            
            // For repeated searches, hash is better
            report.append(String.format("   Note: Hash search is O(1) after index build\n"));
        }
        report.append("\n");
        
        // Cache statistics
        report.append("6. CACHE STATISTICS\n");
        report.append("-".repeat(50)).append("\n");
        ProductCache.CacheStats stats = productService.getCacheStats();
        report.append(String.format("   Cache hits: %d\n", stats.hits));
        report.append(String.format("   Cache misses: %d\n", stats.misses));
        report.append(String.format("   Hit rate: %.1f%%\n", stats.getHitRate()));
        report.append(String.format("   Products cached: %d\n", stats.productsCached));
        report.append(String.format("   Search queries cached: %d\n", stats.searchQueriesCached));
        
        ProductSearcher.IndexStats indexStats = productService.getSearchIndexStats();
        report.append(String.format("   Search index: %s\n", indexStats));
        
        report.append("\n=== END OF BENCHMARK REPORT ===\n");
        
        return report.toString();
    }
    
    /**
     * Compare performance with caching enabled vs disabled.
     */
    public String compareCachingPerformance(int iterations) {
        StringBuilder report = new StringBuilder();
        report.append("=== CACHING IMPACT COMPARISON ===\n\n");
        
        // Test with caching disabled
        productService.setCachingEnabled(false);
        long noCacheTotal = 0;
        for (int i = 0; i < iterations; i++) {
            long start = System.nanoTime();
            productService.getAllProducts();
            noCacheTotal += System.nanoTime() - start;
        }
        double noCacheAvg = noCacheTotal / (double) iterations / 1_000_000.0;
        
        // Test with caching enabled
        productService.setCachingEnabled(true);
        ProductCache.getInstance().invalidateAll();
        
        // First call to warm up cache
        productService.getAllProducts();
        
        long cacheTotal = 0;
        for (int i = 0; i < iterations; i++) {
            long start = System.nanoTime();
            productService.getAllProducts();
            cacheTotal += System.nanoTime() - start;
        }
        double cacheAvg = cacheTotal / (double) iterations / 1_000_000.0;
        
        report.append(String.format("Iterations: %d\n", iterations));
        report.append(String.format("Without cache: %.3f ms average\n", noCacheAvg));
        report.append(String.format("With cache: %.3f ms average\n", cacheAvg));
        report.append(String.format("Improvement: %.1fx faster\n", noCacheAvg / cacheAvg));
        
        return report.toString();
    }
    
    /**
     * Main method for standalone benchmark execution.
     */
    public static void main(String[] args) {
        System.out.println("Starting performance benchmarks...\n");
        
        PerformanceBenchmark benchmark = new PerformanceBenchmark();
        
        // Run all benchmarks
        String report = benchmark.runAllBenchmarks();
        System.out.println(report);
        
        // Run caching comparison
        System.out.println(benchmark.compareCachingPerformance(10));
    }
}

