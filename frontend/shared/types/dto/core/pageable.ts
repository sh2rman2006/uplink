export interface PageableRequest {
  page?: number;
  size?: number;
  sort?: string;
  direction?: "ASC" | "DESC";
}

export interface PageableResponse<T> {
  content: T[];
  page: {
    size: number;
    number: number;
    totalElements: number;
    totalPages: number;
  };
}

export interface SimplePageableResponse<T> {
  items: T[];
  total: number;
  page: number;
  size: number;
  totalPages: number;
  hasNext: boolean;
  hasPrevious: boolean;
}

// shared/types/dto/core/pageable.ts

export interface PageableRequest {
  page?: number;
  size?: number;
  sort?: string;
  direction?: "ASC" | "DESC";
}

export interface PageableResponse<T> {
  content: T[];
  page: {
    size: number;
    number: number;
    totalElements: number;
    totalPages: number;
  };
}

export interface SpringPage<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  numberOfElements?: number;
  first?: boolean;
  last?: boolean;
  empty?: boolean;
  pageable?: any;
  sort?: any;
}

export function toPageableResponse<T>(p: SpringPage<T>): PageableResponse<T> {
  return {
    content: p.content ?? [],
    page: {
      size: p.size ?? 0,
      number: p.number ?? 0,
      totalElements: p.totalElements ?? 0,
      totalPages: p.totalPages ?? 0,
    },
  };
}

export function createPageableParams(params: PageableRequest): Record<string, any> {
  const queryParams: Record<string, any> = {};

  if (params.page !== undefined) queryParams.page = params.page;
  if (params.size !== undefined) queryParams.size = params.size;

  if (params.sort) {
    queryParams.sort = params.sort;
    if (params.direction) queryParams.sort += `,${params.direction.toLowerCase()}`;
  }

  return queryParams;
}
