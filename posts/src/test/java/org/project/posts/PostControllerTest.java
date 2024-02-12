package org.project.posts;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PostController.class)
@AutoConfigureMockMvc
public class PostControllerTest {

    @Autowired
    MockMvc mockMvc;
    List<Post> posts = new ArrayList<>();

    @MockBean
    PostRepository postRepository;

    @BeforeEach
    void setUp() {
        //create some posts
        posts = List.of(
                new Post(1, 1, "Hello, World!", "This is my first post.", null),
                new Post(2, 1, "Hello again, World!", "This is my second post.", null)
        );
    }

    //REST API

    //list all posts
    @Test
    void shouldFindAllPosts() throws Exception {
        String jsonResponse = """
                [
                    {
                        "id":1,
                        "userId":1,
                        "title":"Hello, World!",
                        "body":"This is my first post.",
                        "version":null
                    },
                    {
                        "id":2,
                        "userId":1,
                        "title":"Hello again, World!",
                        "body":"This is my second post.",
                        "version":null
                    }
                ]
                """;

        when(postRepository.findAll()).thenReturn(posts);
        mockMvc.perform(MockMvcRequestBuilders.get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(jsonResponse));
    }


    // /api/post/1 - pass

    @Test
    void shouldFindPostWhenGivenValidID() throws Exception {
        when(postRepository.findById(1)).thenReturn(Optional.of(posts.get(0)));

        var post = posts.get(0);
        String json = """
                 {
                        "id":1,
                        "userId":1,
                        "title":"Hello, World!",
                        "body":"This is my first post.",
                        "version":null
                    }
                """;

        mockMvc.perform(MockMvcRequestBuilders.get("/api/posts/1"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(json));
    }

    // /api/post/999 - fail
    @Test
    void shouldNotFindPostWhenGivenInvalidId() throws Exception {
        when(postRepository.findById(999)).thenThrow(PostNotFoundException.class);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/posts/1"))
                .andExpect(status().isNotFound());

    }

    @Test
    void shouldCreateNewPostWhenPostIsValid() throws Exception {
        var post = posts.get(0);
        when(postRepository.save(post)).thenReturn(post);
        String json = """
                 {
                        "id":1,
                        "userId":1,
                        "title":"Hello, World!",
                        "body":"This is my first post.",
                        "version":null
                    }
                """;


        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/posts")
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldNotCreatePostWhenPostIsInvalid() throws Exception {
        var post = new Post(3, 1, "", "", null);
        when(postRepository.save(post)).thenReturn(post);
        String json = """
                 {
                        "id":3,
                        "userId":1,
                        "title":"",
                        "body":"",
                        "version":null
                    }
                """;


        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/posts")
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    //update
    @Test
    void shouldUpdatePostWhenGivenValidPost() throws Exception {
        Post updated=new Post(1,1,"This is a new title","This is a new body",1);
        when(postRepository.findById(1)).thenReturn(Optional.of(updated));
        when(postRepository.save(updated)).thenReturn(updated);

        String json = """
                 {
                        "id":1,
                        "userId":1,
                        "title":"This is a new title",
                        "body":"This is a new body",
                        "version":1
                    }
                """;

        mockMvc.perform(MockMvcRequestBuilders.put("/api/posts/1")
                .contentType("application/json")
                .content(json))
                .andExpect(status().isOk());

    }

    //delete
    @Test
    void shouldDeletePostWhenGivenValidId() throws Exception {
        doNothing().when(postRepository).deleteById(1);
        mockMvc.perform(delete("/api/posts/1"))
                .andExpect(status().isNoContent());

        verify(postRepository, times(1)).deleteById(1);
    }
}



