package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private ItemRequestClient itemRequestClient;

    @Test
    void createValidRequestDelegatesToClient() throws Exception {
        when(itemRequestClient.create(eq(1L), any())).thenReturn(ResponseEntity.ok(Map.of("id", 1)));

        mvc.perform(post("/requests")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new ItemRequestCreateDto("Need a drill"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void createRequestWithBlankDescriptionIsRejected() throws Exception {
        mvc.perform(post("/requests")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new ItemRequestCreateDto(" "))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());

        verify(itemRequestClient, never()).create(anyLong(), any());
    }

    @Test
    void getByRequestorDelegatesToClient() throws Exception {
        when(itemRequestClient.getByRequestor(1L)).thenReturn(ResponseEntity.ok(java.util.List.of()));

        mvc.perform(get("/requests").header(USER_ID_HEADER, 1))
                .andExpect(status().isOk());
    }

    @Test
    void getAllDelegatesToClient() throws Exception {
        when(itemRequestClient.getAll(1L)).thenReturn(ResponseEntity.ok(java.util.List.of()));

        mvc.perform(get("/requests/all").header(USER_ID_HEADER, 1))
                .andExpect(status().isOk());
    }

    @Test
    void getByIdDelegatesToClient() throws Exception {
        when(itemRequestClient.getById(1L, 7L)).thenReturn(ResponseEntity.ok(Map.of("id", 7)));

        mvc.perform(get("/requests/7").header(USER_ID_HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7));
    }
}
