package ar.coders.springmvcsecurity;

import ar.coders.springmvcsecurity.model.Authority;
import ar.coders.springmvcsecurity.model.EncryptionAlgorithm;
import ar.coders.springmvcsecurity.model.User;
import ar.coders.springmvcsecurity.repositories.ProductRepository;
import ar.coders.springmvcsecurity.repositories.UserRepository;


import ar.coders.springmvcsecurity.security.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@EnableAutoConfiguration(
        exclude = {DataSourceAutoConfiguration.class,
                DataSourceTransactionManagerAutoConfiguration.class,
                HibernateJpaAutoConfiguration.class})
class SpringMvcSecurityApplicationTests {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private ProductRepository productRepository;

    @MockBean
    private ProductService productService;

    SpringMvcSecurityApplicationTests() {
    }

    @Test
    @DisplayName("Authenticating with wrong user")
    public void loggingInWithWrongUser() throws Exception {
        mvc.perform(formLogin())
           .andExpect(unauthenticated());
    }

    @Test
    @DisplayName("Skip authentication and test the controller method")
    @WithMockUser(username = "mary", password = "12345")
    public void skipAuthenticationTest() throws Exception {
        when(productService.findAll()).thenReturn(List.of());
        mvc.perform(get("/main"))
           .andExpect(status().isOk())
           .andExpect(content().string(containsString("Hello, mary!")));
    }

    @Test
    @DisplayName("Test authentication with valid user")
    public void testAuthenticationWithValidUser() throws Exception {
        User mockUser = new User();
        mockUser.setUsername("mary");
        mockUser.setPassword(bCryptPasswordEncoder.encode("12345"));
        mockUser.setAlgorithm(EncryptionAlgorithm.BCRYPT);

        Authority a = new Authority();
        a.setName("read");
        a.setUser(mockUser);
        mockUser.setAuthorities(List.of(a));

        when(userRepository.findUserByUsername("mary"))
                .thenReturn(Optional.of(mockUser));

        mvc.perform(formLogin()
                            .user("mary")
                            .password("12345"))
           .andExpect(authenticated());
    }

    @Test
    @DisplayName("Test authentication with inexistent user")
    public void testAuthenticationWithInexistentUser() throws Exception {
        when(userRepository.findUserByUsername("mary"))
                .thenReturn(Optional.empty());

        mvc.perform(formLogin()
                            .user("mary")
                            .password("12345"))
           .andExpect(unauthenticated());
    }

    @Test
    @DisplayName("Test authentication with invalid password")
    public void testAuthenticationWithInvalidPassword() throws Exception {
        User mockUser = new User();
        mockUser.setUsername("mary");
        mockUser.setPassword(bCryptPasswordEncoder.encode("55555"));
        mockUser.setAlgorithm(EncryptionAlgorithm.BCRYPT);

        Authority a = new Authority();
        a.setName("read");
        a.setUser(mockUser);
        mockUser.setAuthorities(List.of(a));

        when(userRepository.findUserByUsername("mary"))
                .thenReturn(Optional.of(mockUser));

        mvc.perform(formLogin()
                            .user("mary")
                            .password("12345"))
           .andExpect(unauthenticated());
    }
}
