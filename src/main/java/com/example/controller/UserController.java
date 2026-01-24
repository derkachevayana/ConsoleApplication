package com.example.controller;

import com.example.dto.UserRequest;
import com.example.dto.UserResponse;
import com.example.dto.UserUpdateRequest;
import com.example.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "Операции управления пользователями")
public class UserController {

    private final UserService userService;

    @Operation(
            summary = "Создать нового пользователя",
            description = "Создает нового пользователя и отправляет событие в Kafka"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Пользователь успешно создан",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Неверные входные данные"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Пользователь с таким email уже существует"
            )
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createUser(
            @Parameter(description = "Данные нового пользователя", required = true)
            @Valid @RequestBody UserRequest request) {

        UserResponse response = userService.createUser(request);

        response.add(linkTo(methodOn(UserController.class).getUserById(response.getId())).withSelfRel());
        response.add(linkTo(methodOn(UserController.class).getAllUsers()).withRel(IanaLinkRelations.COLLECTION));
        response.add(Link.of(linkTo(UserController.class).slash(response.getId()).toUri().toString(), "update"));
        response.add(Link.of(linkTo(UserController.class).slash(response.getId()).toUri().toString(), "delete"));
        response.add(linkTo(methodOn(UserController.class).getUserByEmail(response.getEmail())).withRel("by-email"));

        return response;
    }

    @Operation(summary = "Получить пользователя по ID")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Пользователь найден",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Пользователь не найден"
            )
    })
    @GetMapping("/{id}")
    public UserResponse getUserById(
            @Parameter(description = "ID пользователя", example = "1", required = true)
            @PathVariable Long id) {

        UserResponse response =userService.getUserById(id);

        response.add(linkTo(methodOn(UserController.class).getUserById(id)).withSelfRel());
        response.add(linkTo(methodOn(UserController.class).getAllUsers()).withRel(IanaLinkRelations.COLLECTION));
        response.add(Link.of(linkTo(UserController.class).slash(id).toUri().toString(), "update"));
        response.add(Link.of(linkTo(UserController.class).slash(id).toUri().toString(), "delete"));
        response.add(linkTo(methodOn(UserController.class).getUserByEmail(response.getEmail())).withRel("by-email"));

        return response;
    }

    @Operation(summary = "Получить всех пользователей")
    @ApiResponse(
            responseCode = "200",
            description = "Список всех пользователей"
    )
    @GetMapping
    public CollectionModel<UserResponse> getAllUsers() {

        List<UserResponse> users = userService.getAllUsers();

        List<UserResponse> usersWithLinks = users.stream()
                .map(user -> {
                    user.add(linkTo(methodOn(UserController.class).getUserById(user.getId())).withSelfRel());
                    user.add(Link.of(linkTo(UserController.class).slash(user.getId()).toUri().toString(), "update"));
                    user.add(Link.of(linkTo(UserController.class).slash(user.getId()).toUri().toString(), "delete"));
                    user.add(linkTo(methodOn(UserController.class).getUserByEmail(user.getEmail())).withRel("by-email"));

                    return user;
                })
                .collect(Collectors.toList());

        return CollectionModel.of(usersWithLinks,
                linkTo(methodOn(UserController.class).getAllUsers()).withSelfRel(),
                linkTo(methodOn(UserController.class).createUser(null)).withRel("create"));
    }

    @Operation(summary = "Получить пользователя по email")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Пользователь найден",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Пользователь не найден"
            )
    })
    @GetMapping("/email/{email}")
    public UserResponse getUserByEmail(
            @Parameter(description = "Email пользователя", example = "user@example.com", required = true)
            @PathVariable String email) {

        UserResponse response = userService.getUserByEmail(email);

        response.add(linkTo(methodOn(UserController.class).getUserByEmail(email)).withSelfRel());
        response.add(linkTo(methodOn(UserController.class).getUserById(response.getId())).withRel("by-id"));
        response.add(linkTo(methodOn(UserController.class).getAllUsers()).withRel(IanaLinkRelations.COLLECTION));
        response.add(Link.of(linkTo(UserController.class).slash(response.getId()).toUri().toString(), "update"));
        response.add(Link.of(linkTo(UserController.class).slash(response.getId()).toUri().toString(), "delete"));

        return response;
    }

    @Operation(summary = "Обновить пользователя")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Пользователь успешно обновлен",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Пользователь не найден"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Неверные входные данные"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Email уже используется"
            )
    })
    @PutMapping("/{id}")
    public UserResponse updateUser(
            @Parameter(description = "ID пользователя", example = "1", required = true)
            @PathVariable Long id,

            @Parameter(description = "Обновленные данные пользователя", required = true)
            @Valid @RequestBody UserUpdateRequest request) {

        UserResponse response = userService.updateUser(id, request);

        response.add(linkTo(methodOn(UserController.class).getUserById(id)).withSelfRel());
        response.add(linkTo(methodOn(UserController.class).getAllUsers()).withRel(IanaLinkRelations.COLLECTION));
        response.add(Link.of(linkTo(UserController.class).slash(id).toUri().toString(), "delete"));
        response.add(linkTo(methodOn(UserController.class).getUserByEmail(response.getEmail())).withRel("by-email"));

        return response;
    }

    @Operation(summary = "Удалить пользователя")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Пользователь успешно удален"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Пользователь не найден"
            )
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(
            @Parameter(description = "ID пользователя", example = "1", required = true)
            @PathVariable Long id) {

        userService.deleteUser(id);
    }
}
