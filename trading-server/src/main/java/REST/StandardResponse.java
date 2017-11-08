package REST;

import com.google.gson.JsonElement;

public class StandardResponse {
    private Integer status;
    private String message;
    private JsonElement data;

    public StandardResponse(Integer status, String message, JsonElement data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }
}
