package com.Employee.EmployeeTask.Config;

import lombok.*;

/*
* Created by Kappala Varalakshmi
* On 26-02-2024
*/

@Getter
@Setter
public class Response<T> {
    private int status;
    private T data;
    private String message;

    // Constructor for success case
    public Response(T data, String message) {
        this.status = 1;
        this.data = data;
        this.message = message;
    }

    // Constructor for error case
    public Response(String message) {
        this.status = 0;
        this.message = message;
    }

    // Constructor for error case with data
    public Response(int status, String message) {
        this.status = status;
        this.data = null;
        this.message = message;
    }
}
