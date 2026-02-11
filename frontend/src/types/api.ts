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
export interface SingleApiResponse<T> {
    status: string;
    message: string;
    data: T;
}

export interface AuthData {
    email: string;
    id: number;
    role: string;
    username: string;
    authExpiry?: number;
    token: string;
    cartId: number;
}

export interface RegisterPayload {
    username: string;
    email: string;
    password: string;
}
