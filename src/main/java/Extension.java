import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;

public class Extension implements BurpExtension {
    @Override
    public void initialize(MontoyaApi montoyaApi) {
        montoyaApi.extension().setName("CBOR Wrangler");

        // Register the message editor tab provider for CBOR content
        CborMessageEditorTabProvider provider = new CborMessageEditorTabProvider(montoyaApi);
        montoyaApi.userInterface().registerHttpRequestEditorProvider(provider);
        montoyaApi.userInterface().registerHttpResponseEditorProvider(provider);

        montoyaApi.logging().logToOutput("CBOR Wrangler extension loaded successfully");
    }
}