package com.pdfeditor.pdfeditor.controller;

import com.pdfeditor.pdfeditor.model.UserModel;
import com.pdfeditor.pdfeditor.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutionException;

@RestController
public class UserController {
    public UserService userService;
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/create")
    public String createUser(@RequestBody UserModel userModel) throws InterruptedException, ExecutionException {
        return userService.createUser(userModel);
    }

    @GetMapping("/get")
    public UserModel getUser(@RequestParam String id) throws InterruptedException, ExecutionException {
        return userService.getUser(id);
    }

    @PutMapping("/update")
    public String getUser(@RequestBody UserModel userModel) throws InterruptedException, ExecutionException {
        return userService.updateUser(userModel);
    }

    @DeleteMapping("/delete")
    public String deleteUser(@RequestParam String id) throws InterruptedException, ExecutionException {
        return userService.deleteUser(id);
    }

    @GetMapping("/test")
    public ResponseEntity<String> testGetEndpoint() { return ResponseEntity.ok("Test Get Endpoint is Working"); }


}
