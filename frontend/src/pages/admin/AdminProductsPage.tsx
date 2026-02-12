import * as React from "react"
import { toast } from "sonner"
import { Button } from "@/components/ui/button"
import { Checkbox } from "@/components/ui/checkbox"
import {
    DropdownMenu,
    DropdownMenuCheckboxItem,
    DropdownMenuContent,
    DropdownMenuGroup,
    DropdownMenuItem,
    DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import { Input } from "@/components/ui/input"
import {
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableHeader,
    TableRow,
} from "@/components/ui/table"
import {
    flexRender,
    getCoreRowModel,
    getFilteredRowModel,
    getPaginationRowModel,
    getSortedRowModel,
    useReactTable,
    type ColumnDef,
    type ColumnFiltersState,
    type SortingState,
    type VisibilityState,
} from "@tanstack/react-table"
import { ArrowUpDown, ChevronDown, CircleCheck, MoreHorizontal, Plus, Search, ShieldAlert, LogIn, ListRestart } from "lucide-react"
import {
    DialogTrigger,
    Dialog,
    DialogContent,
    DialogDescription,
    DialogFooter,
    DialogHeader,
    DialogTitle,
} from "@/components/ui/dialog"
import {
    Pagination,
    PaginationContent,
    PaginationItem,
    PaginationNext,
    PaginationPrevious,
} from "@/components/ui/pagination"
import {
    Combobox,
    ComboboxContent,
    ComboboxEmpty,
    ComboboxInput,
    ComboboxItem,
    ComboboxList,
} from "@/components/ui/combobox"
import { auth } from "@/lib/auth"
import { Link, useLocation } from "react-router-dom"

export interface InventoryItem {
    id: number;
    productId: number;
    name: string;
    description: string;
    price: number;
    category: {
        categoryId: number;
        categoryName: string;
    };
    image: string;
    quantity: number;
}



export default function AdminProductsPage() {
    const location = useLocation();
    const [data, setData] = React.useState<InventoryItem[]>([])
    const [loading, setLoading] = React.useState(true)
    const [sorting, setSorting] = React.useState<SortingState>([])
    const [columnFilters, setColumnFilters] = React.useState<ColumnFiltersState>(
        []
    )
    const [columnVisibility, setColumnVisibility] =
        React.useState<VisibilityState>({})
    const [rowSelection, setRowSelection] = React.useState({})

    // Add Inventory State
    const [isAddDialogOpen, setIsAddDialogOpen] = React.useState(false);
    // Removed unused categories state in favor of categoryList
    const [newInventory, setNewInventory] = React.useState({
        name: "",
        description: "",
        price: "",
        categoryId: "",
        imageUrl: "",
        quantity: "",
    });

    // Edit Inventory State
    const [isEditDialogOpen, setIsEditDialogOpen] = React.useState(false);
    const [editingItem, setEditingItem] = React.useState<InventoryItem | null>(null);

    // View Inventory State
    const [isViewDialogOpen, setIsViewDialogOpen] = React.useState(false);
    const [viewingItem, setViewingItem] = React.useState<InventoryItem | null>(null);

    // Search and Pagination State
    const [searchTerm, setSearchTerm] = React.useState("")
    const [globalFilter, setGlobalFilter] = React.useState("")
    const [page, setPage] = React.useState(1)
    const [totalPages, setTotalPages] = React.useState(0)

    // Error State
    const [authError, setAuthError] = React.useState<'unauthorized' | 'forbidden' | null>(null)

    const fetchInventory = (query = "", pageIndex = 1) => {
        setLoading(true);
        const graphqlQuery = {
            query: `
                query GetInventories($page: Int, $size: Int, $query: String) {
                    inventories(page: $page, size: $size, query: $query) {
                        content {
                            id
                            quantity
                            product {
                                id
                                name
                                description
                                price
                                imageUrl
                                category {
                                    id
                                    name
                                }
                            }
                        }
                        page {
                            totalPages
                        }
                    }
                }
            `,
            variables: {
                page: pageIndex,
                size: 10,
                query: query
            }
        };

        const user = auth.getUser();
        const headers: Record<string, string> = {
            "Content-Type": "application/json",
        };

        if (user?.token) {
            headers["Authorization"] = `Bearer ${user.token}`;
        }

        fetch("/graphql", {
            method: "POST",
            headers,
            body: JSON.stringify(graphqlQuery),
        })
            .then((res) => {
                // GraphQL always returns 200, so we can't check status codes here
                // We need to check the error types in the response payload
                if (!res.ok) throw new Error("Failed to fetch inventory");
                return res.json();
            })
            .then((payload) => {
                // Check for GraphQL errors with specific error types
                if (payload.errors && payload.errors.length > 0) {
                    const error = payload.errors[0];
                    const errorType = error.extensions?.errorType || error.extensions?.classification;

                    if (errorType === 'UNAUTHORIZED' || error.message.includes('Authorization')) {
                        setAuthError('unauthorized');
                        throw new Error(error.message || "Unauthorized - Please log in");
                    }
                    if (errorType === 'FORBIDDEN' || error.message.includes('Insufficient permissions')) {
                        setAuthError('forbidden');
                        throw new Error(error.message || "Forbidden - You don't have permission to access this resource");
                    }
                    throw new Error(error.message || "Failed to fetch inventory");
                }
                const data = payload.data.inventories;
                const inventory = (data.content || []).map((item: any) => ({
                    id: parseInt(item.id),
                    productId: parseInt(item.product.id),
                    name: item.product.name ?? "Unnamed Product",
                    description: item.product.description,
                    price: item.product.price,
                    category: {
                        categoryId: parseInt(item.product.category?.id || "0"),
                        categoryName: item.product.category?.name || "Uncategorized"
                    },
                    image: item.product.imageUrl,
                    quantity: item.quantity,
                }));
                setData(inventory);
                setTotalPages(data.page?.totalPages || 0);
            })
            .catch((err) => {
                console.error("Error fetching inventory:", err);
                toast.error("Failed to load inventory");
            })
            .finally(() => setLoading(false));
    };

    const handleSearch = () => {
        setPage(1);
        fetchInventory(searchTerm, 1);
    };

    const handleShowAll = () => {
        setSearchTerm("");
        setGlobalFilter("");
        setPage(1);
        fetchInventory("", 1);
    };

    const handlePageChange = (newPage: number) => {
        if (newPage >= 1 && newPage <= totalPages) {
            setPage(newPage);
            fetchInventory(searchTerm, newPage);
        }
    };


    // Category Search State
    const [categorySearchTerm, setCategorySearchTerm] = React.useState("")
    const [categoryList, setCategoryList] = React.useState<{ categoryId: number; categoryName: string }[]>([])

    // Debounce search term
    React.useEffect(() => {
        const timer = setTimeout(() => {
            fetchCategoryList(categorySearchTerm)
        }, 300)

        return () => clearTimeout(timer)
    }, [categorySearchTerm])

    const fetchCategoryList = (query = "") => {
        fetch(`/api/categories?query=${encodeURIComponent(query)}`)
            .then((res) => res.json())
            .then((payload) => {
                setCategoryList(payload.data.content || []);
            })
            .catch((err) => console.error("Error fetching categories:", err));
    };

    React.useEffect(() => {
        fetchInventory();
        fetchCategoryList();
    }, []);

    const handleCreateInventory = () => {
        const mutation = {
            query: `
                mutation CreateInventory($input: CreateInventoryInput!) {
                    createInventory(input: $input) {
                        id
                        quantity
                        product {
                            id
                            name
                            description
                            price
                            imageUrl
                            category {
                                id
                                name
                            }
                        }
                    }
                }
            `,
            variables: {
                input: {
                    quantity: parseInt(newInventory.quantity),
                    product: {
                        name: newInventory.name,
                        description: newInventory.description,
                        price: parseFloat(newInventory.price),
                        categoryId: newInventory.categoryId,
                        imageUrl: newInventory.imageUrl,
                    }
                }
            }
        };

        console.log("GraphQL Mutation Request:", JSON.stringify(mutation, null, 2));

        const user = auth.getUser();
        const headers: Record<string, string> = {
            "Content-Type": "application/json",
        };

        if (user?.token) {
            headers["Authorization"] = `Bearer ${user.token}`;
        }

        fetch("/graphql", {
            method: "POST",
            headers,
            body: JSON.stringify(mutation),
        })
            .then((res) => {
                console.log("Response status:", res.status, res.statusText);
                // GraphQL always returns 200, so we can't check status codes here
                if (!res.ok) {
                    return res.text().then(text => {
                        console.error("Response error text:", text);
                        throw new Error("Failed to create inventory");
                    });
                }
                return res.json();
            })
            .then((payload) => {
                console.log("GraphQL Response:", JSON.stringify(payload, null, 2));
                // Check for GraphQL errors with specific error types
                if (payload.errors && payload.errors.length > 0) {
                    const error = payload.errors[0];
                    const errorType = error.extensions?.errorType || error.extensions?.classification;
                    console.error("GraphQL Errors:", payload.errors);

                    if (errorType === 'UNAUTHORIZED' || error.message.includes('Authorization')) {
                        setAuthError('unauthorized');
                        throw new Error(error.message || "Unauthorized - Please log in");
                    }
                    if (errorType === 'FORBIDDEN' || error.message.includes('Insufficient permissions')) {
                        setAuthError('forbidden');
                        throw new Error(error.message || "Forbidden - You don't have permission to perform this action");
                    }
                    throw new Error(error.message);
                }
                setIsAddDialogOpen(false);
                setNewInventory({
                    name: "",
                    description: "",
                    price: "",
                    categoryId: "",
                    imageUrl: "",
                    quantity: "",
                });
                fetchInventory(searchTerm, page);
                toast.success("Inventory created successfully", {
                    icon: <CircleCheck className="h-5 w-5 text-green-500" />,
                });
            })
            .catch((err) => {
                console.error("Error creating inventory:", err);
                toast.error(err.message);
            });
    };

    const handleEditItem = (item: InventoryItem) => {
        setEditingItem(item);
        setCategorySearchTerm(item.category?.categoryName || "");
        setIsEditDialogOpen(true);
    };

    const handleUpdateItem = () => {
        if (!editingItem) return;

        const payload = {
            quantity: editingItem.quantity,
            product: {
                name: editingItem.name,
                description: editingItem.description,
                price: editingItem.price,
                categoryId: editingItem.category.categoryId,
                imageUrl: editingItem.image,
            }
        };

        fetch(`/api/inventories/${editingItem.id}`, {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload),
        })
            .then((res) => {
                if (!res.ok) throw new Error("Failed to update inventory");
                return res.json();
            })
            .then(() => {
                setIsEditDialogOpen(false);
                setEditingItem(null);
                fetchInventory();
                toast.success("Inventory updated successfully", {
                    icon: <CircleCheck className="h-5 w-5 text-green-500" />,
                });
            })
            .catch((err) => toast.error(err.message));
    };

    const handleViewItem = (item: InventoryItem) => {
        setViewingItem(item);
        setIsViewDialogOpen(true);
    };

    const columns: ColumnDef<InventoryItem>[] = React.useMemo(
        () => [
            {
                id: "select",
                header: ({ table }) => (
                    <Checkbox
                        checked={
                            table.getIsAllPageRowsSelected() ||
                            (table.getIsSomePageRowsSelected() && "indeterminate")
                        }
                        onCheckedChange={(value) => table.toggleAllPageRowsSelected(!!value)}
                        aria-label="Select all"
                    />
                ),
                cell: ({ row }) => (
                    <Checkbox
                        checked={row.getIsSelected()}
                        onCheckedChange={(value) => row.toggleSelected(!!value)}
                        aria-label="Select row"
                    />
                ),
                enableSorting: false,
                enableHiding: false,
            },
            {
                accessorKey: "image",
                header: "Image",
                cell: ({ row }) => (
                    <img
                        src={row.getValue("image")}
                        alt={row.getValue("name")}
                        className="h-10 w-10 object-cover rounded-md"
                    />
                ),
            },
            {
                accessorKey: "name",
                header: ({ column }) => {
                    return (
                        <Button
                            variant="ghost"
                            onClick={() => column.toggleSorting(column.getIsSorted() === "asc")}
                            className="justify-start !px-0 text-left"
                        >
                            Name
                            <ArrowUpDown className="ml-2 h-4 w-4" />
                        </Button>
                    )
                },
                cell: ({ row }) => <div className="lowercase">{row.getValue("name")}</div>,
            },
            {
                accessorKey: "description",
                header: "Description",
                cell: ({ row }) => <div className="truncate max-w-[200px]">{row.getValue("description")}</div>,
            },
            {
                accessorKey: "category",
                header: "Category",
                cell: ({ row }) => {
                    const category = row.getValue("category") as InventoryItem["category"];
                    return <div className="text-left">{category?.categoryName || "N/A"}</div>;
                },
            },
            {
                accessorKey: "quantity",
                header: ({ column }) => {
                    return (
                        <Button
                            variant="ghost"
                            onClick={() => column.toggleSorting(column.getIsSorted() === "asc")}
                            className="justify-start !px-0 text-left"
                        >
                            Quantity
                            <ArrowUpDown className="ml-2 h-4 w-4" />
                        </Button>
                    )
                },
                cell: ({ row }) => <div className="text-left font-medium">{row.getValue("quantity")}</div>,
            },
            {
                accessorKey: "price",
                header: () => <div className="text-left">Price</div>,
                cell: ({ row }) => {
                    const amount = parseFloat(row.getValue("price"))

                    const formatted = new Intl.NumberFormat("en-US", {
                        style: "currency",
                        currency: "USD",
                    }).format(amount)

                    return <div className="text-left font-medium">{formatted}</div>
                },
            },
            {
                id: "actions",
                enableHiding: false,
                cell: ({ row }) => {
                    const item = row.original

                    return (
                        <DropdownMenu>
                            <DropdownMenuTrigger asChild>
                                <Button variant="ghost" className="h-8 w-8 p-0">
                                    <span className="sr-only">Open menu</span>
                                    <MoreHorizontal className="h-4 w-4" />
                                </Button>
                            </DropdownMenuTrigger>
                            <DropdownMenuContent align="end">
                                <DropdownMenuGroup>
                                    <DropdownMenuItem onClick={() => handleViewItem(item)}>
                                        View details
                                    </DropdownMenuItem>
                                    <DropdownMenuItem onClick={() => handleEditItem(item)}>
                                        Edit product
                                    </DropdownMenuItem>
                                </DropdownMenuGroup>
                            </DropdownMenuContent>
                        </DropdownMenu>
                    )
                },
            },
        ],
        []
    );

    const table = useReactTable({
        data,
        columns,
        onSortingChange: setSorting,
        onColumnFiltersChange: setColumnFilters,
        onGlobalFilterChange: setGlobalFilter,
        getCoreRowModel: getCoreRowModel(),
        getPaginationRowModel: getPaginationRowModel(),
        getSortedRowModel: getSortedRowModel(),
        getFilteredRowModel: getFilteredRowModel(),
        onColumnVisibilityChange: setColumnVisibility,
        onRowSelectionChange: setRowSelection,
        globalFilterFn: (row, _columnId, filterValue) => {
            const searchValue = filterValue.toLowerCase();
            const name = row.getValue<string>("name")?.toLowerCase() || "";
            const description = row.getValue<string>("description")?.toLowerCase() || "";
            const category = (row.getValue("category") as InventoryItem["category"])?.categoryName?.toLowerCase() || "";

            return name.includes(searchValue) ||
                description.includes(searchValue) ||
                category.includes(searchValue);
        },
        state: {
            sorting,
            columnFilters,
            columnVisibility,
            rowSelection,
            globalFilter,
        },
    })

    // Show error screens for auth issues
    if (authError === 'unauthorized') {
        const loginUrl = `/login?next=${encodeURIComponent(location.pathname)}`;
        return (
            <div className="flex flex-col items-center justify-center h-[70vh] gap-4">
                <LogIn className="h-16 w-16 text-muted-foreground" />
                <h2 className="text-2xl font-bold">Unauthorized</h2>
                <p className="text-muted-foreground">You need to be logged in to access this page.</p>
                <Button asChild>
                    <Link to={loginUrl}>Go to Login</Link>
                </Button>
            </div>
        );
    }

    if (authError === 'forbidden') {
        return (
            <div className="flex flex-col items-center justify-center h-[70vh] gap-4">
                <ShieldAlert className="h-16 w-16 text-destructive" />
                <h2 className="text-2xl font-bold">Access Forbidden</h2>
                <p className="text-muted-foreground">You don't have permission to access this page.</p>
                <p className="text-sm text-muted-foreground">Please contact an administrator if you believe this is an error.</p>
            </div>
        );
    }

    return (
        <div className="w-full p-6">
            <div className="flex items-center py-4">
                <div className="flex w-full max-w-sm items-center space-x-2">
                    <Input
                        placeholder="Filter products..."
                        value={searchTerm}
                        onChange={(event) => {
                            const value = event.target.value;
                            setSearchTerm(value);
                            setGlobalFilter(value);
                        }}
                        onKeyDown={(e) => {
                            if (e.key === "Enter") {
                                handleSearch();
                            }
                        }}
                    />
                    <Button type="submit" size="icon" onClick={handleSearch}>
                        <Search className="h-4 w-4" />
                    </Button>
                    <Button variant="outline" onClick={handleShowAll}>
                        <ListRestart className="h-4 w-4 mr-2" />
                        Show All
                    </Button>
                </div>

                <Dialog open={isAddDialogOpen} onOpenChange={(open) => {
                    setIsAddDialogOpen(open);
                    if (open) setCategorySearchTerm("");
                }}>
                    <DialogTrigger asChild>
                        <Button className="ml-2">
                            <Plus className="mr-2 h-4 w-4" /> Add Inventory
                        </Button>
                    </DialogTrigger>
                    <DialogContent className="sm:max-w-[425px] overflow-y-auto max-h-[90vh]">
                        <DialogHeader>
                            <DialogTitle>Add New Inventory</DialogTitle>
                            <DialogDescription>
                                Create a new product and add it to inventory.
                            </DialogDescription>
                        </DialogHeader>
                        <div className="grid gap-4 py-4">
                            <div className="flex flex-col gap-2">
                                <label htmlFor="name" className="text-sm font-medium">
                                    Name
                                </label>
                                <Input
                                    id="name"
                                    value={newInventory.name}
                                    onChange={(e) => setNewInventory({ ...newInventory, name: e.target.value })}
                                />
                            </div>
                            <div className="flex flex-col gap-2">
                                <label htmlFor="description" className="text-sm font-medium">
                                    Description
                                </label>
                                <Input
                                    id="description"
                                    value={newInventory.description}
                                    onChange={(e) => setNewInventory({ ...newInventory, description: e.target.value })}
                                />
                            </div>
                            <div className="flex flex-col gap-2">
                                <label htmlFor="price" className="text-sm font-medium">
                                    Price
                                </label>
                                <Input
                                    id="price"
                                    type="number"
                                    value={newInventory.price}
                                    onChange={(e) => setNewInventory({ ...newInventory, price: e.target.value })}
                                />
                            </div>
                            <div className="flex flex-col gap-2">
                                <label htmlFor="quantity" className="text-sm font-medium">
                                    Quantity
                                </label>
                                <Input
                                    id="quantity"
                                    type="number"
                                    value={newInventory.quantity}
                                    onChange={(e) => setNewInventory({ ...newInventory, quantity: e.target.value })}
                                />
                            </div>
                            <div className="flex flex-col gap-2">
                                <label htmlFor="category" className="text-sm font-medium">
                                    Category
                                </label>
                                <Combobox
                                    items={categoryList.map(c => c.categoryName)}
                                    onValueChange={(val) => {
                                        if (val) {
                                            const selectedCat = categoryList.find(c => c.categoryName === val);
                                            if (selectedCat) {
                                                setNewInventory({ ...newInventory, categoryId: String(selectedCat.categoryId) });
                                            }
                                        }
                                    }}
                                >
                                    <ComboboxInput
                                        placeholder="Search categories..."
                                        onChange={(e) => setCategorySearchTerm(e.target.value)}
                                    />
                                    <ComboboxContent>
                                        <ComboboxEmpty>No categories found.</ComboboxEmpty>
                                        <ComboboxList>
                                            {(item) => (
                                                <ComboboxItem key={item} value={item}>
                                                    {item}
                                                </ComboboxItem>
                                            )}
                                        </ComboboxList>
                                    </ComboboxContent>
                                </Combobox>
                            </div>
                            <div className="flex flex-col gap-2">
                                <label htmlFor="image" className="text-sm font-medium">
                                    Image URL
                                </label>
                                <Input
                                    id="image"
                                    value={newInventory.imageUrl}
                                    onChange={(e) => setNewInventory({ ...newInventory, imageUrl: e.target.value })}
                                />
                            </div>
                        </div>
                        <DialogFooter>
                            <Button type="submit" className="w-full" onClick={handleCreateInventory}>Add Inventory</Button>
                        </DialogFooter>
                    </DialogContent>
                </Dialog>

                <Dialog open={isEditDialogOpen} onOpenChange={setIsEditDialogOpen}>
                    <DialogContent className="sm:max-w-[425px] overflow-y-auto max-h-[90vh]">
                        <DialogHeader>
                            <DialogTitle>Edit Inventory</DialogTitle>
                            <DialogDescription>
                                Update product and inventory details.
                            </DialogDescription>
                        </DialogHeader>
                        <div className="grid gap-4 py-4">
                            <div className="flex flex-col gap-2">
                                <label htmlFor="editName" className="text-sm font-medium">
                                    Name
                                </label>
                                <Input
                                    id="editName"
                                    value={editingItem?.name || ""}
                                    onChange={(e) =>
                                        setEditingItem((prev) =>
                                            prev ? { ...prev, name: e.target.value } : null
                                        )
                                    }
                                />
                            </div>
                            <div className="flex flex-col gap-2">
                                <label htmlFor="editDescription" className="text-sm font-medium">
                                    Description
                                </label>
                                <Input
                                    id="editDescription"
                                    value={editingItem?.description || ""}
                                    onChange={(e) =>
                                        setEditingItem((prev) =>
                                            prev ? { ...prev, description: e.target.value } : null
                                        )
                                    }
                                />
                            </div>
                            <div className="flex flex-col gap-2">
                                <label htmlFor="editPrice" className="text-sm font-medium">
                                    Price
                                </label>
                                <Input
                                    id="editPrice"
                                    type="number"
                                    value={editingItem?.price || ""}
                                    onChange={(e) =>
                                        setEditingItem((prev) =>
                                            prev ? { ...prev, price: parseFloat(e.target.value) } : null
                                        )
                                    }
                                />
                            </div>
                            <div className="flex flex-col gap-2">
                                <label htmlFor="editQuantity" className="text-sm font-medium">
                                    Quantity
                                </label>
                                <Input
                                    id="editQuantity"
                                    type="number"
                                    value={editingItem?.quantity || ""}
                                    onChange={(e) =>
                                        setEditingItem((prev) =>
                                            prev ? { ...prev, quantity: parseInt(e.target.value) } : null
                                        )
                                    }
                                />
                            </div>
                            <div className="flex flex-col gap-2">
                                <label htmlFor="editCategory" className="text-sm font-medium">
                                    Category
                                </label>
                                <Combobox
                                    items={categoryList.map(c => c.categoryName)}
                                    defaultValue={editingItem?.category?.categoryName}
                                    onValueChange={(val) => {
                                        const selectedCategory = categoryList.find(c => c.categoryName === val);
                                        if (selectedCategory) {
                                            setEditingItem((prev) =>
                                                prev ? { ...prev, category: { ...prev.category, categoryId: selectedCategory.categoryId, categoryName: selectedCategory.categoryName } } : null
                                            )
                                        }
                                    }}
                                >
                                    <ComboboxInput
                                        placeholder="Search categories..."
                                        onChange={(e) => setCategorySearchTerm(e.target.value)}
                                    />
                                    <ComboboxContent>
                                        <ComboboxEmpty>No categories found.</ComboboxEmpty>
                                        <ComboboxList>
                                            {(item) => (
                                                <ComboboxItem key={item} value={item}>
                                                    {item}
                                                </ComboboxItem>
                                            )}
                                        </ComboboxList>
                                    </ComboboxContent>
                                </Combobox>
                            </div>
                            <div className="flex flex-col gap-2">
                                <label htmlFor="editImage" className="text-sm font-medium">
                                    Image URL
                                </label>
                                <Input
                                    id="editImage"
                                    value={editingItem?.image || ""}
                                    onChange={(e) =>
                                        setEditingItem((prev) =>
                                            prev ? { ...prev, image: e.target.value } : null
                                        )
                                    }
                                />
                            </div>
                        </div>
                        <DialogFooter>
                            <Button type="submit" className="w-full" onClick={handleUpdateItem}>
                                Save Changes
                            </Button>
                        </DialogFooter>
                    </DialogContent>
                </Dialog>

                <Dialog open={isViewDialogOpen} onOpenChange={setIsViewDialogOpen}>
                    <DialogContent className="sm:max-w-[425px]">
                        <DialogHeader>
                            <DialogTitle>Product Details</DialogTitle>
                        </DialogHeader>
                        <div className="grid gap-4 py-4">
                            <div className="flex flex-col gap-2">
                                <label className="text-sm font-medium">Name</label>
                                <div className="text-sm text-muted-foreground">{viewingItem?.name}</div>
                            </div>
                            <div className="flex flex-col gap-2">
                                <label className="text-sm font-medium">Description</label>
                                <div className="text-sm text-muted-foreground">{viewingItem?.description}</div>
                            </div>
                            <div className="flex flex-col gap-2">
                                <label className="text-sm font-medium">Price</label>
                                <div className="text-sm text-muted-foreground">
                                    {new Intl.NumberFormat("en-US", { style: "currency", currency: "USD" }).format(viewingItem?.price || 0)}
                                </div>
                            </div>
                            <div className="flex flex-col gap-2">
                                <label className="text-sm font-medium">Quantity</label>
                                <div className="text-sm text-muted-foreground">{viewingItem?.quantity}</div>
                            </div>
                            <div className="flex flex-col gap-2">
                                <label className="text-sm font-medium">Category</label>
                                <div className="text-sm text-muted-foreground">{viewingItem?.category?.categoryName}</div>
                            </div>
                            <div className="flex flex-col gap-2">
                                <label className="text-sm font-medium">Image</label>
                                {viewingItem?.image && (
                                    <img
                                        src={viewingItem.image}
                                        alt={viewingItem.name}
                                        className="h-40 w-full object-cover rounded-md"
                                    />
                                )}
                            </div>
                        </div>
                    </DialogContent>
                </Dialog>

                <DropdownMenu>
                    <DropdownMenuTrigger asChild>
                        <Button variant="outline" className="ml-auto">
                            Columns <ChevronDown className="ml-2 h-4 w-4" />
                        </Button>
                    </DropdownMenuTrigger>
                    <DropdownMenuContent align="end">
                        <DropdownMenuGroup>
                            {table
                                .getAllColumns()
                                .filter((column) => column.getCanHide())
                                .map((column) => {
                                    return (
                                        <DropdownMenuCheckboxItem
                                            key={column.id}
                                            className="capitalize"
                                            checked={column.getIsVisible()}
                                            onCheckedChange={(value) =>
                                                column.toggleVisibility(!!value)
                                            }
                                        >
                                            {column.id}
                                        </DropdownMenuCheckboxItem>
                                    )
                                })}
                        </DropdownMenuGroup>
                    </DropdownMenuContent>
                </DropdownMenu>
            </div>
            <div className="rounded-md border">
                <Table>
                    <TableHeader>
                        {table.getHeaderGroups().map((headerGroup) => (
                            <TableRow key={headerGroup.id}>
                                {headerGroup.headers.map((header) => {
                                    return (
                                        <TableHead key={header.id}>
                                            {header.isPlaceholder
                                                ? null
                                                : flexRender(
                                                    header.column.columnDef.header,
                                                    header.getContext()
                                                )}
                                        </TableHead>
                                    )
                                })}
                            </TableRow>
                        ))}
                    </TableHeader>
                    <TableBody>
                        {loading ? (
                            <TableRow>
                                <TableCell colSpan={columns.length} className="h-24 text-center">
                                    Loading...
                                </TableCell>
                            </TableRow>
                        ) : table.getRowModel().rows?.length ? (
                            table.getRowModel().rows.map((row) => (
                                <TableRow
                                    key={row.id}
                                    data-state={row.getIsSelected() && "selected"}
                                >
                                    {row.getVisibleCells().map((cell) => (
                                        <TableCell key={cell.id}>
                                            {flexRender(
                                                cell.column.columnDef.cell,
                                                cell.getContext()
                                            )}
                                        </TableCell>
                                    ))}
                                </TableRow>
                            ))
                        ) : (
                            <TableRow>
                                <TableCell
                                    colSpan={columns.length}
                                    className="h-24 text-center"
                                >
                                    No results.
                                </TableCell>
                            </TableRow>
                        )}
                    </TableBody>
                </Table>
            </div>
            <div className="flex items-center justify-end space-x-2 py-4">
                <div className="text-muted-foreground flex-1 text-sm">
                    {table.getFilteredSelectedRowModel().rows.length} of{" "}
                    {table.getFilteredRowModel().rows.length} row(s) selected.
                </div>
                <Pagination className="w-auto h-auto shrink-0 space-x-2 justify-end">
                    <PaginationContent>
                        <PaginationItem>
                            <PaginationPrevious
                                onClick={() => handlePageChange(page - 1)}
                                className={page <= 1 ? "pointer-events-none opacity-50" : "cursor-pointer"}
                            />
                        </PaginationItem>
                        {/* Optionally add page numbers here if needed, keeping it simple for now as requested */}
                        <PaginationItem>
                            <PaginationNext
                                onClick={() => handlePageChange(page + 1)}
                                className={page >= totalPages ? "pointer-events-none opacity-50" : "cursor-pointer"}
                            />
                        </PaginationItem>
                    </PaginationContent>
                </Pagination>
            </div>
        </div>
    )
}
