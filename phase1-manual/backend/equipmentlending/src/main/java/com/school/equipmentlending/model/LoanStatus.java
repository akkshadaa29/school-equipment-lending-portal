package com.school.equipmentlending.model;

public enum LoanStatus {
    BORROWED,    // item currently borrowed (active loan)
    RETURNED,    // item returned by the borrower
    OVERDUE,     // item not returned by due date
    CANCELLED    // loan cancelled before start
}
