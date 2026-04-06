package com.finance.dashboard.model;

public enum Role {
    VIEWER,   // GET /dashboard/recent only
    ANALYST,  // VIEWER + full transaction CRUD (own records) + all dashboard analytics
    ADMIN     // Full access: all users, all transactions, all analytics
}
