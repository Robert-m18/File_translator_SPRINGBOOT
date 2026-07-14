package com.example.robert.config.JWT;

import com.example.robert.models.Role;
import com.example.robert.models.User;
import com.example.robert.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    // TODO: Twój test
    // Podpowiedź: userRepository.findByEmail(...) zwraca Optional<User>
    @Test
    void loadUserByUsername_shouldReturnUser_whenUserExists() {
        // given
        when(userRepository.findByEmail("test@gmail.com"))
                .thenReturn(Optional.of(new User(1L,"Test User","test@gmail.com", "testhaslo123", Role.USER)));
        // when
        UserDetails userDetails = userDetailsService.loadUserByUsername("test@gmail.com");
        // then
        assertThat(userDetails.getUsername()).isEqualTo("test@gmail.com");
        assertThat(userDetails.getPassword()).isEqualTo("testhaslo123");
        assertThat(userDetails.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_USER");
    }

    @Test
    void loadUserByUsername_shouldThrowException_whenUserNotFound() {
        // given - mock zwraca pusty Optional (użytkownik nie istnieje w "bazie")
        when(userRepository.findByEmail("brak@test.pl"))
                .thenReturn(Optional.empty());

        // when / then - metoda powinna rzucić wyjątek zamiast zwrócić null
        UsernameNotFoundException ex = assertThrows(
                UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername("brak@test.pl")
        );

        assertThat(ex.getMessage()).contains("brak@test.pl");

        // verify - upewniamy się, że mock faktycznie został wywołany z tym argumentem,
        // a nie że test przypadkiem przeszedł z innego powodu
        verify(userRepository).findByEmail("brak@test.pl");
    }
}