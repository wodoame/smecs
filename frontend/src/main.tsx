import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import "./index.css";
import { createBrowserRouter, RouterProvider, Navigate } from "react-router-dom";
import App from "./App.tsx";
import { ProductList } from "./components/product/ProductList";
import AdminProductsPage from "./pages/admin/AdminProductsPage";
import CategoriesPage from "./pages/CategoriesPage";

const router = createBrowserRouter([
  {
    path: "/",
    element: <App />,
    children: [
      {
        index: true,
        element: <Navigate to="/products" replace />,
      },
      {
        path: "products",
        element: <ProductList />,
      },
      {
        path: "admin/inventories",
        element: <AdminProductsPage />,
      },
      {
        path: "categories",
        element: <CategoriesPage />,
      },
    ],
  },
]);

createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <RouterProvider router={router} />
  </StrictMode>,
);
