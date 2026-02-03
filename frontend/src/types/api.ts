export interface PageInfo {
    empty: boolean;
    first: boolean;
    hasNext: boolean;
    hasPrevious: boolean;
    last: boolean;
    page: number;
    size: number;
    totalElements: number;
    totalPages: number;
}

export interface ApiResponse<T> {
    status: string;
    message: string;
    data: {
        content: T[];
        page: PageInfo;
    };
}
