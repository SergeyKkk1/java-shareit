package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private ItemClient itemClient;

    @Test
    void createValidItemDelegatesToClient() throws Exception {
        when(itemClient.create(eq(1L), any())).thenReturn(ResponseEntity.ok(Map.of("id", 1)));

        mvc.perform(post("/items")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new ItemDto(null, "Drill", "Powerful", true, 2L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void createItemWithoutNameIsRejected() throws Exception {
        mvc.perform(post("/items")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new ItemDto(null, null, "Powerful", true, null))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());

        verify(itemClient, never()).create(anyLong(), any());
    }

    @Test
    void createItemWithoutDescriptionIsRejected() throws Exception {
        mvc.perform(post("/items")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new ItemDto(null, "Drill", null, true, null))))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).create(anyLong(), any());
    }

    @Test
    void createItemWithoutAvailableIsRejected() throws Exception {
        mvc.perform(post("/items")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new ItemDto(null, "Drill", "Powerful", null, null))))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).create(anyLong(), any());
    }

    @Test
    void updateDelegatesToClientWithoutFullValidation() throws Exception {
        when(itemClient.update(eq(1L), eq(5L), any())).thenReturn(ResponseEntity.ok(Map.of("id", 5)));

        mvc.perform(patch("/items/5")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new ItemDto(null, "NewName", null, null, null))))
                .andExpect(status().isOk());
    }

    @Test
    void getDelegatesToClient() throws Exception {
        when(itemClient.getById(1L, 5L)).thenReturn(ResponseEntity.ok(Map.of("id", 5)));

        mvc.perform(get("/items/5").header(USER_ID_HEADER, 1))
                .andExpect(status().isOk());
    }

    @Test
    void getByOwnerDelegatesToClient() throws Exception {
        when(itemClient.getByOwner(1L)).thenReturn(ResponseEntity.ok(java.util.List.of()));

        mvc.perform(get("/items").header(USER_ID_HEADER, 1))
                .andExpect(status().isOk());
    }

    @Test
    void searchDelegatesToClient() throws Exception {
        when(itemClient.search("drill")).thenReturn(ResponseEntity.ok(java.util.List.of()));

        mvc.perform(get("/items/search").param("text", "drill"))
                .andExpect(status().isOk());
    }

    @Test
    void addCommentWithBlankTextIsRejected() throws Exception {
        mvc.perform(post("/items/5/comment")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new CommentDto(""))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addValidCommentDelegatesToClient() throws Exception {
        when(itemClient.addComment(eq(1L), eq(5L), any())).thenReturn(ResponseEntity.ok(Map.of("id", 1)));

        mvc.perform(post("/items/5/comment")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new CommentDto("Nice"))))
                .andExpect(status().isOk());
    }
}
