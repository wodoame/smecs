import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import "./index.css";
import { createBrowserRouter, RouterProvider, Navigate } from "react-router-dom";
import App from "./App.tsx";
import { ProductList } from "./components/product/ProductList";
import AdminProductsPage from "./pages/admin/AdminProductsPage";
import AdminCategoriesPage from "./pages/admin/AdminCategoriesPage";
import CategoriesPage from "./pages/CategoriesPage";
import ComboboxTestPage from "./pages/ComboboxTestPage";
import CartPage from "./pages/CartPage";
import SignupPage from "./pages/SignupPage";
import LoginPage from "./pages/LoginPage";


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
        path: "admin/categories",
        element: <AdminCategoriesPage />,
      },
      {
        path: "categories",
        element: <CategoriesPage />,
      },
      {
        path: "test-combobox",
        element: <ComboboxTestPage />,
      },
      {
        path: "cart",
        element: <CartPage />,
      },

    ],
  },
  {
    path: "/signup",
    element: <SignupPage />,
  },
  {
    path: "/login",
    element: <LoginPage />,
  },
]);

createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <RouterProvider router={router} />
  </StrictMode>,
);
