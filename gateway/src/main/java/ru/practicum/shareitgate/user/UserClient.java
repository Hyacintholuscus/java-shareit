package ru.practicum.shareitgate.user;

import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareitgate.client.BaseClient;
import ru.practicum.shareitgate.user.dto.UserDto;

import javax.validation.ValidationException;
import java.util.Map;

@Service
public class UserClient extends BaseClient {
    private static final String API_PREFIX = "/users";

    @Autowired
    public UserClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(builder.uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                .build());
    }

    public ResponseEntity<Object> createUser(UserDto userDto) {
        return post("", userDto);
    }

    private void validateFields(Map<String, Object> fields) {
        fields.forEach((k, v) -> {
            switch (k) {
                case "name":
                    if (v.toString().isBlank() || v.toString().length() > 200) {
                        throw new ValidationException("Name's shouldn't be blank " +
                                "or size shouldn't be more than 200 characters");
                    }
                    break;
                case "email":
                    if (v.toString().isBlank() || v.toString().length() > 200
                            || !EmailValidator.getInstance().isValid(v.toString())) {
                        throw new ValidationException("Email shouldn't be blank " +
                                "or size shouldn't be more than 200 characters");
                    }
                    break;
            }
        });
    }

    public ResponseEntity<Object> updateUser(Long id, Map<String, Object> fields) {
        fields.remove("id");
        validateFields(fields);
        return patch("/" + id, fields);
    }

    public ResponseEntity<Object> getAllUsers() {
        return get("");
    }

    public ResponseEntity<Object> getUserById(Long id) {
        return get("/" + id);
    }

    public ResponseEntity<Object> deleteUserById(Long id) {
        return delete("/" + id);
    }
}
