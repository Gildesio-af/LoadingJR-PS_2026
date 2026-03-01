package com.loandingjr.chat.controller;

import com.loandingjr.chat.dto.chat.ChatRequestDTO;
import com.loandingjr.chat.dto.chat.ChatResponseDTO;
import com.loandingjr.chat.security.CustomUserDetails;
import com.loandingjr.chat.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/chats")
@RequiredArgsConstructor
@Tag(name = "Chat Controller", description = "Controller responsible for chat management operations, including creating, retrieving, accepting, and closing chats.")
public class ChatController {
    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @GetMapping
    @Operation(summary = "Get Pending Chat for User", description = "Retrieves the pending chat for the authenticated user, if one exists.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved pending chat",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ChatResponseDTO.class),
            examples = @ExampleObject(
                    name = "Get Pending Chat Example",
                    summary = "Example of a successful response when retrieving a pending chat for the authenticated user",
                    value = """
                            {
                                "content": [
                                    {
                                        "id": "87195b5b-65ed-4aca-8db3-f7503c888868",
                                        "status": "PENDING",
                                        "initiatorUsername": "admin",
                                        "participantUsername": "test2",
                                        "messages": {
                                            "content": [],
                                            "page": {
                                                "size": 0,
                                                "number": 0,
                                                "totalElements": 0,
                                                "totalPages": 1
                                            }
                                        },
                                        "aiReport": null,
                                        "createdAt": "2026-02-23T16:35:29.469366",
                                        "closedAt": null
                                    }
                                ],
                                "page": {
                                    "size": 20,
                                    "number": 0,
                                    "totalElements": 1,
                                    "totalPages": 1
                                }
                            }
                            """
            ))),
        @ApiResponse(responseCode = "401", description = "User not authenticated", content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                        name = "Get Pending Chat Authentication Error",
                        summary = "Example of an authentication error response when trying to retrieve a pending chat without being authenticated",
                        value = """
                            {
                                "timestamp": "2026-02-24T23:32:14.043425400Z",
                                "status": 401,
                                "error": "Access Denied",
                                "message": "Full authentication is required to access this resource",
                                "path": "/chats/pending"
                            }
                            """
                )
        ))
    })
    public ResponseEntity<Page<ChatResponseDTO>> getPendingChat(@Parameter(hidden = true)
                                                                @AuthenticationPrincipal CustomUserDetails userDetails,
                                                                @Parameter(description = "Pagination and sorting information for the pending chat response")
                                                                @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        System.out.println("User ID from Principal: " + userDetails.getId());
        return ResponseEntity.ok(chatService.getPendingChatForUser(userDetails.getId(), pageable));
    }

    @Operation(summary = "Get Chat by ID", description = "Retrieves a specific chat by its ID, including paginated messages.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved chat details",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ChatResponseDTO.class),
            examples = @ExampleObject(
                    name = "Get Chat by ID Example",
                    summary = "Example of a successful response when retrieving a chat by ID",
                    value = """
                            {
                                "id": "3644c5b8-be7e-4f07-a57b-c6ea59f60720",
                                "status": "CLOSED",
                                "initiatorUsername": "admin",
                                "participantUsername": "test2",
                                "messages": {
                                    "content": [
                                        {
                                            "id": "d987cf5f-d607-49a5-b5ee-918e0a85c754",
                                            "senderId": "550e8400-e29b-41d4-a716-446655440000",
                                            "senderUsername": "test2",
                                            "content": "Ola, tudo bem?",
                                            "sentAt": "2026-02-23T13:07:21.831069"
                                        },
                                        {
                                            "id": "ba4b33d9-8f7a-44a9-9bf0-77acbf3586a2",
                                            "senderId": "6f1a2b30-9c7d-4e44-b8f2-123456789abc",
                                            "senderUsername": "admin",
                                            "content": "Tudo e com você?",
                                            "sentAt": "2026-02-23T13:07:42.835855"
                                        },
                                        {
                                            "id": "31d915ab-1125-45c3-8810-c8037604d5f7",
                                            "senderId": "6f1a2b30-9c7d-4e44-b8f2-123456789abc",
                                            "senderUsername": "admin",
                                            "content": "Que bom, estou bem também.",
                                            "sentAt": "2026-02-23T13:08:10.918414"
                                        },
                                        {
                                            "id": "a4687f57-0d6a-41eb-a606-086e101c641f",
                                            "senderId": "550e8400-e29b-41d4-a716-446655440000",
                                            "senderUsername": "test2",
                                            "content": "Que bom, tchau",
                                            "sentAt": "2026-02-23T13:08:34.392648"
                                        },
                                        {
                                            "id": "7bedaf70-1939-477b-a5f2-6ab151bd46aa",
                                            "senderId": "6f1a2b30-9c7d-4e44-b8f2-123456789abc",
                                            "senderUsername": "admin",
                                            "content": "Flw",
                                            "sentAt": "2026-02-23T13:08:44.86011"
                                        }
                                    ],
                                    "page": {
                                        "size": 20,
                                        "number": 0,
                                        "totalElements": 9,
                                        "totalPages": 1
                                    }
                                },
                                "aiReport": "Here's a summary of the chat conversation, along with insights: {...}",
                                "createdAt": "2026-02-23T09:12:55.128493",
                                "closedAt": "2026-02-23T13:24:53.621173"
                            }
                            """
            ))),
        @ApiResponse(responseCode = "401", description = "User not authenticated", content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                        name = "Chat Acceptance Authentication Error",
                        summary = "Example of an authentication error response when trying to accept a chat without being authenticated",
                        value = """
                            {
                                "timestamp": "2026-02-24T23:32:14.043425400Z",
                                "status": 401,
                                "error": "Access Denied",
                                "message": "Full authentication is required to access this resource",
                                "path": "/chats/{chatId}/accept"
                            }
                            """
                )
        )),
        @ApiResponse(responseCode = "404", description = "Participant user not found", content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                        name = "Chat Creation User Not Found Error",
                        summary = "Example of a not found error response when the participant user does not exist",
                        value = """
                            {
                                "error": "User with identifier 550e8400-e29b-41d4-a716-4466554400 not found",
                                "description": "uri=/chats",
                                "status": "404 NOT_FOUND",
                                "timestamp": "2026-02-24 20:21:49"
                            }
                            """
                )
        ))
    })
    @GetMapping("/{chatId}")
    public ResponseEntity<ChatResponseDTO> getChatById(
            @Parameter(description = "ID of the chat to be retrieved") @PathVariable String chatId,
            @Parameter(description = "Pagination and sorting information for messages within the chat")
            @PageableDefault(size = 20, sort = "sentAt", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(chatService.getChatById(chatId, pageable));
    }

    @Operation(summary = "Create a new Chat", description = "Initiates a new chat session.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Chat created successfully",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ChatResponseDTO.class),
            examples = @ExampleObject(
                    name = "Chat Creation Example",
                    summary = "Example of a successful chat creation response",
                    value = """
                            {
                                "id": "18f75092-0829-4c0d-931f-c202fdddc6d6",
                                "status": "PENDING",
                                "initiatorUsername": "test2",
                                "participantUsername": "test2",
                                "messages": null,
                                "aiReport": null,
                                "createdAt": "2026-02-24T20:16:00.3344992",
                                "closedAt": null
                            }
                            """
            ))),
        @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                        name = "Chat Creation Validation Error",
                        summary = "Example of a validation error response when creating a chat",
                        value = """
                                {
                                    "error": "User with identifier 550e8400-e29b-41d4-a716-44665544000 not found",
                                    "description": "uri=/chats",
                                    "status": "404 NOT_FOUND",
                                    "timestamp": "2026-02-24 20:21:49"
                                }
                                """
                )
        )),
        @ApiResponse(responseCode = "401", description = "User not authenticated", content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                        name = "Chat Creation Authentication Error",
                        summary = "Example of an authentication error response when trying to create a chat without being authenticated",
                        value = """
                                {
                                    "timestamp": "2026-02-24T23:32:14.043425400Z",
                                    "status": 401,
                                    "error": "Access Denied",
                                    "message": "Full authentication is required to access this resource",
                                    "path": "/chats"
                                }
                                """
                )
        )),
        @ApiResponse(responseCode = "404", description = "Participant user not found", content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                        name = "Chat Creation User Not Found Error",
                        summary = "Example of a not found error response when the participant user does not exist",
                        value = """
                                {
                                    "error": "User with identifier 550e8400-e29b-41d4-a716-4466554400 not found",
                                    "description": "uri=/chats",
                                    "status": "404 NOT_FOUND",
                                    "timestamp": "2026-02-24 20:21:49"
                                }
                                """
                )
        )),
        @ApiResponse(responseCode = "409", description = "Chat cannot be created (e.g., user already in an active chat)", content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                        name = "Chat Creation Conflict Error",
                        summary = "Example of a conflict error response when trying to create a chat while the user is already in an active chat",
                        value = """
                                {
                                    "error": "User with ID 550e8400-e29b-41d4-a716-446655440000 is already in an active chat",
                                    "description": "uri=/chats",
                                    "status": "409 CONFLICT",
                                    "timestamp": "2026-02-24 20:25:44"
                                }
                                """)
        ))
    })
    @PostMapping
    public ResponseEntity<ChatResponseDTO> createChat(@Valid @RequestBody ChatRequestDTO request,
                                                      @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) {
        ChatResponseDTO chat = chatService.requestChat(userDetails.getId(), request);
        messagingTemplate.convertAndSend("/topic/chat/updates", chat);
        return ResponseEntity.ok(chat);
    }

    @Operation(summary = "Accept a Chat", description = "Accepts a pending chat request.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Chat accepted successfully"),
        @ApiResponse(responseCode = "401", description = "User not authenticated", content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                        name = "Chat Acceptance Authentication Error",
                        summary = "Example of an authentication error response when trying to accept a chat without being authenticated",
                        value = """
                                {
                                    "timestamp": "2026-02-24T23:32:14.043425400Z",
                                    "status": 401,
                                    "error": "Access Denied",
                                    "message": "Full authentication is required to access this resource",
                                    "path": "/chats/{chatId}/accept"
                                }
                                """
                )
        )),
        @ApiResponse(responseCode = "404", description = "Chat not found", content = @Content)
    })
    @PatchMapping("/{chatId}/accept")
    public ResponseEntity<Void> acceptChat(
            @Parameter(description = "ID of the chat to accept") @PathVariable String chatId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) {
        chatService.acceptChat(chatId, userDetails.getId());
        messagingTemplate.convertAndSend("/topic/chat/updates", (Object) Map.of("chatId", chatId, "status", "ACCEPTED"));
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Close a Chat", description = "Closes an active chat session.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Chat closed successfully"),
        @ApiResponse(responseCode = "401", description = "User not authenticated", content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                        name = "Chat Acceptance Authentication Error",
                        summary = "Example of an authentication error response when trying to accept a chat without being authenticated",
                        value = """
                            {
                                "timestamp": "2026-02-24T23:32:14.043425400Z",
                                "status": 401,
                                "error": "Access Denied",
                                "message": "Full authentication is required to access this resource",
                                "path": "/chats/{chatId}/accept"
                            }
                            """
                )
        )),
        @ApiResponse(responseCode = "404", description = "Chat not found", content = @Content)
    })
    @PatchMapping("/{chatId}/close")
    public ResponseEntity<Void> closeChat(
            @Parameter(description = "ID of the chat to close") @PathVariable String chatId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) {
        chatService.closeChat(chatId, userDetails.getId());
        return ResponseEntity.ok().build();
    }
    @GetMapping("/history")
    @Operation(summary = "Get Chat History for User", description = "Retrieves closed chats for the authenticated user.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved chat history"),
        @ApiResponse(responseCode = "401", description = "User not authenticated", content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                        name = "Chat History Authentication Error",
                        summary = "Example of an authentication error response when trying to retrieve chat history without being authenticated",
                        value = """
                            {
                                "timestamp": "2026-02-24T23:32:14.043425400Z",
                                "status": 401,
                                "error": "Access Denied",
                                "message": "Full authentication is required to access this resource",
                                "path": "/chats/history"
                            }
                            """
                )
        ))
    })
    public ResponseEntity<Page<ChatResponseDTO>> getChatHistory(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "Pagination and sorting information for chat history")
            @PageableDefault(size = 20, sort = "closedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(chatService.getChatHistoryForUser(userDetails.getId(), pageable));
    }

    @Operation(summary = "Reject a Chat", description = "Rejects a pending chat request.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Chat rejected successfully"),
        @ApiResponse(responseCode = "401", description = "User not authenticated", content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                        name = "Chat Rejection Authentication Error",
                        summary = "Example of an authentication error response when trying to reject a chat without being authenticated",
                        value = """
                                {
                                    "timestamp": "2026-02-24T23:32:14.043425400Z",
                                    "status": 401,
                                    "error": "Access Denied",
                                    "message": "Full authentication is required to access this resource",
                                    "path": "/chats/{chatId}/reject"
                                }
                                """
                )
        )),
        @ApiResponse(responseCode = "404", description = "Chat not found", content = @Content)
    })
    @PatchMapping("/{chatId}/reject")
    public ResponseEntity<Void> rejectChat(
            @Parameter(description = "ID of the chat to reject") @PathVariable String chatId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) {
        chatService.rejectChat(chatId, userDetails.getId());
        messagingTemplate.convertAndSend("/topic/chat/updates", (Object) Map.of("chatId", chatId, "status", "REJECTED"));
        return ResponseEntity.ok().build();
    }
}
