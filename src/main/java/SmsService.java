import com.vonage.client.VonageClient;
import com.vonage.client.messages.sms.SmsTextRequest;
import io.github.cdimascio.dotenv.Dotenv;

public class SmsService {
    private final VonageClient client;

    public SmsService() {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        String key = dotenv.get("VONAGE_API_KEY");
        String secret = dotenv.get("VONAGE_API_SECRET");
        if (key == null || secret == null) {
            throw new IllegalStateException(
                    "Missing VONAGE_API_KEY or VONAGE_API_SECRET. Check your .env file.");
        }
        client = VonageClient.builder().apiKey(key).apiSecret(secret).build();
    }

    /** Sends an SMS and returns the message ID. Throws on failure. */
    public String send(String to, String text) {
        var response = client.getMessagesClient().sendMessage(
                SmsTextRequest.builder()
                        .from("Vonage APIs")
                        .to(to)
                        .text(text)
                        .build());
        return response.getMessageUuid().toString();
    }
}
