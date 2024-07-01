package ru.practicum.shareitgate.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareitgate.client.BaseClient;
import org.springframework.stereotype.Service;
import ru.practicum.shareitgate.item.dto.CreateCommentDto;
import ru.practicum.shareitgate.item.dto.CreateItemDto;

import javax.validation.ValidationException;
import java.util.Map;

@Service
public class ItemClient extends BaseClient {
    private static final String API_PREFIX = "/items";

    @Autowired
    public ItemClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(builder.uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                .build());
    }

    public ResponseEntity<Object> createItem(Long userId, CreateItemDto creationDto) {
        return post("", userId, creationDto);
    }

    public ResponseEntity<Object> createComment(Long itemId, Long authorId, CreateCommentDto creationDto) {
        return post("/" + itemId + "/comment", authorId, creationDto);
    }

    private void validateFields(Map<String, Object> fields) {
        fields.forEach((k, v) -> {
            switch (k) {
                case "name":
                    if (v.toString().isBlank() || v.toString().length() > 60) {
                        throw new ValidationException("Name shouldn't be blank " +
                                "or size shouldn't be more than 60 characters");
                    }
                    break;
                case "description":
                    if (v.toString().isBlank() || v.toString().length() > 200) {
                        throw new ValidationException("Description's shouldn't be blank " +
                                "or size shouldn't be more than 60 characters");
                    }
                    break;
                case "available":
                    if (v == null) {
                        throw new ValidationException("Status 'available' shouldn't be empty.");
                    }
                    break;
            }
        });
    }

    public ResponseEntity<Object> updateItem(Long itemId, Long userId, Map<String, Object> fields) {
        fields.remove("id");
        validateFields(fields);
        return patch("/" + itemId, userId, fields);
    }

    public ResponseEntity<Object> getItemById(Long userId, Long itemId) {
        return get("/" + itemId, userId);
    }

    public ResponseEntity<Object> getItemsByUser(Long userId, int from, int size) {
        Map<String, Object> parameters = Map.of(
                "from", from,
                "size", size
        );
        return get("?from={from}&size={size}", userId, parameters);
    }

    public ResponseEntity<Object> searchItems(Long userId, String text, int from, int size) {
        Map<String, Object> parameters = Map.of(
                "text", text,
                "from", from,
                "size", size
        );
        return get("/search?text={text}&from={from}&size={size}", userId, parameters);
    }

    public ResponseEntity<Object> deleteItem(Long userId, Long itemId) {
        return delete("/" + itemId, userId);
    }
}
